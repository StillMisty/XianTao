#!/usr/bin/env python3
"""进度模拟器 —— 以数据库种子数据 + 代码公式为输入，评估 1~110 级的成长曲线。

用途：数值调整前后对比，验证「开局简单、后面越来越难」的长线挂机节奏。

模型口径（与代码对齐）：
- 修为需求 = 100 × 等级²（存储上限 = 5 倍）；突破失败同样消耗一份需求。
- 历练收益/分钟 = max(地图等级 × 5, √有效悟性 × 12) × 效率(1 + 身法×1%, 上限 3) × 等级衰减
  （高于地图等级 5 级后每级 -4%，下限 0.1）。
- 战斗收益 = Σ 怪物 exp_reward × 数量 × (1 + (怪物等级 − 玩家等级)×5%, 钳制 [0.1,3])；
  遭遇频率来自 EncounterCalculator（间隔 [3,20] 分钟，每次 slot 掷骰）。
- 属性成长：初始四维各 5，有效值 = 存储值 + 4 + 等级；大境界突破（每 10 级）存储值 +20%（复利）。
- 突破成功率 = logistic(等级/65)^4 + 失败补偿（每次失败 +5%~25%），钳制 [0,100]；
  大境界/渡劫期走战斗（本模拟按成功率×0.85 粗估，保守）。
- 默认无装备、无灵兽、运势中性（≈1.0），作为基线；装备/灵兽会显著降低所需时间。

用法：
    python3 tools/balance/progression.py                 # 打印基线曲线
    python3 tools/balance/progression.py --json out.json # 输出完整数据
"""

import argparse
import json
import math
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def load_db_password():
    yml = (ROOT / "src/main/resources/application-local.yml").read_text()
    m = re.search(r"password:\s*(\S+)", yml)
    if not m:
        raise RuntimeError("未在 application-local.yml 找到数据库密码")
    return m.group(1)


def psql(db_password, sql):
    env = {"PGPASSWORD": db_password, "PATH": "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin"}
    out = subprocess.run(
        ["psql", "-h", "localhost", "-U", "still", "-d", "xiantao", "-tAF", "|", "-c", sql],
        capture_output=True,
        text=True,
        env=env,
        check=True,
    ).stdout.strip()
    return [line.split("|") for line in out.splitlines() if line]


def fetch_data(db_password):
    maps = []
    for row in psql(
        db_password,
        "select id, name, level_requirement, map_type, encounter_richness from map_node "
        "order by level_requirement, id",
    ):
        maps.append(
            {
                "id": int(row[0]),
                "name": row[1],
                "level": int(row[2]),
                "type": row[3],
                "richness": int(row[4]),
            }
        )

    monsters = {}
    for row in psql(
        db_password,
        "select id, name, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward "
        "from monster_template",
    ):
        monsters[int(row[0])] = {
            "name": row[1],
            "level": int(row[2]),
            "hp": int(row[3]),
            "atk": int(row[4]),
            "def": int(row[5]),
            "spd": int(row[6]),
            "exp": int(row[7]),
        }

    pools = {}
    for row in psql(
        db_password,
        "select owner_id, weight::int, params->>'monster_template_id', "
        "params->>'min_count', params->>'max_count' from activity_event "
        "where event_type = 'COMBAT' order by owner_id",
    ):
        owner = int(row[0])
        mid = row[2]
        if mid is None:
            continue
        pools.setdefault(owner, []).append(
            {
                "template": int(mid),
                "weight": float(row[1]),
                "min": int(row[3] or 1),
                "max": int(row[4] or 1),
            }
        )
    return maps, monsters, pools


# ---------------- 公式（与代码一致） ----------------


PARAMS = {
    "exp_coeff": 240.0,      # 修为需求系数（与 Player 实现一致）
    "exp_exp": 2.2,          # 修为需求指数
    "train_map_factor": 5.0,   # 历练：地图等级 × 系数
    "train_wis_factor": 12.0,  # 历练：√悟性 × 系数
    "eff_cap": 2.0,            # 身法效率加成上限（+200%）
    "decay_rate": 0.04,        # 高于地图等级 5 级后每级衰减
    "decay_offset": 5,
    "decay_floor": 0.1,
    "success_mid": 65.0,       # 突破成功率 logistic 中点
    "success_steep": 4.0,      # logistic 陡度
    "pity_min": 5.0,
    "pity_max": 20.0,
    "monster_mod_slope": 0.05,  # 怪物等级差经验修正斜率
    "monster_mod_floor": 0.1,   # 修正下限
    "monster_mod_cap": 3.0,
}


