#!/usr/bin/env python3
"""XianTao 本地端到端自测。

组成：本地假 QQ 平台（REST）+ 独立测试库 + 真实应用进程（webhook 模式）。
覆盖：验签 → 新鲜度 → 去重 → 调度（匹配/认证/ScopedValue）→ 命令 → 通知/按钮 → 发送 JSON。

用法：
    ./gradlew bootJar          # 先构建
    python3 tools/e2e/e2e.py   # 运行；--keep-db 保留测试库，--verbose 打印日志

不接触真实 QQ 平台，也不触碰开发库（使用独立的 xiantao_e2e 库，跑完即删）。
"""

import argparse
import json
import os
import re
import shutil
import socket
import subprocess
import sys
import tempfile
import threading
import time
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAR = ROOT / "build/libs/XianTao-0.0.1-SNAPSHOT.jar"
SIGNER = ROOT / "tools/e2e/SignEvent.java"
DEFAULT_SECRET = "e2e-secret-0123456789abcdef"
DEFAULT_DB = "xiantao_e2e"
WEBHOOK_PATH = "/qq/webhook"
REQUIRED_JAVA_MAJOR = 27


def _java_major(java: str) -> int:
    try:
        result = subprocess.run(
            [java, "-version"], capture_output=True, text=True, timeout=20, check=False
        )
        match = re.search(r'version "(\d+)', result.stderr)
        return int(match.group(1)) if match else 0
    except (OSError, subprocess.SubprocessError):
        return 0


def resolve_java() -> str:
    """显式解析 Java 27+：避免 shell PATH 漂移到旧 JDK 导致 jar 启动失败。"""
    candidates = []
    if os.environ.get("E2E_JAVA"):
        candidates.append(os.environ["E2E_JAVA"])
    if os.environ.get("JAVA_HOME"):
        candidates.append(str(Path(os.environ["JAVA_HOME"]) / "bin/java"))
    mise_java = Path.home() / ".local/share/mise/installs/java"
    if mise_java.exists():
        candidates += [
            str(path / "bin/java") for path in sorted(mise_java.iterdir(), reverse=True) if path.is_dir()
        ]
    which_java = shutil.which("java")
    if which_java:
        candidates.append(which_java)
    candidates += [
        str(path)
        for path in sorted(
            Path("/Library/Java/JavaVirtualMachines").glob("*/Contents/Home/bin/java"),
            reverse=True,
        )
    ]
    for candidate in candidates:
        if Path(candidate).exists() and _java_major(candidate) >= REQUIRED_JAVA_MAJOR:
            return candidate
    raise RuntimeError(f"未找到 Java {REQUIRED_JAVA_MAJOR}+ 运行时（可用 E2E_JAVA 指定）")


# ===================== 假 QQ 平台 =====================


class FakeQq:
    """假 QQ OpenAPI：发放 token、接收被动回复；记录全部出站请求供断言。"""

    def __init__(self):
        self.requests = []
        self._lock = threading.Lock()
        outer = self

        class Handler(BaseHTTPRequestHandler):
            def _json(self, code, obj):
                data = json.dumps(obj).encode()
                self.send_response(code)
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(data)))
                self.end_headers()
                self.wfile.write(data)

            def do_GET(self):
                if self.path == "/_requests":
                    with outer._lock:
                        return self._json(200, {"requests": list(outer.requests)})
                return self._json(404, {"error": "not_found"})

            def do_POST(self):
                length = int(self.headers.get("Content-Length", "0"))
                raw = self.rfile.read(length).decode("utf-8")
                if self.path.endswith("/getAppAccessToken"):
                    return self._json(200, {"access_token": "e2e-token", "expires_in": "7200"})
                record = {"path": self.path, "body": json.loads(raw) if raw else None}
                with outer._lock:
                    outer.requests.append(record)
                return self._json(200, {"id": "e2e-sent", "timestamp": "2026-10-01T00:00:00+08:00"})

            def log_message(self, *args):
                pass

        self.server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.port = self.server.server_address[1]
        threading.Thread(target=self.server.serve_forever, daemon=True).start()

    def stop(self):
        self.server.shutdown()

    def count(self):
        with self._lock:
            return len(self.requests)

    def last_body(self):
        with self._lock:
            return self.requests[-1]["body"] if self.requests else None

    def bodies(self):
        with self._lock:
            return [r["body"] for r in self.requests if r.get("body")]

    def markdowns(self):
        return [b["markdown"]["content"] for b in self.bodies() if "markdown" in b]

    def keyboards(self):
        return [b["keyboard"] for b in self.bodies() if "keyboard" in b]


