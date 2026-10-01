#!/usr/bin/env python3
"""全部指令可用性巡检。

从监听器源码提取所有 @Command 模板 → 生成样例消息 → 在本地 E2E 环境逐条投递，
断言每条指令都能在超时内产生回复且不是「系统繁忙」异常路径。

用法：
    ./gradlew bootJar
    python3 tools/e2e/commands.py --dry-run                    # 只看生成的消息
    python3 tools/e2e/commands.py --only Status,Help           # 只跑指定命令组
    python3 tools/e2e/commands.py --db xiantao_e2e_b1 --json-out /tmp/b1.json
"""

import argparse
import json
import os
import re
import sys
import time
from pathlib import Path

import e2e

LISTENER_DIR = e2e.ROOT / "src/main/java/top/stillmisty/xiantao/handle/listener"
PLACEHOLDER = re.compile(r"\{\{([^{}]*)\}\}")
AI_UNAVAILABLE_MARKERS = ("暂时无法回应", "暂时不在")

# 占位符 → 样例值（未命中时按正则推断）
FIXED_SAMPLES = {
    "choice": "A",
    "quantity": "1",
    "amount": "1",
    "level": "1",
    "eventId": "1",
    "newNickname": "端测改名",
    "name": "端测宗门",
    "ethosDesc": "测试道统",
    "content": "测试",
    "herbInput": "测试灵草",
    "args": "测试",
    "skill": "测试法决",
    "locationName": "翠竹林",
    "mapName": "翠竹林",
}


def extract_commands(groups):
    """从监听器源码提取 (命令组, 模板)，按命令组过滤。"""
    items = []
    for path in sorted(LISTENER_DIR.glob("*Listener.java")):
        group = path.stem.removesuffix("Listener")
        for match in re.finditer(r'@Command\("((?:[^"\\]|\\.)*)"\)', path.read_text()):
            # 还原 Java 字符串转义（\\s -> \s），使模板与运行期一致
            items.append((group, match.group(1).replace("\\\\", "\\")))
    if groups:
        wanted = {g.strip().lower() for g in groups if g.strip()}
        items = [item for item in items if item[0].lower() in wanted]
    return items


def to_message(template, samples):
    """把命令模板转换成可直接发送的样例消息。"""

    def replace(match):
        inner = match.group(1)
        name, _, regex = inner.partition(",")
        name = name.strip()
        if name in samples:
            return samples[name]
        if "\\d" in regex:
            return "1"
        return "测试"

    text = PLACEHOLDER.sub(replace, template)
    text = re.sub(r"\(\?![^)]*\)", "", text)  # 去掉负向前瞻
    text = text.replace("\\s*", " ").replace("\\s+", " ")
    return re.sub(r"\s+", " ", text).strip()


def warn_missing_ai_key():
    """本地 AI key 未配置时给出提示（AI 指令会走降级文案）。"""
    local_yml = e2e.ROOT / "src/main/resources/application-local.yml"
    key = ""
    if local_yml.exists():
        match = re.search(r"^\s*api-key:\s*(\S+)\s*$", local_yml.read_text(), re.M)
        key = match.group(1) if match else ""
    if not os.environ.get("DEEPSEEK_API_KEY") and (not key or key.startswith("${")):
        print("⚠ 未配置 DEEPSEEK_API_KEY（本地配置为占位符）：地灵/秘灵/宗灵/掌柜 等 AI 指令会走降级文案\n")


def sweep(args, runner, openid, nickname, samples, commands):
    results = []
    for index, (group, template) in enumerate(commands, start=1):
        message = to_message(template, samples)
        before = runner.fake.count()
        started = time.time()
        status, _ = runner.post_event(openid, message)
        received = runner.wait_for_outbound(before + 1, timeout=args.timeout)
        elapsed = time.time() - started
        reply = ""
        if received:
            markdowns = runner.fake.markdowns()
            reply = (markdowns[-1] if markdowns else "") or ""
            if not reply:
                print(
                    "    ⚠ 出站内容为空或非 markdown："
                    + json.dumps(runner.fake.last_body(), ensure_ascii=False)[:150]
                )
        ok = status == 200 and received and "系统繁忙" not in reply
        ai_unavailable = ok and any(marker in reply for marker in AI_UNAVAILABLE_MARKERS)
        results.append(
            {
                "group": group,
                "template": template,
                "message": message,
                "http": status,
                "seconds": round(elapsed, 1),
                "ok": ok,
                "ai_unavailable": ai_unavailable,
                "reply": reply[:120],
            }
        )
        flag = "⚠" if ai_unavailable else ("✓" if ok else "✗")
        snippet = reply.replace("\n", " ")[:60] if reply else "<无回复>"
        print(f"  {flag} [{index:02d}/{len(commands)}] {group:18s} {message[:28]:30s} {snippet}")
    return results


