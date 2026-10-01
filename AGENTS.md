# AGENTS.md — XianTao (仙道)

QQ 群文字修仙 MUD（SimBot 驱动的常驻机器人，非 Web 服务）。Java 27 · Spring Boot 4 · Spring AI 2 · MyBatis-Flex · PostgreSQL 18 · Flyway · SimBot 5。

## Commands

```bash
./gradlew build                      # compile + test + spotless check（提交前必过）
./gradlew test                       # run all tests
./gradlew test --tests "top.stillmisty.xiantao.XianTaoApplicationTests"   # single test class
./gradlew spotlessApply              # google-java-format Java + db/migration/*.sql
./gradlew installGitHooks            # clone 后执行一次：安装 pre-commit hook（自动格式化暂存的 .java）
```

## Gotchas

- **NullAway = error**：对 `top.stillmisty.xiantao` 包启用 error 级空检查（JSpecify）。可空性不严谨会直接编译失败；可空参数/返回值必须标 `@Nullable`。
- **运行前提**：默认激活 `local` profile，需要本地 PostgreSQL（`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`，默认 localhost:5432/xiantao）和 `DEEPSEEK_API_KEY`。
- **测试现状**：仅占位 `contextLoads` 测试（JUnit，已禁用并行）；验证改动以 `build` 编译 + Spotless 为准。Test/JavaExec 已配置 `--enable-native-access=ALL-UNNAMED`。
- **Flyway 迁移**：`src/main/resources/db/migration/V1.0.x__描述.sql`

## Architecture

```
src/main/java/top/stillmisty/xiantao/
├── config/                        # Spring beans、ChatClient 配置
├── handle/                        # I/O 解析与文本格式化，不含业务逻辑
│   ├── command/                   # CommandGroup 实现：VO → 文本（方法接收 TextFormat fmt 参数）
│   ├── listener/                  # SimBot @Listener，每个命令域一个类
│   ├── interceptor/               # AuthInterceptorFactory（配合 @RequireAuth）
│   ├── platform/                  # PlatformRegistry + PlatformHandler 多平台回复
│   ├── ReplyHelper                # 集中 dispatch：解析事件 → 认证 → 执行命令 → 回复
│   └── TextFormat.java            # 文本格式接口（heading/bold/listItem…）；TextFormat.get() 当前返回 MarkdownFormat
├── domain/                        # 实体（含行为）、枚举、record VO；按领域分包 user/item/beast/combat/fudi/sect…
├── service/                       # 业务逻辑；ai/ 下为 Spring AI 聊天服务与 @Tool
│   ├── ServiceResult.java         # sealed: Success<T> | Failure<T>
│   ├── UserContext.java           # ScopedValue<Long> CURRENT_USER
│   ├── ErrorCode.java / BusinessException.java
│   └── inventory/handler/         # ItemUseHandler 策略（每个 ItemType 一个实现）
├── infrastructure/                # MyBatis-Flex mapper + repository
│   └── repository/BaseRepository  # save() 内部即 insertOrUpdateSelective()
└── util/
```

## Key Patterns

- 所有面向玩家的文本必须符合修仙世界观；术语以根目录 `CONTEXT.md` 为准（避免其 _Avoid_ 列出的同义词）。
- **Auth Interceptor**: listener 方法标注 `@RequireAuth` → `AuthInterceptorFactory` 从 `MessageEvent` 解析 `PlatformType` + `openId`，经 `AuthenticationService.authenticate()` 后把 userId 绑定到 `ScopedValue`。Service 层用 `UserContext.requireCurrentUserId()` 取当前用户。
- **ServiceResult**: sealed `Success<T> | Failure<T>`，命令处理器用 `switch(result)` 模式匹配，不用 instanceof。
- **Item Use Strategy**: 每个 ItemType 一个 handler（Pill/SkillJade/RecipeScroll/ForgingBlueprint/BeastEssence），按 `Map<ItemType, ItemUseHandler>` 分发；`consumesInternally()` 决定 `ItemUseService` 是否自动扣减数量（默认 false）。
- **Structured Errors**: Service 抛 `BusinessException(ErrorCode.X, args...)`，不要裸抛带 message 字符串的运行时异常。

## Conventions

- 枚举 code 用 `UPPER_SNAKE_CASE` + `@EnumValue`；`fromCode` 对未知值抛 `IllegalArgumentException`；DB CHECK 约束与枚举 code 完全一致（含大小写）。
- 持久化统一走 Repository 的 `save()`（内部 `insertOrUpdateSelective()`），不要在调用方写 insert/update 分支；Repository 不向外泄漏持久化细节。
- 并发扣减等原子操作用 `UPDATE ... SET x = x - ? WHERE x >= ?` 条件更新，不要 check-then-act。
- VO 一律 record；实体 `@Data` + 行为方法，不注入 repository、不依赖 infrastructure 层；Service 写方法加 `@Transactional`。
- 提交信息用中文 conventional 风格（`feat:` / `refactor(scope):` / `chore:` …）。

## Agent skills

### Issue tracker

GitHub Issues (`StillMisty/XianTao`). See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default canonical labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout (`CONTEXT.md` + `docs/adr/` at repo root). See `docs/agents/domain.md`.
