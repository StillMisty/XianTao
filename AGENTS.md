# AGENTS.md — XianTao (仙道)

QQ 群文字修仙 MUD（常驻机器人，非 Web 服务）。Java 27 · Spring Boot 4 · Spring AI 2 · MyBatis-Flex · PostgreSQL 18 · Flyway · 自研 `qq-gateway` 模块（QQ 开放平台 WebSocket/Webhook）。

## Commands

```bash
./gradlew build                      # compile + test + spotless check（提交前必过）
./gradlew test                       # run all tests
./gradlew test --tests "top.stillmisty.xiantao.XianTaoApplicationTests"   # single test class
./gradlew spotlessApply              # google-java-format Java + db/migration/*.sql
./gradlew installGitHooks            # clone 后执行一次：安装 pre-commit hook（自动格式化暂存的 .java）
python3 tools/e2e/e2e.py             # 本地端到端自测（假 QQ 平台 + 独立测试库，见 tools/e2e/README.md）
python3 tools/e2e/commands.py        # 全部 71 条指令可用性巡检
```

## Gotchas

- **NullAway = error**：对 `top.stillmisty.xiantao` 与 `top.stillmisty.qqgateway` 包启用 error 级空检查（JSpecify）。可空性不严谨会直接编译失败；可空参数/返回值必须标 `@Nullable`。
- **运行前提**：默认激活 `local` profile，需要本地 PostgreSQL（`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`，默认 localhost:5432/xiantao）和 `DEEPSEEK_API_KEY`。QQ 机器人凭证（`QQ_APP_ID`/`QQ_CLIENT_SECRET`）可选：缺失时应用照常启动，机器人不连接。
- **测试现状**：`qq-gateway` 模块（协议/签名/重连）与 `handle/dispatch`（模板语义/注册/调度）有单测，其余仍以 `build` 编译 + Spotless 为准。Test/JavaExec 已配置 `--enable-native-access=ALL-UNNAMED`。
- **不做主动推送**：QQ 平台主动消息能力已下线，产品决定**永不使用**——完成提醒/推送类需求不要设计主动消息路径（`qq-gateway` 的主动发送方法仅供协议完整性，游戏侧不调用）。
- **QQ 消息长度**：markdown 单条内容按 UTF-8 约 1800 字节分段（最多 4 段，超出截断标注），见 `QQPlatformHandler.splitMessage`。
- **Flyway 迁移**：`src/main/resources/db/migration/V1.0.x__描述.sql`

## Architecture

```
qq-gateway/                          # 独立子模块：QQ 开放平台接入（纯 Java + Jackson，零 Spring）
├── QqBotClient                      # Token 管理 + OpenAPI 调用 + 消息发送
├── QqWebSocketGateway               # WS 长连接：Identify/心跳/Resume/退避重连/事件去重
├── QqWebhookHandler + Verifier      # Webhook：Ed25519 验签、op 13 校验、op 12 回执
└── QqIncomingMessage / QqEventListener / QqMessageSender

src/main/java/top/stillmisty/xiantao/
├── config/                        # Spring beans、ChatClient 配置、QqBotConfiguration（xiantao.qq.*）
├── handle/                        # I/O 解析与文本格式化，不含业务逻辑
│   ├── command/                   # 命令处理器：VO → 文本（方法接收 TextFormat fmt 参数）
│   ├── dispatch/                  # CommandDispatcher（虚拟线程调度）+ CommandRegistry + @Command/@Arg/@CommandGroup
│   ├── listener/                  # @CommandGroup 监听器，每个命令域一个类
│   ├── interceptor/               # @RequireAuth / @RequireGm 标记注解（由 CommandDispatcher 解释）
│   ├── platform/                  # PlatformRegistry + PlatformHandler（QQ）+ QqWebhookController
│   └── ReplyHelper                # 集中 dispatch：执行命令 → 追加未投递通知 → 回复
├── domain/                        # 实体（含行为）、枚举、record VO；按领域分包 user/item/beast/combat/fudi/sect…
├── service/                       # 业务逻辑；ai/ 下为 Spring AI 聊天服务与 @Tool
│   ├── ServiceResult.java         # sealed: Success<T> | Failure<T>
│   ├── UserContext.java           # ScopedValue<Long> CURRENT_USER（每事件一条虚拟线程，无需跨线程传递）
│   ├── ErrorCode.java / BusinessException.java
│   └── inventory/handler/         # ItemUseHandler 策略（每个 ItemType 一个实现）
├── infrastructure/                # MyBatis-Flex mapper + repository
│   └── repository/BaseRepository  # save() 内部即 insertOrUpdateSelective()
└── util/                          # TextFormat 等工具
```

