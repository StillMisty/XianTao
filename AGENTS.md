# AGENTS.md — XianTao (仙道)

A text-based idle Cultivation MUD with minimal interaction

## Commands

```bash
./gradlew build                     # full build (compile + test + spotless check)
./gradlew test                      # run all tests
./gradlew test --tests "*.SkillServiceTest"  # run single test class
./gradlew spotlessApply             # format all Java files (Google Java Format)
./gradlew installGitHooks           # install pre-commit hook (auto-runs spotlessApply on staged Java)
```

## Tech Stack

Java 25 · Spring Boot 4 · Spring AI 2 · MyBatis-Flex · PostgreSQL 18 (JSONB) · Flyway 12 · SimBot 4 · Lombok · Gradle (KTS)

## Architecture

```
src/main/java/top/stillmisty/xiantao/
├── config/                          # Spring beans, ChatClient config
├── handle/                          # I/O parse + text format, no business logic
│   ├── command/                     # VO → text formatters)
│   ├── onebotv11/                   # QQ listeners
│   └── web/                         # REST controllers (TBD)
├── domain/                          # Entities, enums, repository interfaces, VOs
│   ├── user/ item/ beast/ bounty/ fudi/ map/ monster/ pill/ skill/ command/
│   └── item/handler/                # ItemUseHandler strategy implementations (5 handlers)
├── service/                         # Auth + business logic
│   ├── ai/                          # SpiritChatService, SpiritTools, emotion engine
│   ├── annotation/Authenticated.java  # @Authenticated: marks public service methods for AOP auth
│   ├── aspect/AuthenticatedAspect.java # intercepts @Authenticated, binds userId to ScopedValue
│   ├── ServiceResult.java           # sealed: Success<T> | Failure<T>
│   ├── UserContext.java             # ScopedValue<Long> CURRENT_USER
│   └── ...Service.java             # domain services
└── infrastructure/                  # MyBatis-Flex mappers + repository impls
    ├── mapper/                      # Mapper interfaces extending BaseMapper<Entity>
    └── repository/                  # RepositoryImpl classes implementing domain repository interfaces
```

## Key Patterns

- **Dual-method Service**: Public `(PlatformType, String openId, ...)` + `@Authenticated` → `ServiceResult<T>` → delegates to internal `(Long userId, ...)` → raw VO. Internal methods are package-private or public for testing; they call `UserContext.getCurrentUserId()`.
- **Auth AOP**: `AuthenticatedAspect` intercepts any method annotated `@Authenticated`. First two args must be `(PlatformType, String openId)`. Calls `AuthenticationService.authenticate(platform, openId, requiredStatus)`, binds userId to `ScopedValue` via `UserContext.CURRENT_USER`. Returns `ServiceResult.Failure` on auth failure.
- **ServiceResult**: sealed `Success<T> | Failure<T>`. Command handlers pattern-match with `switch(result)` — no `instanceof` needed.
- **Item Use Strategy**: `ItemUseHandler` per `ItemType` (5 handlers: PillUseHandler, SkillJadeUseHandler, RecipeScrollUseHandler, EvolutionStoneUseHandler, ...). `consumesInternally()` controls whether `ItemUseService` auto-deducts quantity (default `false`).
- **AI**: Two `ChatClient` beans in `SpringAiConfig` — `spiritChatClient` (no token limit) and `chatClient` (maxTokens=150). Function calling via `SpiritTools`.
- **Concurrency**: Virtual threads (`spring.threads.virtual.enabled=true`) + `ScopedValue<Long> CURRENT_USER` (no ThreadLocal). ScopedValue is bound per-request in the AOP aspect.
- **SimBot listeners**: `@Listener` + `@ContentTrim` + `@Filter("command {{param}}")` on methods taking `MessageEvent` + `@FilterValue("param")`. Use `event.replyBlocking(text)` to respond.

## Development Flow

1. `domain/` — Entity, enum, VO, Repository interface
2. `db/migration/` — Flyway migration (`V1.0.XX__description.sql`), include DML + `COMMENT ON`
3. `infrastructure/` — Mapper interface + RepositoryImpl + type handlers if needed
4. `service/` — internal method (`Long userId`) + public method with `@Authenticated` returning `ServiceResult<T>`
5. `handle/command/` — VO → text formatter (implements `CommandGroup`)
6. `handle/onebotv11/` or `web/` — listener/controller trigger
