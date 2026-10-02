#!/usr/bin/env python3
"""生成 100+ 级内容迁移（怪物/地图/遭遇池）。

从本地数据库读取参考数据（技能表/掉落表/物品 id），按既有节奏外推生成
`src/main/resources/db/migration/V1.0.57__seed_high_level_content.sql`。

运行：python3 tools/balance/gen_high_level_content.py
"""

import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/db/migration/V1.0.57__seed_high_level_content.sql"


def db_password():
    yml = (ROOT / "src/main/resources/application-local.yml").read_text()
    return re.search(r"password:\s*(\S+)", yml).group(1)


def psql_json(sql):
    env = {"PGPASSWORD": db_password(), "PATH": "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin"}
    out = subprocess.run(
        ["psql", "-h", "localhost", "-U", "still", "-d", "xiantao", "-tAc", sql],
        capture_output=True,
        text=True,
        env=env,
        check=True,
    ).stdout.strip()
    return json.loads(out)


def item_id(name):
    ids = psql_json(f"select coalesce(json_agg(id), '[]') from item_template where name = '{name}'")
    if not ids:
        raise RuntimeError(f"物品不存在: {name}")
    return ids[0]


def monster_ref(name):
    rows = psql_json(
        f"select coalesce(json_agg(json_build_object("
        f"'drop', drop_table, 'skills', skills, 'tags', tags)), '[]') "
        f"from monster_template where name = '{name}'"
    )
    return rows[0]


# ============ 新怪物（102~125，按既有 +7.5% HP / +4.5% ATK / +5% DEF / +8% EXP 外推） ============
# (名称, 等级, HP, ATK, DEF, SPD, EXP, 参考模板, 怪物类型, 描述)
NEW_MONSTERS = [
    ("太虚烛龙", 102, 17500, 500, 220, 95, 23500, "应龙", "BEAST", "烛九阴之遗脉，睁眼为昼、闭眼为夜，盘踞太虚天河之底。"),
    ("元凤", 104, 20500, 545, 240, 100, 27500, "鲲鹏", "FLYING", "凤凰一族最古之血裔，振翅间星火燎原。"),
    ("混元兽", 106, 24000, 595, 265, 105, 32000, "混沌", "WILD_BEAST", "自开天辟地前的混元之气中孕育，无形无相。"),
    ("大罗金仙", 108, 28000, 650, 290, 110, 37500, "阎罗天子", "HUMAN", "陨落的大罗金仙残念，金身不灭、道韵犹存。"),
    ("无极魔尊", 110, 32500, 710, 320, 115, 43500, "原始天魔", "EVIL", "魔道极致所化，一念可令星河倒悬。"),
    ("混世魔猿", 112, 38000, 775, 350, 120, 51000, "原始天魔", "BEAST", "天生地养的混世四猴之首，一怒而山河碎。"),
    ("太上道君", 114, 44500, 845, 385, 125, 59500, "阎罗天子", "HUMAN", "太上道统的护道法相，道法自然、无懈可击。"),
    ("无量天尊", 116, 52000, 925, 420, 130, 69500, "神龙", "SPIRIT", "无量光中显化的天尊法身，镇压诸天。"),
    ("大罗天魔", 118, 61000, 1010, 460, 135, 81500, "原始天魔", "EVIL", "天魔波旬之大罗化身，蛊惑道心、吞噬气运。"),
    ("天劫化身", 120, 71000, 1100, 505, 140, 95000, "混沌", "SPIRIT", "天劫本源凝聚的化身，执掌雷罚、审判万灵。"),
    ("洪荒祖龙", 122, 83000, 1200, 550, 145, 111000, "神龙", "BEAST", "洪荒第一头祖龙，龙威所至四海臣服。"),
    ("鸿蒙道尊", 125, 105000, 1380, 625, 155, 137000, "原始天魔", "ARMORED", "鸿蒙未判时便已存在的道之化身，万法之源。"),
]

# ============ 新地图 ============
# (id, 名称, 类型, 等级, 描述, 遭遇丰富度, 邻接[(目标, 分钟)], 特产[物品名], 遭遇怪物[名称])
NEW_MAPS = [
    (45, "太虚天河", "TRAINING_ZONE", 102, "横贯太虚的星河之水，河底沉浮着上古神兽的骸骨。", 7,
     [(44, 10), (43, 15), (42, 18), (46, 14)], ["混沌石", "龙鳞"], ["太虚烛龙", "元凤"]),
    (46, "混元道场", "TRAINING_ZONE", 104, "开天辟地前的混元之气在此流转，道韵化为有形的道场。", 7,
     [(45, 14), (47, 12)], ["混沌石", "太阳真金"], ["元凤", "混元兽"]),
    (47, "大罗云海", "TRAINING_ZONE", 106, "大罗天外的云海，每一缕云雾都是一道残破的道法。", 7,
     [(46, 12), (48, 12)], ["天劫晶", "凤羽"], ["混元兽", "大罗金仙"]),
    (48, "无极魔渊", "TRAINING_ZONE", 108, "魔气自渊底翻涌而上，坠入者皆成魔。", 7,
     [(47, 12), (49, 12)], ["真龙精血", "龙鳞"], ["大罗金仙", "无极魔尊"]),
    (49, "周天星斗", "TRAINING_ZONE", 110, "三百六十五颗主星列阵于此，星光如剑、周而复始。", 7,
     [(48, 12), (50, 14)], ["天劫晶", "混沌石"], ["无极魔尊", "混世魔猿"]),
    (50, "天罚雷池", "TRAINING_ZONE", 112, "雷霆如雨、万古不歇的雷池，雷劫本源在此显化。", 7,
     [(49, 14), (51, 12)], ["天劫晶", "大乘丹"], ["混世魔猿", "太上道君"]),
    (51, "无量天宫", "SAFE_TOWN", 116, "无量天尊遗留的天宫，仙乐缭绕，是三界外最后的栖身之所。", 3,
     [(50, 12), (52, 14)], [], []),
    (52, "界外天", "TRAINING_ZONE", 118, "三界之外的虚空夹层，天道法则在此残缺不全。", 7,
     [(51, 14), (53, 16)], ["混沌石", "真龙精血"], ["无量天尊", "大罗天魔"]),
    (53, "鸿蒙秘境", "HIDDEN_ZONE", 122, "鸿蒙初判时被剥离的一角秘境，蕴藏着道之本源。", 5,
     [(52, 16)], ["混沌石", "天劫晶"], ["天劫化身", "洪荒祖龙", "鸿蒙道尊"]),
]