# ===================== 断言与输出 =====================


class Checks:
    def __init__(self):
        self.passed = 0
        self.failed = 0

    def expect(self, condition, message):
        if condition:
            self.passed += 1
            print(f"    ✓ {message}")
        else:
            self.failed += 1
            print(f"    ✗ {message}")

    def finish(self):
        print(f"\n结果: {self.passed} 通过, {self.failed} 失败")
        return 1 if self.failed else 0


# ===================== 编排 =====================


class E2e:
    def __init__(self, args):
        self.args = args
        self.checks = Checks()
        self.secret = args.secret
        self.tmp = Path(args.log_dir) if args.log_dir else Path(tempfile.mkdtemp(prefix="xiantao-e2e-"))
        self.tmp.mkdir(parents=True, exist_ok=True)
        self.pg_user, self.pg_password = self._read_db_credentials()
        self.db_name = getattr(args, "db_name", DEFAULT_DB) or DEFAULT_DB
        self.java = resolve_java()
        self.app = None
        self.app_log_handle = None
        self.fake = None
        self.event_seq = 0
        self.registered_openid = None
        self.registered_nickname = None

    # ---------- 基础设施 ----------

    @staticmethod
    def _free_port():
        with socket.socket() as s:
            s.bind(("127.0.0.1", 0))
            return s.getsockname()[1]

    @staticmethod
    def _read_db_credentials():
        text = (ROOT / "src/main/resources/application-local.yml").read_text()

        def field(name):
            match = re.search(rf"^\s*{name}:\s*(\S+)\s*$", text, re.M)
            if not match:
                return ""
            value = match.group(1)
            placeholder = re.fullmatch(r"\$\{[A-Z_]+:([^}]*)}", value)
            return placeholder.group(1) if placeholder else value

        return field("username") or "postgres", field("password")

    def psql(self, sql, db=None, check=True):
        env = dict(os.environ, PGPASSWORD=self.pg_password)
        result = subprocess.run(
            ["psql", "-h", "localhost", "-U", self.pg_user, "-d", db or self.db_name, "-tAc", sql],
            env=env,
            check=False,
            capture_output=True,
            text=True,
        )
        if check and result.returncode != 0:
            raise RuntimeError(f"psql 执行失败: {result.stderr.strip()}\nSQL: {sql}")
        return result

    def prepare_database(self):
        self.psql(f"drop database if exists {self.db_name}", db="postgres")
        self.psql(f"create database {self.db_name}", db="postgres")
        print(f"测试库已重建: {self.db_name}")

    def drop_database(self):
        self.psql(
            "select pg_terminate_backend(pid) from pg_stat_activity "
            f"where datname = '{self.db_name}' and pid <> pg_backend_pid()",
            db="postgres",
            check=False,
        )
        self.psql(f"drop database if exists {self.db_name}", db="postgres", check=False)
        print(f"测试库已删除: {self.db_name}")

    def start_app(self):
        app_port = self.args.app_port or self._free_port()
        log = self.tmp / "app.log"
        env = dict(
            os.environ,
            QQ_APP_ID="100000001",
            QQ_CLIENT_SECRET=self.secret,
            QQ_TRANSPORT="webhook",
        )
        cmd = [
            self.java,
            "--enable-native-access=ALL-UNNAMED",
            "-jar",
            str(JAR),
            f"--server.port={app_port}",
            "--xiantao.qq.transport=webhook",
            f"--xiantao.qq.api-base-url=http://127.0.0.1:{self.fake.port}",
            f"--xiantao.qq.app-base-url=http://127.0.0.1:{self.fake.port}",
            f"--spring.datasource.url=jdbc:postgresql://localhost:5432/{self.db_name}",
            "--logging.level.top.stillmisty.xiantao=INFO",
        ]
        print(f"启动应用: port={app_port}, log={log}")
        self.app_log_handle = log.open("w")
        self.app = subprocess.Popen(
            cmd, env=env, stdout=self.app_log_handle, stderr=subprocess.STDOUT
        )
        self.app_port = app_port
        deadline = time.time() + 120
        while time.time() < deadline:
            if self.app.poll() is not None:
                raise RuntimeError(f"应用启动失败，退出码 {self.app.returncode}，见 {log}")
            if "Started XianTaoApplication" in log.read_text(errors="ignore"):
                print("应用已启动")
                return
            time.sleep(1)
        raise RuntimeError(f"应用启动超时，见 {log}")

    def stop_app(self):
        if self.app and self.app.poll() is None:
            self.app.terminate()
            try:
                self.app.wait(timeout=15)
            except subprocess.TimeoutExpired:
                self.app.kill()
        if self.app_log_handle:
            self.app_log_handle.close()
            self.app_log_handle = None

    # ---------- 事件构造与投递 ----------

    def sign(self, body: bytes) -> str:
        body_file = self.tmp / "event.json"
        body_file.write_bytes(body)
        result = subprocess.run(
            [self.java, str(SIGNER), self.secret, str(body_file)],
            check=True,
            capture_output=True,
            text=True,
        )
        return result.stdout.strip()

    def post_event(self, openid, content, *, event_id=None, timestamp=None, group="E2E-GROUP"):
        self.event_seq += 1
        event_id = event_id or f"EV-{self.event_seq}"
        timestamp = timestamp or time.strftime("%Y-%m-%dT%H:%M:%S+08:00")
        payload = {
            "op": 0,
            "s": self.event_seq,
            "t": "GROUP_AT_MESSAGE_CREATE",
            "id": event_id,
            "d": {
                "id": f"MSG-{event_id}",
                "content": content,
                "timestamp": timestamp,
                "group_openid": group,
                "author": {
                    "id": openid,
                    "member_openid": openid,
                    "username": "端测用户",
                    "bot": False,
                },
            },
        }
        body = json.dumps(payload, ensure_ascii=False).encode()
        request = urllib.request.Request(
            f"http://127.0.0.1:{self.app_port}{WEBHOOK_PATH}",
            data=body,
            headers={"Content-Type": "application/json", "X-Signature-Ed25519": self.sign(body)},
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=15) as response:
                return response.status, response.read().decode()
        except urllib.error.HTTPError as error:
            return error.code, error.read().decode()

    def wait_for_outbound(self, expected_count, timeout=15):
        deadline = time.time() + timeout
        while time.time() < deadline:
            if self.fake.count() >= expected_count:
                return True
            time.sleep(0.2)
        return False

    def wait_settled(self, expected_count, timeout=30, stable=1.0):
        """等待出站消息达到预期条数并稳定（兼容处理中提示与长回复分段）。"""
        deadline = time.time() + timeout
        stable_since = None
        last = -1
        while time.time() < deadline:
            count = self.fake.count()
            if count >= expected_count and count == last:
                if stable_since is None:
                    stable_since = time.time()
                elif time.time() - stable_since >= stable:
                    return True
            else:
                stable_since = None
            last = count
            time.sleep(0.2)
        return self.fake.count() >= expected_count

    # ---------- 场景 ----------

    def scenarios(self):
        unregistered = f"E2E-UNREG-{int(time.time())}"

        print("\n[1] 未注册用户发「帮助」→ 注册引导")
        before = self.fake.count()
        status, ack = self.post_event(unregistered, "帮助")
        self.checks.expect(status == 200, f"回调返回 200（实际 {status}）")
        self.checks.expect('"op":12' in ack, "返回 op 12 回执")
        self.checks.expect(self.wait_for_outbound(before + 1), "收到一条出站回复")
        markdown = self.fake.markdowns()[-1]
        self.checks.expect("我要修仙" in markdown, f"回复包含注册引导（实际：{markdown[:40]}…）")
        self.checks.expect(not self.fake.keyboards(), "无待选择事件时不携带按钮")

        print("\n[2] 重复事件被去重")
        duplicate_id = f"EV-dup-{int(time.time())}"
        count_before = self.fake.count()
        self.post_event(unregistered, "帮助", event_id=duplicate_id)
        self.checks.expect(
            self.wait_for_outbound(count_before + 1), "首次投递产生一次出站"
        )
        count_after_first = self.fake.count()
        body_status, _ = self.post_event(unregistered, "帮助", event_id=duplicate_id)
        time.sleep(1.0)
        self.checks.expect(body_status == 200, "重复事件仍返回 200")
        self.checks.expect(
            self.fake.count() == count_after_first,
            f"重复事件不再产生出站（实际新增 {self.fake.count() - count_after_first}）",
        )

        print("\n[3] 过期事件被拒绝（防重放）")
        count_before = self.fake.count()
        stale_ts = time.strftime(
            "%Y-%m-%dT%H:%M:%S+08:00", time.localtime(time.time() - 3600)
        )
        status, _ = self.post_event(unregistered, "帮助", timestamp=stale_ts)
        time.sleep(0.5)
        self.checks.expect(status == 401, f"过期事件返回 401（实际 {status}）")
        self.checks.expect(self.fake.count() == count_before, "过期事件不产生出站")

        print("\n[4] 注册 → 状态查询")
        self.registered_openid = f"E2E-REG-{int(time.time())}"
        self.registered_nickname = f"端测{int(time.time()) % 100000}"
        before = self.fake.count()
        status, _ = self.post_event(
            self.registered_openid, f"我要修仙 {self.registered_nickname}"
        )
        self.checks.expect(status == 200, "注册事件返回 200")
        self.checks.expect(self.wait_for_outbound(before + 1), "收到注册回复")
        self.checks.expect(
            "欢迎踏入仙途" in self.fake.markdowns()[-1],
            f"注册成功（实际：{self.fake.markdowns()[-1][:50]}…）",
        )

        before = self.fake.count()
        self.post_event(self.registered_openid, "状态")
        self.checks.expect(self.wait_for_outbound(before + 1), "收到状态回复")
        status_markdown = self.fake.markdowns()[-1]
        self.checks.expect(
            self.registered_nickname in status_markdown, "状态回复包含道号"
        )
        self.checks.expect("境界" in status_markdown, "状态回复包含境界")

        print("\n[5] 选择事件：按钮 + 选择结算")
        user_id = self.psql(
            "select user_id from user_auth "
            f"where platform_open_id = '{self.registered_openid}'"
        ).stdout.strip()
        self.checks.expect(user_id.isdigit(), f"取到测试用户 id（{user_id}）")
        self.psql(
            "insert into game_event(user_id, category, occurred_at, delivered, narrative_args, effects) "
            f"values ({user_id}, 'TRAVEL_EVENT', now(), false, '{{}}'::jsonb, "
            """'{"choice":{"options":[{"key":"A","text":"接受考验","effects":[]},"""
            """{"key":"B","text":"转身离开","effects":[]}]}}'::jsonb)"""
        )
        before = self.fake.count()
        self.post_event(self.registered_openid, "状态")
        self.checks.expect(self.wait_for_outbound(before + 1), "收到带选择事件的回复")
        keyboards = self.fake.keyboards()
        self.checks.expect(bool(keyboards), "回复携带按钮键盘")
        if keyboards:
            buttons = keyboards[-1]["content"]["rows"][0]["buttons"]
            self.checks.expect(len(buttons) == 2, f"两个按钮（实际 {len(buttons)}）")
            self.checks.expect(
                buttons[0]["render_data"]["label"] == "接受考验", "按钮文字取选项文本"
            )
            self.checks.expect(buttons[0]["action"]["data"] == "选 A", "点击发送「选 A」")
            self.checks.expect(buttons[0]["action"]["type"] == 2, "指令按钮（type=2）")

        before = self.fake.count()
        self.post_event(self.registered_openid, "选 A")
        self.checks.expect(self.wait_for_outbound(before + 1), "收到选择结算回复")
        choice_markdown = self.fake.markdowns()[-1]
        self.checks.expect("你选择了" in choice_markdown, "结算文案正确")
        self.checks.expect(
            self.fake.count() - before == 1, "结算后不再附带待选择通知"
        )

        remaining = self.psql(
            "select count(*) from game_event where user_id = "
            f"{user_id} and delivered = false"
        ).stdout.strip()
        self.checks.expect(remaining == "0", f"选择事件已标记投递（剩余 {remaining}）")

    # ---------- 入口 ----------

    def run(self):
        if not JAR.exists():
            print(f"缺少 {JAR}，请先执行 ./gradlew bootJar")
            return 2
        self.fake = FakeQq()
        print(f"假 QQ 平台已启动: http://127.0.0.1:{self.fake.port}")
        try:
            self.prepare_database()
            self.start_app()
            self.scenarios()
        finally:
            self.stop_app()
            self.fake.stop()
            if self.args.keep_db:
                print(f"保留测试库: {self.db_name}")
            else:
                self.drop_database()
        return self.checks.finish()


def main():
    parser = argparse.ArgumentParser(description="XianTao 本地端到端自测")
    parser.add_argument("--app-port", type=int, default=0, help="应用端口（默认随机）")
    parser.add_argument("--secret", default=DEFAULT_SECRET, help="webhook 验签用 clientSecret")
    parser.add_argument("--log-dir", default="", help="日志目录（默认临时目录）")
    parser.add_argument("--keep-db", action="store_true", help="跑完保留测试库")
    parser.add_argument("--db", default=DEFAULT_DB, help=f"测试库名（默认 {DEFAULT_DB}）")
    return E2e(parser.parse_args()).run()


if __name__ == "__main__":
    sys.exit(main())
