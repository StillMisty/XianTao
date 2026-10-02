#!/usr/bin/env python3
"""数据报告生成器 —— 读取 analytics_event / player_daily_snapshot 与既有业务表，
输出供「数据分析 → 决策」使用的 Markdown 报告。

用法：
    python3 tools/analytics/report.py                     # 默认近 7 天
    python3 tools/analytics/report.py --days 30
    python3 tools/analytics/report.py --json out.json
"""

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def db_password():
    yml = (ROOT / "src/main/resources/application-local.yml").read_text()
    return re.search(r"password:\s*(\S+)", yml).group(1)


DB_NAME = "xiantao"


def q(sql, password):
    env = {"PGPASSWORD": password, "PATH": "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin"}
    out = subprocess.run(
        ["psql", "-h", "localhost", "-U", "still", "-d", DB_NAME, "-tAF", "|", "-c", sql],
        capture_output=True,
        text=True,
        env=env,
    )
    if out.returncode != 0:
        return []
    return [line.split("|") for line in out.stdout.strip().splitlines() if line]


def has_table(name, password):
    rows = q(
        "select 1 from information_schema.tables where table_name = '%s'" % name, password
    )
    return bool(rows)


def section(title):
    print(f"\n## {title}\n")


def table(headers, rows):
    if not rows:
        print("_（无数据）_")
        return
    print("| " + " | ".join(headers) + " |")
    print("|" + "|".join(["---"] * len(headers)) + "|")
    for r in rows:
        print("| " + " | ".join(str(c) for c in r) + " |")


