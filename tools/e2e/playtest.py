#!/usr/bin/env python3
"""玩法与易用性试玩记录。

模拟真实玩家在本地 E2E 环境（假 QQ 平台 + 独立测试库 + 真应用进程）逐条发送指令，
完整记录每条回复、延迟、长度、按钮，用于可玩性/易用性评估。

用法：
    ./gradlew bootJar
    python3 tools/e2e/playtest.py --phase newbie                  # 新手旅程
    python3 tools/e2e/playtest.py --phase newbie,negative         # 新手 + 易用性负路径
    python3 tools/e2e/playtest.py --phase midgame                 # 中期玩家系统深挖（造数据）
    python3 tools/e2e/playtest.py --phase social                  # 双人社交
    python3 tools/e2e/playtest.py --phase newbie,negative,midgame,social --json-out /tmp/play.json
"""

import argparse
import json
import re
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
        received = (
            self.runner.wait_settled(before_requests + 1, timeout=timeout) if wait else False
        )
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
        snippet = replies[0].replace("\n", " ")[:70] if replies else "<无回复>"
        flag = "✓" if received else "✗"
        button_hint = f"  [按钮×{len(buttons)}]" if buttons else ""
        print(
            f"  {flag} [{len(self.steps):02d}] {command[:26]:28s} "
            f"{record['seconds']:5.1f}s  {snippet}{button_hint}"
        )
        return record


def register(runner, session, gm=False):
    """注册玩家并返回 user_id。"""
    session.step(f"我要修仙 {session.nickname}", "注册道号")
    user_id = runner.psql(
        f"select user_id from user_auth where platform_open_id = '{session.openid}'"
    ).stdout.strip()
    if not user_id.isdigit():
        raise RuntimeError(f"注册失败，未取到 user id: {user_id!r}")
    if gm:
        runner.psql(f"update player set gm = true where id = {user_id}")
    return user_id


def warp_player(runner, user_id, minutes=90):
    """回拨玩家活动开始时间（旅行/历练），跳过等待。"""
    runner.psql(
        f"update player set activity_start_time = now() - interval '{minutes} minutes' "
        f"where id = {user_id}"
    )


def warp_bounty(runner, user_id, minutes=90):
    """回拨进行中悬赏的开始时间，跳过等待。"""
    runner.psql(
        f"update user_bounty set start_time = now() - interval '{minutes} minutes' "
        f"where user_id = {user_id} and status = 'ACTIVE'"
    )


def first_reply(record):
    return record["replies"][0] if record["replies"] else ""


def phase_newbie(runner, fake):
    """新手旅程：从零开始，玩家会做什么、机器人教得会吗。"""
    stamp = int(time.time())
    session = Session(runner, fake, f"PLAY-A-{stamp}", f"云中鹤{stamp % 10000}", "newbie")

    print("\n=== 阶段 1：新手旅程（未注册 → 第一小时） ===")
    session.step("帮助", "未注册玩家第一次发消息")
    session.step(f"我要修仙 {session.nickname}", "注册道号")
    session.step("状态", "第一眼角色面板")
    session.step("帮助", "查看全部玩法分组")
    session.step("帮助 修炼", "了解修炼")
    session.step("帮助 地图", "了解地图")
    session.step("地图", "看看自己在哪")
    map2 = runner.psql("select name from map_node order by id offset 1 limit 1").stdout.strip()
    if map2:
        session.step(f"前往 {map2}", f"尝试旅行到「{map2}」")
    session.step("历练", "开始历练")
    session.step("历练结算", "立刻结算（观察是否提示未完成）")
    session.step("悬赏", "看看悬赏")
    session.step("今日运势", "每日运势")
    session.step("背包", "看看背包")
    session.step("排行榜", "排行榜")
    session.step("世界事件", "世界事件")
    session.step(f"查看 {session.nickname}", "查看自己的档案")
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
    return session


