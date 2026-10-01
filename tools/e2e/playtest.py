#!/usr/bin/env python3
"""玩法与易用性试玩记录。

模拟真实玩家在本地 E2E 环境（假 QQ 平台 + 独立测试库 + 真应用进程）逐条发送指令，
完整记录每条回复、延迟、长度、按钮，用于可玩性/易用性评估。

用法：
    ./gradlew bootJar
    python3 tools/e2e/playtest.py --phase newbie            # 新手旅程
    python3 tools/e2e/playtest.py --phase negative          # 易用性负路径
    python3 tools/e2e/playtest.py --phase newbie,negative --json-out /tmp/play.json
"""

import argparse
import json
import sys
import time
from pathlib import Path

import e2e


class Session:
    """单个玩家的试玩会话：逐条发指令，记录完整回复。"""

    def __init__(self, runner, fake, openid, nickname, phase):
        self.runner = runner
        self.fake = fake
        self.openid = openid
        self.nickname = nickname
        self.phase = phase
        self.steps = []

    def step(self, command, note="", timeout=60.0, wait=True):
        before_requests = self.fake.count()
        before_md = len(self.fake.markdowns())
        before_kb = len(self.fake.keyboards())
        started = time.time()
        status, _ = self.runner.post_event(self.openid, command)
        received = self.runner.wait_for_outbound(before_requests + 1, timeout=timeout) if wait else False
        elapsed = time.time() - started
        replies = self.fake.markdowns()[before_md:]
        keyboards = self.fake.keyboards()[before_kb:]
        buttons = []
        for keyboard in keyboards:
            for row in (keyboard.get("content") or {}).get("rows") or []:
                for button in row.get("buttons", []):
                    buttons.append(
                        {
                            "label": button.get("render_data", {}).get("label"),
                            "data": button.get("action", {}).get("data"),
                        }
                    )
        record = {
            "phase": self.phase,
            "step": len(self.steps) + 1,
            "command": command,
            "note": note,
            "http": status,
            "received": received,
            "seconds": round(elapsed, 2),
            "replies": replies,
            "chars": [len(r) for r in replies],
            "keyboards": len(keyboards),
            "buttons": buttons,
        }
        self.steps.append(record)
        snippet = (replies[0].replace("\n", " ")[:70] if replies else "<无回复>")
        flag = "✓" if received else "✗"
        button_hint = f"  [按钮×{len(buttons)}]" if buttons else ""
        print(
            f"  {flag} [{len(self.steps):02d}] {command[:26]:28s} "
            f"{record['seconds']:5.1f}s  {snippet}{button_hint}"
        )
        return record


def phase_newbie(runner, fake):
    """新手旅程：从零开始，玩家会做什么、机器人教得会吗。"""
    stamp = int(time.time())
    openid = f"PLAY-A-{stamp}"
    nickname = f"云中鹤{stamp % 10000}"
    session = Session(runner, fake, openid, nickname, "newbie")

    print("\n=== 阶段 1：新手旅程（未注册 → 第一小时） ===")
    session.step("帮助", "未注册玩家第一次发消息")
    session.step(f"我要修仙 {nickname}", "注册道号")
    session.step("状态", "第一眼角色面板")
    session.step("帮助", "查看全部玩法分组")
    session.step("帮助 修炼", "了解修炼")
    session.step("帮助 地图", "了解地图")
    session.step("地图", "看看自己在哪")
    map2 = runner.psql(
        "select name from map_node order by id offset 1 limit 1"
    ).stdout.strip()
    if map2:
        session.step(f"前往 {map2}", f"尝试旅行到「{map2}」")
    session.step("历练", "开始历练")
    session.step("历练结算", "立刻结算（观察是否提示未完成）")
    session.step("悬赏", "看看悬赏")
    session.step("今日运势", "每日运势")
    session.step("背包", "看看背包")
    session.step("排行榜", "排行榜")
    session.step("世界事件", "世界事件")
    session.step(f"查看 {nickname}", "查看自己的档案")
    session.step("突破", "修为不足时突破（观察引导）")
    session.step("丹方", "没有丹方时")
    session.step("法决", "没有法决时")
    session.step("宗门", "未入宗门时")
    session.step("师徒", "未拜师时")
    session.step("福地", "福地状态")
    session.step("福地地块", "福地地块")
    session.step("地灵 你好呀", "AI 地灵对话", timeout=90)
    session.step("秘境", "秘境列表")
    session.step("掌柜 你这儿有什么好货？", "AI 掌柜对话", timeout=90)
    session.step("GM帮助", "非 GM 访问 GM 指令")
    session.step("回收 不存在的东西", "回收不存在的物品", timeout=90)

    session.meta = {"openid": openid, "nickname": nickname, "user_id": None}
    return session