def jsonb_sql(obj, indent):
    """把 JSON 对象内联为 jsonb_build_object/array 风格的 SQL。"""
    pad = " " * indent
    if isinstance(obj, dict):
        parts = []
        for k, v in obj.items():
            parts.append(f"{pad}        '{k}',\n{jsonb_sql(v, indent + 8)}")
        return f"{pad}jsonb_build_object(\n" + ",\n".join(parts) + f"\n{pad})"
    if isinstance(obj, list):
        if not obj:
            return f"{pad}jsonb_build_array()"
        parts = [jsonb_sql(v, indent + 4) for v in obj]
        return f"{pad}jsonb_build_array(\n" + ",\n".join(parts) + f"\n{pad})"
    return f"{pad}{json.dumps(obj, ensure_ascii=False)}"


def main():
    refs = {name: monster_ref(name) for name in {m[7] for m in NEW_MONSTERS}}
    lines = []
    lines.append("-- 100+ 级内容：怪物 / 地图 / 遭遇池（由 tools/balance/gen_high_level_content.py 生成）\n")

    # 1. 新怪物
    lines.append("INSERT\n    INTO\n        monster_template(name, description, monster_type, base_level,")
    lines.append("            base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags)")
    lines.append("    VALUES")
    blocks = []
    for name, level, hp, atk, dfn, spd, exp, ref, mtype, desc in NEW_MONSTERS:
        r = refs[ref]
        skills = json.dumps(r["skills"], ensure_ascii=False)
        drop = json.dumps(r["drop"], ensure_ascii=False)
        tags = json.dumps(r["tags"], ensure_ascii=False)
        blocks.append(
            f"    ('{name}', '{desc}', '{mtype}', {level}, {hp}, {atk}, {dfn}, {spd}, {exp},\n"
            f"        '{skills}'::jsonb, '{drop}'::jsonb, '{tags}'::jsonb)"
        )
    lines.append(",\n".join(blocks) + ";\n")

    # 1.5 事件类型登记（activity_event.code 的外键目标）
    lines.append("INSERT\n    INTO\n        event_type(activity_type, code, name, description)")
    lines.append("    VALUES")
    et_blocks = [
        f"    ('TRAINING', 'combat_monster_{m[0]}', '{m[0]}', '遭遇{m[0]}')" for m in NEW_MONSTERS
    ]
    lines.append(",\n".join(et_blocks) + ";\n")

    # 2. 新地图
    lines.append("INSERT\n    INTO\n        map_node(id, name, description, map_type, level_requirement,")
    lines.append("            neighbors, specialties, encounter_richness)")
    lines.append("    VALUES")
    map_blocks = []
    for mid, name, mtype, level, desc, rich, neighbors, items, monsters in NEW_MAPS:
        nb = jsonb_sql([{"targetId": t, "minutes": m} for t, m in neighbors], 8)
        specs = [{"templateId": item_id(i), "weight": 30 // max(1, len(items))} for i in items]
        sp = jsonb_sql(specs, 8) if specs else "jsonb_build_array()"
        map_blocks.append(
            f"    ({mid}, '{name}', '{desc}', '{mtype}', {level},\n        {nb},\n        {sp},\n        {rich})"
        )
    lines.append(",\n".join(map_blocks) + ";\n")

    # 3. 既有地图补边（双向连通）
    lines.append("-- 既有地图追加通往新区域的边（双向连通）")
    for src, minutes in ((44, 10), (43, 15), (42, 18)):
        lines.append(
            f"UPDATE map_node SET neighbors = neighbors || jsonb_build_array("
            f"jsonb_build_object('targetId', 45, 'minutes', {minutes})) WHERE id = {src};"
        )
    lines.append("")

    # 4. 遭遇池
    lines.append("INSERT\n    INTO\n        activity_event(activity_type, owner_id, code, event_type, weight, params)")
    lines.append("    VALUES")
    event_blocks = []
    for mid, name, mtype, level, desc, rich, neighbors, items, monsters in NEW_MAPS:
        if not monsters:
            continue
        for mon in monsters:
            w = 40
            event_blocks.append(
                f"    ('TRAINING', {mid}, 'combat_monster_{mon}', 'COMBAT', {w},\n"
                f"        jsonb_build_object(\n"
                f"            'monster_template_id', (SELECT id FROM monster_template WHERE name = '{mon}'),\n"
                f"            'min_count', 1,\n"
                f"            'max_count', {'2' if mtype == 'TRAINING_ZONE' else '1'}\n"
                f"        ))"
            )
    lines.append(",\n".join(event_blocks) + ";\n")

    OUT.write_text("\n".join(lines))
    print(f"已生成 {OUT}（{len(NEW_MONSTERS)} 怪物 / {len(NEW_MAPS)} 地图 / {len(event_blocks)} 遭遇）")


if __name__ == "__main__":
    main()