def phase_negative(runner, fake, newbie_session):
    """易用性负路径：缺参/错参/错别字/重复/非命令消息。"""
    session = Session(
        runner, fake, newbie_session.openid, newbie_session.nickname, "negative"
    )

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
    session.step(f"我要修仙 {session.nickname}", "重复注册同名道号", timeout=10)
    session.step("选 Z", "无效选择（当前没有待选事件）", timeout=10)
    session.step(f"GM给灵石 {session.nickname} 1000", "非 GM 调用 GM 指令", timeout=10)
    return session


def phase_midgame(runner, fake):
    """中期玩家：造数据（等级/修为/灵石/物品）后逐个系统深挖。"""
    stamp = int(time.time())
    session = Session(runner, fake, f"PLAY-B-{stamp}", f"问道{stamp % 10000}", "midgame")
    print("\n=== 阶段 3：中期玩家系统深挖（测试库造数据） ===")
    user_id = register(runner, session, gm=True)

    herbs = runner.psql(
        "select name from item_template where type='HERB' order by id limit 2"
    ).stdout.split()
    scroll = runner.psql(
        "select name from item_template where type='RECIPE_SCROLL' order by id limit 1"
    ).stdout.strip()
    blueprint = runner.psql(
        "select name from item_template where type='FORGING_BLUEPRINT' order by id limit 1"
    ).stdout.strip()
    jade = runner.psql(
        "select name from item_template where type='SKILL_JADE' order by id limit 1"
    ).stdout.strip()
    potion = runner.psql(
        "select name from item_template where type='POTION' order by id limit 1"
    ).stdout.strip()
    seed = runner.psql(
        "select name from item_template where type='SEED' order by id limit 1"
    ).stdout.strip()
    egg = runner.psql(
        "select name from item_template where type='BEAST_EGG' order by id limit 1"
    ).stdout.strip()
    equip = runner.psql("select name from equipment_template order by id limit 1").stdout.strip()
    dungeon = runner.psql("select name from dungeon_template order by id limit 1").stdout.strip()
    monster = runner.psql("select name from monster_template order by id limit 1").stdout.strip()

    print(
        f"  种子: 药材={herbs} 丹方={scroll} 图纸={blueprint} 玉简={jade} "
        f"丹药={potion} 种子={seed} 兽卵={egg} 装备={equip} 秘境={dungeon}"
    )

    # 数值与物品种子
    session.step(f"GM等级 {session.nickname} 10", "拉到 10 级", timeout=10)
    session.step(f"GM给修为 {session.nickname} 50000", "补修为", timeout=10)
    session.step(f"GM给灵石 {session.nickname} 200000", "补灵石", timeout=10)
    for item, qty, note in [
        (herbs[0] if herbs else "", 20, "药材"),
        (scroll, 3, "丹方卷轴"),
        (blueprint, 3, "锻造图纸"),
        (jade, 3, "法决玉简"),
        (potion, 5, "丹药"),
        (seed, 10, "种子"),
        (egg, 3, "兽卵"),
        (equip, 1, "装备"),
    ]:
        if item:
            session.step(f"GM给物品 {session.nickname} {item} {qty}", f"发放{note}", timeout=10)

    # 基础面板
    session.step("状态", "10 级角色面板")
    if equip:
        session.step(f"装备 {equip}", "穿上装备")
    session.step("背包", "背包全貌（含种子/兽卵/玉简）")
    session.step("突破", "修为充足后突破（可能触发雷劫）")

    # 旅行 + 历练：青石镇不可吐纳，先前往试炼之地
    session.step("前往 翠竹林", "旅行到试炼之地")
    warp_player(runner, user_id)
    session.step("状态", "回拨时间后触发到达")
    session.step("历练", "开始历练")
    warp_player(runner, user_id)
    session.step("历练结算", "回拨时间后结算历练")

    # 悬赏（回拨 user_bounty.start_time）
    bounty_reply = first_reply(session.step("悬赏", "悬赏列表"))
    bounty_match = re.search(r"ID[:：]\s*(\d+)", bounty_reply)
    if bounty_match:
        session.step(f"悬赏接取 {bounty_match.group(1)}", "接取悬赏")
        warp_bounty(runner, user_id)
        session.step("悬赏结算", "回拨时间后结算悬赏")
        session.step("悬赏", "悬赏列表（放弃测试）")
        session.step(f"悬赏接取 {bounty_match.group(1)}", "再次接取")
        session.step("悬赏放弃", "放弃悬赏")

    # 学习与炼制：卷轴/图纸/玉简都需先「使用」
    if scroll:
        session.step(f"炼方 {scroll}", "未学习时直接炼方（观察引导）")
    if blueprint:
        session.step(f"锻造 {blueprint}", "未学习时直接锻造（观察引导）")
    if scroll:
        session.step(f"使用 {scroll}", "用卷轴学丹方")
    recipe_reply = first_reply(session.step("丹方", "查看已学丹方"))
    recipe_match = re.search(r"\d+\.\s*([^\s（(]+)", recipe_reply)
    if herbs:
        session.step(f"炼 {herbs[0]}3", "手动炼丹")
    if recipe_match:
        session.step(f"炼方 {recipe_match.group(1)}", "按丹方自动炼丹")
    if blueprint:
        session.step(f"使用 {blueprint}", "用图纸学锻造")
    session.step("锻造列表", "锻造可选项")
    if blueprint:
        session.step(f"锻造 {blueprint}", "按图纸锻造")
    if equip:
        session.step(f"强化 {equip}", "强化装备")
    if jade:
        session.step(f"使用 {jade}", "使用玉简学功法")
    skill_reply = first_reply(session.step("法决", "查看已学法决"))
    skill_match = re.search(r"[「【]([^」】]+)[」】]", skill_reply)
    if skill_match:
        session.step(f"法决装载 {skill_match.group(1)}", "装载法决")
    if potion:
        session.step(f"使用 {potion}", "服用丹药")

    # 福地
    session.step("福地", "福地状态")
    session.step("福地地块", "福地地块")
    if seed:
        session.step(f"地灵 帮我把{seed}种下", "AI 地灵（工具调用）", timeout=90)
    session.step("福地渡劫", "福地渡劫")

    # 宗门（金丹期门槛：21 级起）
    session.step(f"GM等级 {session.nickname} 21", "拉到金丹期", timeout=10)
    session.step(f"宗门创建 问道宗{stamp % 1000} 以剑入道，快意恩仇", "创建宗门")
    session.step("宗门", "宗门状态")
    session.step("宗灵 你好", "AI 宗灵", timeout=90)

    # 其他系统
    event_reply = first_reply(session.step("世界事件", "世界事件"))
    event_match = re.search(r"参与\s*#(\d+)", event_reply)
    if event_match:
        session.step(f"参与事件 {event_match.group(1)}", "参与世界事件")
    session.step("今日运势", "今日运势")
    session.step("掌柜 我想买一颗小聚灵丹", "AI 掌柜购买", timeout=90)
    if herbs:
        session.step(f"回收 {herbs[0]}", "AI 回收", timeout=90)
    if monster:
        session.step(f"查看 {monster}", "查看怪物图鉴")
    session.step("排行榜 灵石", "灵石排行榜")

    # 秘境放最后（进入后状态被占用）
    session.step("秘境", "秘境列表")
    if dungeon:
        session.step(f"秘境 {dungeon}", f"进入秘境「{dungeon}」")
        session.step("秘灵 我要四处探索", "AI 秘灵", timeout=90)
    return session


