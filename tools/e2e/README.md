# 本地端到端自测（假 QQ 平台）

让 Agent/开发者**不依赖真人操作 QQ** 就能验证完整业务链路：

```
签名事件 → Webhook 验签 → 新鲜度校验 → 事件去重 → 命令调度（匹配/认证/ScopedValue/串行车道）
        → 命令执行 → 通知追加/按钮构建 → 发送 JSON → 假平台断言
```

## 运行

```bash
./gradlew bootJar          # 先构建（工具使用 build/libs 下的 jar）
python3 tools/e2e/e2e.py   # 默认随机端口、独立测试库、跑完清理
```

可选参数：

| 参数 | 说明 |
|---|---|
| `--app-port` | 指定应用端口（默认随机空闲端口） |
| `--secret` | webhook 验签用的 clientSecret（默认内置测试值） |
| `--log-dir` | 应用日志目录（默认临时目录） |
| `--keep-db` | 保留测试库 `xiantao_e2e`，便于排查 |

## 隔离性

- **不接触真实 QQ**：本地假平台负责发放 token 与接收出站消息（`/v2/groups/{id}/messages` 等）
- **不碰开发库**：每次运行重建独立库 `xiantao_e2e`（Flyway 自动迁移），结束后删除
- 与正在运行的正式实例互不影响（独立端口 + 独立库）

## 覆盖的场景

1. 未注册用户发「帮助」→ 收到注册引导（含 op 12 回执）
2. 重复事件被去重（只产生一次出站）
3. 过期事件被拒（防重放，HTTP 401）
4. 注册 → 「状态」查询（道号/境界渲染）
5. 选择事件：回复携带按钮（文字/`选 X`/指令类型）→ 「选 A」结算 → 事件标记投递

## 未覆盖（需要真实平台）

- **真实 QQ 入站**：QQ 不允许机器人触发自己的消息事件，必须由真人或第二个账号发送
- **真实 QQ 出站**：由假平台代替；真实发送路径已由正式实例的实机验证覆盖
- **WebSocket 传输**：本工具走 webhook 模式；WS 协议栈由 `qq-gateway` 单测（假传输）+ 实机上线验证覆盖

## 全部指令可用性巡检

```bash
python3 tools/e2e/commands.py --dry-run                 # 只看生成的样例消息
python3 tools/e2e/commands.py                           # 巡检全部指令（当前 71 条）
python3 tools/e2e/commands.py --only "Map,Gm,Status"    # 只跑指定命令组
python3 tools/e2e/commands.py --db xiantao_e2e_b1 --json-out /tmp/b1.json   # 指定测试库与结果文件
```

行为：从 `handle/listener/*Listener.java` 提取全部 `@Command` 模板 → 生成样例消息（占位符按自定义正则推断取值）→
注册测试玩家并授予 GM、发放样例物品（丹药）→ 逐条投递并断言：

- 超时内收到回复，且回复不包含「系统繁忙」（异常路径）
- AI 降级文案（地灵/秘灵/宗灵/掌柜）标记为 ⚠，提示配置 `DEEPSEEK_API_KEY` 后可复验
- 路由遮蔽由单测 `CommandShadowingTest` 系统性兜底（样例消息必须由命令自身命中）

退出码：全部通过为 0，有失败为 1。

## 相关文件

- `SignEvent.java`：按 QQ 种子派生规则做 Ed25519 签名（与 `QqWebhookVerifier` 一致），JDK 单文件运行
- `e2e.py`：假平台 + 应用进程编排 + 固定场景断言（含 Webhook 验签/去重/防重放/按钮）
- `commands.py`：全部指令巡检（样例生成 + 逐条投递 + 结果汇总）
