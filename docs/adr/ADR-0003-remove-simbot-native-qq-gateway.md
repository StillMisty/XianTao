# ADR-0003: 移除 SimBot，自研 qq-gateway 模块（WebSocket + Webhook）

## 状态

已接受

## 背景

项目唯一的机器人框架依赖是 SimBot `5.0.0-Preview5`（Kotlin/协程技术栈），但实际能力面极窄：

- 收 3 类事件（`GROUP_AT_MESSAGE_CREATE` / `GROUP_MESSAGE_CREATE` / `C2C_MESSAGE_CREATE`），被动回 Markdown；
- 未使用消息元素体系、`Bot`/`BotManager`、主动发送、Ark/Card 等任何高级能力；
- 31 个文件、约 1.6k 行 `import love.forte.*`，全部集中在 `handle/` 与启动类；`domain/`、`service/`、`handle/command/` 零依赖。

工程风险与运行代价：

1. **预览版与兼容面**：5.0.0 预览线自 2026-01 起历经 Preview1→Preview5 仍无正式版；starter 的 `dependencyManagement` 引用 Spring Boot 3.2.1，项目运行在 Boot 4.1.1 / Java 27，属未验证组合；组件内嵌 Ktor 3.3.3 与项目显式 3.6.0 跨版本混用。
2. **技术栈割裂**：Kotlin stdlib/reflect、kotlinx-serialization/datetime、协程调度全部进入运行时；NullAway/JSpecify 体系里混入 JetBrains `@Nullable`。
3. **线程模型代价**：协程模型下「认证拦截器在原始线程、业务 handler 在 IO 线程」，`UserContext` 不得不以事件对象为 key、用两个 `WeakHashMap` + 双检锁做跨线程传递（`service/UserContext.java` 旧实现），存在内存与竞态隐患。
4. **协议行为不可控**：重连、去重、被动回复窗口、`msg_seq`、限流等细节封装在框架内部，无法按需调整。

## 决策

1. 新建独立 Gradle 子模块 **`qq-gateway`**（纯 Java + Jackson + SLF4J，零 Spring 依赖），直接实现 QQ 开放平台接入：
   - **凭据**：`POST /app/getAppAccessToken` 获取 Access Token，提前 5 分钟刷新、单飞行；
   - **WebSocket**：`GET /gateway` → Hello/Identify/Heartbeat/Resume，指数退避重连，事件按 `id` 去重；
   - **Webhook**：AppSecret 派生 Ed25519 种子验签（重算签名常量时间比较）、op 13 地址校验应答、op 12 回执；
   - **发送**：被动回复自动分配 `msg_seq`（同一 `msg_id` 复用序号重试，避免重复消息），Token 失效/限流自动重试；另暴露群/单聊主动发送接口。
2. WebSocket 鉴权 token 的两种官方口径（新版 `QQBot {AccessToken}` / 旧版 `Bot {appId}.{appToken}`）做成可配置策略，默认新版，线上以沙箱联调为准。
3. 应用侧用 `handle/dispatch` 取代 quantcat：`@Command`（模板语义与 `@Filter` 逐条对齐）、`@Arg`、`@RequireAuth`/`@RequireGm`；拦截顺序固化为「匹配 → 认证 → GM」，与 ADR-0002 语义一致。
4. **每条消息一条虚拟线程端到端执行**（匹配→认证→业务→回复），`ScopedValue` 直接绑定，删除 `UserContext` 的跨线程传递实现。
5. `openId` 取值与 SimBot 完全一致（群聊 `author.member_openid`、单聊 `author.user_openid`），保证既有 `user_auth` 绑定关系不失效。

## 理由

- 能力面窄且协议稳定（v2 API），JDK 自带 `HttpClient`/`WebSocket` 与虚拟线程足以覆盖，零新增第三方运行时依赖，同时移除 Ktor 与整个 Kotlin 运行时。
- 需要「协议行为完全可控」：重连、去重、被动窗口、限流、`msg_seq` 都要能观测、能测试、能调整。

## 后果

### 正面

- 摆脱预览版依赖与 Boot 3.2 编译基线，Spring Boot / Java 升级不再受 starter 桥接限制。
- 线程模型简化：一事件一线程，`ScopedValue` 全程有效；`UserContext` 由 88 行降为 35 行。
- 协议层可测试：`qq-gateway` 25 个单测（含 RFC 8032 向量、Identify/Resume、去重、Token 刷新、429 重试），`handle/dispatch` 12 个单测（含全部 71 个真实命令模板的一致性回归）。
- 应用依赖图中不再有 Kotlin/GPL-LGPL 组件。

### 负面

- 协议维护成本转移给项目自身：QQ 开放平台接口变更需要自行跟进。
- 需要真实凭证联调确认：WS token 口径、网关域名（已做成可配置）、Webhook 公网地址。

### 注意事项

- Webhook 模式要求公网 HTTPS 与 80/443/8080/8443 端口；WebSocket 为默认，零部署变化。
- 凭证从 `src/main/resources/simbot-bots/QGuild.bot.json` 迁移到环境变量 `QQ_APP_ID` / `QQ_CLIENT_SECRET`（可选 `QQ_APP_TOKEN`）；缺失时应用正常启动、机器人不连接。
- `@Command` 方法必须是 `public`；`CommandRegistry` 在启动时校验重复模板、参数绑定与 `@RequireGm` 必须伴随 `@RequireAuth`，违规即启动失败。

## 相关决策

- ADR-0002 的拦截顺序（匹配 → 认证 → GM）在新调度器中原样保留，并成为固定顺序而非可调优先级。

## 日期

2026-10-01