## Key Patterns

- 所有面向玩家的文本必须符合修仙世界观；术语以根目录 `CONTEXT.md` 为准（避免其 _Avoid_ 列出的同义词）。
- **Command Dispatch**: 监听器类标 `@CommandGroup`，方法标 `@Command("模板")`（模板语义与旧 SimBot `@Filter` 逐条对齐：`{{name}}`→`(?<name>.+)`、全匹配、字面量引用），参数用 `QqIncomingMessage` + `@Arg("名")`。`CommandDispatcher` 每条消息一条虚拟线程，顺序固定为「匹配 → 认证（@RequireAuth）→ GM（@RequireGm）」，认证后把 userId 绑定到 `ScopedValue`，Service 层用 `UserContext.requireCurrentUserId()` 取当前用户。同一玩家的命令经 `PerKeySerialExecutor` 按到达顺序串行执行（跨玩家并行），避免连点/双击竞态。
- **ServiceResult**: sealed `Success<T> | Failure<T>`，命令处理器用 `switch(result)` 模式匹配，不用 instanceof。
- **选择事件按钮**: 待选择事件（`EffectData.ChoiceOptions`）在回复时自动附带按钮（`QqKeyboard`，点击发送「选 X」），正文里的文本选项保留作兜底；平台限制 5×5，`xiantao.qq.choice-buttons=false` 可全局关闭。
- **Item Use Strategy**: 每个 ItemType 一个 handler（Pill/SkillJade/RecipeScroll/ForgingBlueprint/BeastEssence），按 `Map<ItemType, ItemUseHandler>` 分发；`consumesInternally()` 决定 `ItemUseService` 是否自动扣减数量（默认 false）。
- **Structured Errors**: Service 抛 `BusinessException(ErrorCode.X, args...)`，不要裸抛带 message 字符串的运行时异常。

## Conventions

- 枚举 code 用 `UPPER_SNAKE_CASE` + `@EnumValue`；`fromCode` 对未知值抛 `IllegalArgumentException`；DB CHECK 约束与枚举 code 完全一致（含大小写）。
- 持久化统一走 Repository 的 `save()`（内部 `insertOrUpdateSelective()`），不要在调用方写 insert/update 分支；Repository 不向外泄漏持久化细节。
- 并发扣减等原子操作用 `UPDATE ... SET x = x - ? WHERE x >= ?` 条件更新，不要 check-then-act。
- VO 一律 record；实体 `@Data` + 行为方法，不注入 repository、不依赖 infrastructure 层；Service 写方法加 `@Transactional`。
- 提交信息用中文 conventional 风格（`feat:` / `refactor(scope):` / `chore:` …）。

## Agent skills

### OpenSpec

规格与变更管理（`openspec/`）：`specs/` 为当前行为契约（如 `qq-messaging`、`game-features`），变更走 `changes/` → 校验 → 归档。CLI：`openspec list --specs`、`openspec validate <change>`、`openspec archive <change>`。

### Issue tracker

GitHub Issues (`StillMisty/XianTao`). See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default canonical labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout (`CONTEXT.md` + `docs/adr/` at repo root). See `docs/agents/domain.md`.