def exp_to_next(level):
    return int(PARAMS["exp_coeff"] * level ** PARAMS["exp_exp"])


def success_rate(level, fail_count):
    raw_base = 100.0 / (1.0 + (level / PARAMS["success_mid"]) ** PARAMS["success_steep"])
    pity = PARAMS["pity_min"] + (PARAMS["pity_max"] / (1.0 + (level / 50.0) ** 2))
    return max(0.0, min(100.0, raw_base + fail_count * pity))


def expected_attempts(level, cap=60):
    """含失败补偿的期望突破次数（每次失败 +pity）。"""
    fails = 0
    total_p = 0.0
    weighted = 0.0
    for _ in range(cap):
        p = success_rate(level, fails) / 100.0
        weighted += (fails + 1) * p * (1 - total_p)
        total_p += p * (1 - total_p)
        fails += 1
        if total_p > 0.999:
            break
    if total_p < 0.999:  # 兜底
        weighted += (1 - total_p) * (cap + 1)
    return weighted / max(total_p, 1e-9) if total_p > 1e-9 else cap


def stored_stats(level):
    """模拟到指定等级的存储四维（大境界 ×1.2 复利）。"""
    stats = [5, 5, 5, 5]
    for lv in range(1, level):
        if lv % 10 == 0:  # 大境界突破发生在 10→11、20→21…
            eff = [s + 4 + lv for s in stats]
            stats = [s + int(e * 0.20) for s, e in zip(stats, eff)]
    return stats


def player_power(level):
    stats = stored_stats(level)
    eff = [s + 4 + level for s in stats]
    return {
        "str": eff[0],
        "con": eff[1],
        "agi": eff[2],
        "wis": eff[3],
        "hp": 100 + eff[1] * 20,
        "atk": eff[0] * 2,
        "def": eff[1],
    }


def training_exp_per_minute(map_level, power):
    base = max(map_level * PARAMS["train_map_factor"], math.sqrt(power["wis"]) * PARAMS["train_wis_factor"])
    efficiency = 1.0 + min(power["agi"] * 0.01, PARAMS["eff_cap"])
    return base * efficiency


def encounter_interval(map_level, richness, player_level):
    map_danger = 1.0 + (map_level - 1) * 0.015
    delta = abs(player_level - map_level)
    mismatch = 1.0 + delta / max(map_level, 1) * 2.5
    base_interval = 12.0 - richness
    return max(3.0, min(20.0, base_interval * mismatch / map_danger))


def combat_exp_per_minute(pool, monsters, player_level, interval):
    if not pool:
        return 0.0, 0
    total_w = sum(e["weight"] for e in pool)
    per_minute = 0.0
    avg_monster_level = 0.0
    for e in pool:
        m = monsters.get(e["template"])
        if m is None:
            continue
        share = e["weight"] / total_w
        count = (e["min"] + e["max"]) / 2
        modifier = max(
            PARAMS["monster_mod_floor"],
            min(
                PARAMS["monster_mod_cap"],
                1.0 + (m["level"] - player_level) * PARAMS["monster_mod_slope"],
            ),
        )
        per_minute += share * m["exp"] * count * modifier
        avg_monster_level += share * m["level"]
    # 每个 slot（interval 分钟）掷骰一次，命中概率 = min(1, 4/interval)
    roll_chance = min(1.0, 4.0 / interval)
    return per_minute * roll_chance / interval, avg_monster_level


def combat_rounds(monster, player_power, advantage=1.0):
    if player_power["atk"] <= 0:
        return 9999
    player_dmg = max(1, round(player_power["atk"] * advantage) - round(monster["def"] * 0.4))
    monster_dmg = max(1, monster["atk"] - round(player_power["def"] * 0.4))
    rounds_to_kill = math.ceil(monster["hp"] / player_dmg)
    rounds_to_die = math.ceil(player_power["hp"] / monster_dmg)
    return rounds_to_kill, rounds_to_die