def phase_negative(runner, fake, newbie_session):
    """易用性负路径：缺参/错参/错别字/重复/非命令消息。"""
    openid = newbie_session.openid
    nickname = newbie_session.nickname
    session = Session(runner, fake, openid, nickname, "negative")

    print("\n=== 阶段 2：易用性负路径 ===")
    session.step("前往", "缺参数：是否静默", timeout=10)
    session.step("装备", "缺参数：是否静默", timeout=10)
    session.step("回收", "缺参数：是否静默", timeout=10)
    session.step("使用", "缺参数：是否静默", timeout=10)
    session.step("帮助 修练", "错别字（练/炼）搜索", timeout=10)
    session.step("帮助 不存在的系统", "未知帮助项", timeout=10)
    session.step("前往 不存在的洞天", "不存在的目的地", timeout=10)
    session.step("使用 不存在物品", "不存在的物品", timeout=10)
    session.step("丹方 不存在", "不存在的丹方", timeout=10)
    session.step("随便打一段没有命令的闲聊", "非命令消息", timeout=10)
    session.step("  状态  ", "前后空格容错", timeout=10)
    session.step("帮助  修炼", "多空格容错", timeout=10)
    session.step(f"我要修仙 {nickname}", "重复注册同名道号", timeout=10)
    session.step("选 Z", "无效选择（当前没有待选事件）", timeout=10)
    session.step("GM给灵石 " + nickname + " 1000", "非 GM 调用 GM 指令", timeout=10)
    return session


PHASES = {
    "newbie": phase_newbie,
    "negative": None,  # 依赖 newbie 会话
}


def main():
    parser = argparse.ArgumentParser(description="玩法与易用性试玩记录")
    parser.add_argument("--phase", default="newbie,negative", help="阶段：newbie,negative（逗号分隔）")
    parser.add_argument("--db", default="xiantao_e2e_play")
    parser.add_argument("--secret", default=e2e.DEFAULT_SECRET)
    parser.add_argument("--json-out", default="")
    parser.add_argument("--keep-db", action="store_true")
    args = parser.parse_args()

    wanted = [p.strip() for p in args.phase.split(",") if p.strip()]
    runner = e2e.E2e(
        argparse.Namespace(
            app_port=0, secret=args.secret, log_dir="", keep_db=args.keep_db, db_name=args.db
        )
    )
    runner.fake = e2e.FakeQq()
    print(f"假 QQ 平台: {runner.fake.port} | 测试库: {args.db}")
    sessions = []
    try:
        runner.prepare_database()
        runner.start_app()

        newbie = None
        if "newbie" in wanted:
            newbie = phase_newbie(runner, runner.fake)
            sessions.append(newbie)
        if "negative" in wanted:
            if newbie is None:
                raise RuntimeError("negative 阶段依赖 newbie 阶段（需在同一玩家上继续）")
            sessions.append(phase_negative(runner, runner.fake, newbie))
    finally:
        runner.stop_app()
        runner.fake.stop()
        if not args.keep_db:
            runner.drop_database()

    if args.json_out:
        payload = {
            "meta": {
                "db": args.db,
                "app_log": str(runner.tmp / "app.log"),
                "generated_at": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
            },
            "sessions": [
                {"phase": s.phase, "openid": s.openid, "nickname": s.nickname, "steps": s.steps}
                for s in sessions
            ],
        }
        Path(args.json_out).write_text(json.dumps(payload, ensure_ascii=False, indent=2))
        print(f"\n明细已写入 {args.json_out}")

    total = sum(len(s.steps) for s in sessions)
    silent = sum(1 for s in sessions for st in s.steps if not st["received"])
    print(f"共记录 {total} 步；无回复 {silent} 步")
    return 0


if __name__ == "__main__":
    sys.exit(main())