def phase_social(runner, fake):
    """双人社交：拜师/收徒/护道/切磋/叛师。"""
    stamp = int(time.time())
    a = Session(runner, fake, f"PLAY-S1-{stamp}", f"青松{stamp % 10000}", "social")
    b = Session(runner, fake, f"PLAY-S2-{stamp}", f"明月{stamp % 10000}", "social")
    print("\n=== 阶段 4：双人社交 ===")
    register(runner, a)
    register(runner, b)
    a.step(f"拜师 {b.nickname}", "A 拜 B 为师")
    a.step("师徒", "A 查看师徒关系")
    b.step("师徒", "B 查看师徒关系")
    a.step(f"护道 {b.nickname}", "A 为 B 护道")
    b.step("护道查询", "B 查看护道")
    a.step(f"切磋 {b.nickname}", "A 向 B 切磋")
    a.step("叛师", "A 叛师")
    b.step(f"收徒 {a.nickname}", "B 收 A 为徒")
    a.step("师徒", "A 再看师徒")
    b.step(f"逐出师门 {a.nickname}", "B 逐出师门")
    a.step("师徒", "A 最后确认")
    return [a, b]



def phase_deepdive(runner, fake):
    """补齐深挖：早期突破、秘境入口、有境界差的师徒、可参与的世界事件。"""
    stamp = int(time.time())
    print("\n=== 阶段 5：补齐深挖（突破/秘境/师徒/世界事件） ===")

    # 1. 早期突破（修为刚好 100，成功率 100%）
    s1 = Session(runner, fake, f"PLAY-D1-{stamp}", f"试炼{stamp % 10000}", "deepdive")
    register(runner, s1, gm=True)
    s1.step(f"GM给修为 {s1.nickname} 100", "刚好够突破", timeout=10)
    s1.step("突破", "早期突破（观察成功率与文案）")
    s1.step("状态", "突破后境界")

    # 2. 世界事件（解析可参与事件）
    event_reply = first_reply(s1.step("世界事件", "世界事件"))
    event_match = re.search(r"参与\s*#(\d+)", event_reply)
    if event_match:
        s1.step(f"参与事件 {event_match.group(1)}", "参与可参与的世界事件")
    else:
        print("    （本轮没有可参与的世界事件）")

    # 3. 秘境（传送到入口节点）
    s2 = Session(runner, fake, f"PLAY-D2-{stamp}", f"洞天{stamp % 10000}", "deepdive")
    register(runner, s2, gm=True)
    s2.step(f"GM等级 {s2.nickname} 21", "金丹期", timeout=10)
    s2.step(f"GM传送 {s2.nickname} 紫府秘境", "传送到秘境入口", timeout=10)
    s2.step("秘境", "秘境列表（应展示可进入的秘境）")
    s2.step("秘境 紫府秘境", "进入秘境")
    s2.step("秘灵 我要四处探索", "AI 秘灵", timeout=90)

    # 4. 师徒（满足境界差）
    c = Session(runner, fake, f"PLAY-D3-{stamp}", f"小徒{stamp % 10000}", "deepdive")
    d = Session(runner, fake, f"PLAY-D4-{stamp}", f"师尊{stamp % 10000}", "deepdive")
    register(runner, c)
    register(runner, d, gm=True)
    d.step(f"GM等级 {d.nickname} 21", "师尊拉到金丹期", timeout=10)
    c.step(f"拜师 {d.nickname}", "拜师（境界差满足）")
    c.step("师徒", "徒弟视角")
    d.step("师徒", "师傅视角")
    c.step(f"切磋 {d.nickname}", "师徒切磋")
    d.step(f"逐出师门 {c.nickname}", "逐出师门")
    c.step("师徒", "确认师徒关系")
    c.step(f"拜师 {d.nickname}", "再次拜师")
    c.step("叛师", "叛师")
    c.step("师徒", "叛师后状态")
    return [s1, s2, c, d]


def main():
    parser = argparse.ArgumentParser(description="玩法与易用性试玩记录")
    parser.add_argument(
        "--phase", default="newbie,negative", help="阶段：newbie,negative,midgame,social,deepdive（逗号分隔）"
    )
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
        if "midgame" in wanted:
            sessions.append(phase_midgame(runner, runner.fake))
        if "social" in wanted:
            sessions.extend(phase_social(runner, runner.fake))
        if "deepdive" in wanted:
            sessions.extend(phase_deepdive(runner, runner.fake))
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