def main():
    parser = argparse.ArgumentParser(description="仙道数据报告")
    parser.add_argument("--days", type=int, default=7)
    parser.add_argument("--db", default="xiantao")
    parser.add_argument("--json", default="")
    sys.stdout.reconfigure(encoding="utf-8")
    args = parser.parse_args()
    global DB_NAME
    DB_NAME = args.db
    password = db_password()
    result = {}

    print(f"# 仙道数据报告（近 {args.days} 天）")

    if not has_table("analytics_event", password):
        print("\n> ⚠️ 采集表尚未创建（需部署含 V1.0.63 的版本）")
        return 1

    # ============ 总览 ============
    section("总览")
    rows = q(
        f"""
        select
          (select count(*) from player) as 总玩家,
          (select count(distinct user_id) from player_daily_snapshot
            where snapshot_date >= current_date - {args.days}) as 活跃玩家,
          (select count(*) from player where create_time >= current_date - {args.days}) as 新注册,
          (select count(*) from analytics_event
            where occurred_at >= current_date - {args.days}) as 事件数
        """,
        password,
    )
    if rows:
        labels = ["总玩家", "活跃玩家", "新注册", "事件数"]
        table(labels, [[rows[0][i] for i in range(4)]])
        result["overview"] = dict(zip(labels, rows[0]))

    # 日活跃趋势（快照）
    section("日活跃（快照口径）")
    rows = q(
        f"""
        select snapshot_date, count(*), round(avg(level), 1)
        from player_daily_snapshot
        where snapshot_date >= current_date - {args.days}
        group by snapshot_date order by snapshot_date
        """,
        password,
    )
    table(["日期", "活跃数", "平均等级"], rows)

    # ============ 进度节奏 ============
    section("进度节奏（对照 ADR-0005：无装备基线 ~6 个月到大乘圆满）")
    rows = q(
        f"""
        select kind, subject, count(*), round(avg(value), 1)
        from analytics_event
        where occurred_at >= current_date - {args.days}
          and kind in ('breakthrough_attempt', 'breakthrough_result', 'level_up')
        group by kind, subject order by kind, subject
        """,
        password,
    )
    table(["事件", "主体", "次数", "均值"], rows)

    rows = q(
        f"""
        select
          case when value < 21 then '1-20 炼气/筑基'
               when value < 41 then '21-40 金丹/元婴'
               when value < 71 then '41-70 化神/炼虚'
               when value < 91 then '71-90 合体'
               else '91+ 大乘/渡劫' end as 段位,
          count(*) filter (where (payload->>'success')::boolean) as 成功,
          count(*) as 总突破,
          round(100.0 * count(*) filter (where (payload->>'success')::boolean) / count(*), 1) as 成功率
        from analytics_event
        where kind = 'breakthrough_result' and occurred_at >= current_date - {args.days}
        group by 1 order by 1
        """,
        password,
    )
    table(["段位", "成功", "总突破", "成功率%"], rows)

    # 等级分布（最新快照）
    rows = q(
        """
        select
          case when level < 21 then '1-20 炼气/筑基'
               when level < 41 then '21-40 金丹/元婴'
               when level < 71 then '41-70 化神/炼虚'
               when level < 91 then '71-90 合体'
               else '91+ 大乘/渡劫' end as 段位,
          count(*)
        from player_daily_snapshot
        where snapshot_date = (select max(snapshot_date) from player_daily_snapshot)
        group by 1 order by 1
        """,
        password,
    )
    table(["段位", "人数"], rows)

    # ============ 经济 ============
    section("经济（灵石）")
    rows = q(
        f"""
        select kind, subject as 来源, count(*) as 笔数, sum(value) as 总额, round(avg(value)) as 均值
        from analytics_event
        where kind in ('stones_gain', 'stones_spend') and occurred_at >= current_date - {args.days}
        group by kind, subject order by 总额 desc limit 20
        """,
        password,
    )
    table(["类型", "来源", "笔数", "总额", "均值"], rows)

    rows = q(
        f"""
        select
          coalesce(sum(value) filter (where kind = 'stones_gain'), 0) as 总产出,
          coalesce(sum(value) filter (where kind = 'stones_spend'), 0) as 总消耗
        from analytics_event where occurred_at >= current_date - {args.days}
        """,
        password,
    )
    table(["总产出", "总消耗"], rows)

    rows = q(
        """
        select
          case when level < 21 then '1-20' when level < 41 then '21-40'
               when level < 71 then '41-70' when level < 91 then '71-90' else '91+' end as 段位,
          round(percentile_cont(0.5) within group (order by spirit_stones)) as 中位余额,
          round(percentile_cont(0.9) within group (order by spirit_stones)) as p90余额,
          count(*)
        from player_daily_snapshot
        where snapshot_date = (select max(snapshot_date) from player_daily_snapshot)
        group by 1 order by 1
        """,
        password,
    )
    table(["段位", "中位余额", "P90余额", "人数"], rows)

    # ============ 战斗 ============
    section("战斗（遭遇）")
    rows = q(
        f"""
        select
          count(*) as 场次,
          count(*) filter (where (payload->>'won')::boolean) as 胜,
          count(*) filter (where not (payload->>'won')::boolean) as 负/平,
          round(100.0 * count(*) filter (where (payload->>'won')::boolean) / count(*), 1) as 胜率,
          round(avg(value), 1) as 均回合,
          round(avg((payload->>'exp')::numeric)) as 均经验
        from analytics_event
        where kind = 'encounter' and occurred_at >= current_date - {args.days}
        """,
        password,
    )
    table(["场次", "胜", "负/平", "胜率%", "均回合", "均经验"], rows)

    rows = q(
        f"""
        select e.subject as 怪物, count(*) as 场次,
               round(100.0 * count(*) filter (where (e.payload->>'won')::boolean) / count(*), 1) as 胜率,
               round(avg(e.value), 1) as 均回合
        from analytics_event e
        where e.kind = 'encounter' and e.occurred_at >= current_date - {args.days}
        group by 1 order by 场次 desc limit 12
        """,
        password,
    )
    table(["怪物", "场次", "胜率%", "均回合"], rows)

    # ============ 指令 ============
    section("指令使用与质量")
    rows = q(
        f"""
        select subject as 指令, count(*) as 次数,
               round(100.0 * count(*) filter (where not (payload->>'ok')::boolean) / count(*), 1) as 失败率,
               round(avg(value)) as 均耗时ms,
               round(percentile_cont(0.95) within group (order by value)) as p95ms
        from analytics_event
        where kind = 'command' and occurred_at >= current_date - {args.days}
        group by 1 order by 次数 desc limit 20
        """,
        password,
    )
    table(["指令", "次数", "失败率%", "均耗时ms", "p95ms"], rows)

    rows = q(
        f"""
        select count(*) as 命令数,
               round(100.0 * count(*) filter (where not (payload->>'ok')::boolean) / count(*), 2) as 总失败率,
               round(avg(value)) as 均耗时ms
        from analytics_event
        where kind = 'command' and occurred_at >= current_date - {args.days}
        """,
        password,
    )
    table(["命令数", "总失败率%", "均耗时ms"], rows)

    # ============ AI 对话 ============
    section("AI 对话用量（chat_history）")
    rows = q(
        f"""
        select chat_type, count(*) as 消息数, count(distinct user_id) as 用户数
        from chat_history where created_at >= current_date - {args.days}
        group by 1 order by 消息数 desc
        """,
        password,
    )
    table(["类型", "消息数", "用户数"], rows)

    # ============ 数据质量 ============
    section("数据质量")
    rows = q(
        f"""
        select kind, count(*) from analytics_event
        where occurred_at >= current_date - {args.days}
        group by 1 order by 2 desc
        """,
        password,
    )
    table(["事件类型", "数量"], rows)
    rows = q(
        "select max(snapshot_date), count(distinct snapshot_date) from player_daily_snapshot",
        password,
    )
    table(["最新快照", "快照天数"], rows)

    if args.json:
        Path(args.json).write_text(json.dumps(result, ensure_ascii=False, indent=2))
        print(f"\n已写入 {args.json}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