def best_map_for(level, maps, pools, monsters):
    candidates = [m for m in maps if m["type"] == "TRAINING_ZONE" and m["level"] <= level]
    if not candidates:
        candidates = [m for m in maps if m["type"] == "TRAINING_ZONE"]
    best = None
    for m in candidates:
        interval = encounter_interval(m["level"], m["richness"], level)
        power = player_power(level)
        train = training_exp_per_minute(m["level"], power)
        combat, avg_mlevel = combat_exp_per_minute(
            pools.get(m["id"], []), monsters, level, interval
        )
        decay = 1.0
        diff = level - m["level"] - PARAMS["decay_offset"]
        if diff > 0:
            decay = max(
                PARAMS["decay_floor"], 1.0 - diff * PARAMS["decay_rate"]
            )
        total = (train + combat) * decay
        if best is None or total > best["income"]:
            best = {
                "map": m,
                "interval": interval,
                "train": train,
                "combat": combat,
                "decay": decay,
                "income": total,
                "avg_monster_level": avg_mlevel,
            }
    return best


def simulate(maps, monsters, pools, max_level=110):
    rows = []
    cumulative_hours = 0.0
    for level in range(1, max_level + 1):
        power = player_power(level)
        best = best_map_for(level, maps, pools, monsters)
        income = best["income"] if best else 0.0
        need = exp_to_next(level)
        attempts = expected_attempts(level)
        # 大境界/渡劫期粗估：战斗突破成功率按 RNG 率的 85% 保守折算
        is_major = level % 10 == 0
        if is_major:
            attempts = attempts / 0.85
        hours = (need * attempts) / (income * 60) if income > 0 else float("inf")
        cumulative_hours += hours
        rows.append(
            {
                "level": level,
                "realm": (level - 1) // 10,
                "need": need,
                "income": round(income, 1),
                "hours": round(hours, 1),
                "cumulative_days": round(cumulative_hours / 24, 1),
                "success_pct": round(success_rate(level, 0), 1),
                "attempts": round(attempts, 2),
                "map": best["map"]["name"] if best else "-",
                "map_level": best["map"]["level"] if best else 0,
                "monster_level": round(best["avg_monster_level"], 1) if best else 0,
                "atk": power["atk"],
                "def": power["def"],
                "hp": power["hp"],
            }
        )
    return rows


def print_report(rows):
    print(
        f"{'等级':>4} {'需求':>9} {'收入/分':>8} {'小时/级':>7} {'累计天':>7} "
        f"{'成功率':>6} {'期望次':>6} {'地图':<10} {'怪级':>5} {'攻':>5} {'防':>5} {'HP':>6}"
    )
    for r in rows:
        print(
            f"{r['level']:>4} {r['need']:>9,} {r['income']:>8.1f} {r['hours']:>7.1f} "
            f"{r['cumulative_days']:>7.1f} {r['success_pct']:>5.1f}% {r['attempts']:>6.2f} "
            f"{r['map']:<10} {r['monster_level']:>5.1f} {r['atk']:>5} {r['def']:>5} {r['hp']:>6}"
        )
    print()
    print("=== 里程碑（累计时间） ===")
    for mark in (10, 21, 31, 41, 56, 71, 91, 111):
        row = next((r for r in rows if r["level"] == mark), None)
        if row:
            print(
                f"Lv.{mark:>3}: {row['cumulative_days']:>7.1f} 天 "
                f"（本级 {row['hours']:.1f}h，成功率 {row['success_pct']}%）"
            )


def main():
    parser = argparse.ArgumentParser(description="仙道进度模拟器")
    parser.add_argument("--json", default="", help="输出完整 JSON")
    parser.add_argument("--max-level", type=int, default=110)
    parser.add_argument("--set", default="", help="参数覆盖：key=value,key=value")
    parser.add_argument("--brief", action="store_true", help="只输出里程碑摘要")
    args = parser.parse_args()
    for kv in filter(None, args.set.split(",")):
        k, v = kv.split("=")
        PARAMS[k.strip()] = float(v)

    db_password = load_db_password()
    maps, monsters, pools = fetch_data(db_password)
    rows = simulate(maps, monsters, pools, args.max_level)
    if args.brief:
        marks = [10, 21, 31, 41, 56, 71, 91, 111]
        print("  ".join(f"Lv{m}:{next(r['cumulative_days'] for r in rows if r['level'] == m):.1f}d"
                        for m in marks if any(r["level"] == m for r in rows)))
    else:
        print_report(rows)
    if args.json:
        Path(args.json).write_text(json.dumps(rows, ensure_ascii=False, indent=2))
        print(f"\n已写入 {args.json}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