def main():
    parser = argparse.ArgumentParser(description="全部指令可用性巡检")
    parser.add_argument("--only", default="", help="命令组白名单（逗号分隔）")
    parser.add_argument("--db", default="xiantao_e2e_cmd", help="测试库名（并行时各自独立）")
    parser.add_argument("--secret", default=e2e.DEFAULT_SECRET)
    parser.add_argument("--timeout", type=float, default=45.0, help="单条指令等待回复秒数")
    parser.add_argument("--dry-run", action="store_true", help="只打印生成的消息")
    parser.add_argument("--json-out", default="", help="结果写入 JSON 文件")
    parser.add_argument("--keep-db", action="store_true")
    args = parser.parse_args()

    groups = args.only.split(",") if args.only else []
    commands = extract_commands(groups)

    if args.dry_run:
        samples = {**FIXED_SAMPLES, "nickname": "巡检用户", "targetNickname": "巡检用户"}
        for group, template in commands:
            print(f"[{group}] {template}  ->  {to_message(template, samples)}")
        print(f"\n共 {len(commands)} 条")
        return 0

    if not commands:
        print(f"没有匹配的命令组: {args.only}")
        return 2

    runner = e2e.E2e(
        argparse.Namespace(
            app_port=0, secret=args.secret, log_dir="", keep_db=args.keep_db, db_name=args.db
        )
    )
    runner.fake = e2e.FakeQq()
    print(f"假 QQ 平台: {runner.fake.port} | 测试库: {args.db} | 待测指令: {len(commands)}")
    results = []
    try:
        runner.prepare_database()
        runner.start_app()

        openid = f"E2E-CMD-{int(time.time())}"
        nickname = f"巡检{int(time.time()) % 100000}"
        runner.post_event(openid, f"我要修仙 {nickname}")
        if not runner.wait_for_outbound(1):
            raise RuntimeError("测试玩家注册失败（未收到回复）")
        user_id = runner.psql(
            f"select user_id from user_auth where platform_open_id = '{openid}'"
        ).stdout.strip()
        if not user_id.isdigit():
            raise RuntimeError(f"未取到测试用户 id: {user_id!r}")
        runner.psql(f"update player set gm = true where id = {user_id}")
        item = runner.psql(
            "select name from item_template where type = 'POTION' order by id limit 1"
        ).stdout.strip()
        if not item:
            item = runner.psql("select name from item_template order by id limit 1").stdout.strip()
        map_name = runner.psql("select name from map_node order by id limit 1").stdout.strip()
        samples = {
            **FIXED_SAMPLES,
            "nickname": nickname,
            "targetNickname": nickname,
            "itemName": item or "测试物品",
            "locationName": map_name or "翠竹林",
            "mapName": map_name or "翠竹林",
        }
        print(f"测试玩家: {nickname} (id={user_id}, GM) | 样例物品: {item} | 样例地图: {map_name}")
        warn_missing_ai_key()

        # 把样例物品发进背包，让「使用/装备/丢弃/回收」走更深分支
        if item:
            before_grant = runner.fake.count()
            runner.post_event(openid, f"GM给物品 {nickname} {item} 3")
            runner.wait_for_outbound(before_grant + 1)
            print(f"已发放样例物品 x3: {item}\n")

        results = sweep(args, runner, openid, nickname, samples, commands)
    finally:
        runner.stop_app()
        runner.fake.stop()
        if not args.keep_db:
            runner.drop_database()

    ok_count = sum(1 for r in results if r["ok"])
    ai_count = sum(1 for r in results if r.get("ai_unavailable"))
    failed = [r for r in results if not r["ok"]]
    summary = f"\n结果: {ok_count}/{len(results)} 通过"
    if ai_count:
        summary += f"（其中 {ai_count} 条因 AI 不可用走降级文案，属环境限制）"
    print(summary)
    for failure in failed:
        print(f"  ✗ [{failure['group']}] {failure['template']} -> {failure['message']}")
        print(f"      HTTP {failure['http']} | {failure['reply'] or '<无回复>'}")

    if args.json_out:
        Path(args.json_out).write_text(json.dumps(results, ensure_ascii=False, indent=2))
        print(f"明细已写入 {args.json_out}")

    return 0 if not failed else 1


if __name__ == "__main__":
    sys.exit(main())
