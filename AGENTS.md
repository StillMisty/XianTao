# AGENTS.md — XianTao (仙道)

A text-based idle Cultivation MUD with minimal interaction

## Commands

```bash
./gradlew build                     # full build (compile + test + spotless check)
./gradlew test                      # run all tests
```

## Tech Stack

Java 25、SpringBoot 4、Spring AI 2、MyBatis-Flex、PostgreSQL 18、Flyway 12、SimBot 4、Gradle (KTS)

## Architecture

```
docs/
src/main/java/top/stillmisty/xiantao/
├── config/                          # Spring beans, ChatClient config
├── handle/                          # I/O parse + text format, no business logic
│   ├── command/                     # VO → text formatters (implements CommandGroup)
│   ├── listener/                    # SimBot @Listener (OneBotV11 + QQ per class)
│   ├── TextFormat.java              # sealed: PlainFormat | MarkdownFormat (heading/bold/listItem/separator)
├── domain/                          # Entities, enums, VOs
│   ├── user/ item/ beast/ bounty/ fudi/ map/ monster/ pill/ skill/ command/
│   └── item/handler/                # ItemUseHandler strategy implementations (5 handlers)
├── service/                         # Auth + business logic
│   ├── ai/                          # SpiritChatService, SpiritTools, emotion engine
│   ├── annotation/Authenticated.java  # @Authenticated: marks public service methods for AOP auth
│   ├── aspect/AuthenticatedAspect.java # intercepts @Authenticated, binds userId to ScopedValue
│   ├── ServiceResult.java           # sealed: Success<T> | Failure<T>
│   ├── UserContext.java             # ScopedValue<Long> CURRENT_USER
│   ├── ErrorCode.java               # enum of structured business error codes (68 constants)
│   ├── BusinessException.java       # RuntimeException carrying ErrorCode + format args
│   └── ...Service.java              # domain services
└── infrastructure/                  # MyBatis-Flex mappers + repositories
    ├── mapper/                      # Mapper interfaces extending BaseMapper<Entity>
    └── repository/                  # Repository classes using mappers for persistence
```

## Key Patterns

- **Dual-method Service**: Public `(PlatformType, String openId, ...)` + `@Authenticated` → `ServiceResult<T>` → delegates to internal `(Long userId, ...)` → raw VO. Internal methods are package-private or public for testing; they call `UserContext.getCurrentUserId()`.
- **Auth AOP**: `AuthenticatedAspect` intercepts any method annotated `@Authenticated`. First two args must be `(PlatformType, String openId)`. Calls `AuthenticationService.authenticate(platform, openId, requiredStatus)`, binds userId to `ScopedValue` via `UserContext.CURRENT_USER`. Returns `ServiceResult.Failure` on auth failure.
- **ServiceResult**: sealed `Success<T> | Failure<T>`. Command handlers pattern-match with `switch(result)` — no `instanceof` needed.
- **Item Use Strategy**: `ItemUseHandler` per `ItemType` (5 handlers: PillUseHandler, SkillJadeUseHandler, RecipeScrollUseHandler, EvolutionStoneUseHandler, ...). `consumesInternally()` controls whether `ItemUseService` auto-deducts quantity (default `false`).
- **Structured Errors**: Services throw `BusinessException(ErrorCode.X, args...)` instead of `IllegalStateException("message")`. `AuthenticatedAspect` catches `BusinessException` and extracts the formatted message.
- **Unified Formatting**: `TextFormat` sealed interface (PLAIN/MARKDOWN) parameterizes command handlers — one format method serves two platforms. Listeners call `handleXxx(platform, openId, ..., TextFormat.PLAIN)` or `TextFormat.MARKDOWN`.
- **Listener Platform Merge**: `handle/listener/` contains one class per command with both OneBotV11 and QQ `@Listener` methods. `ReplyHelper` centralizes platform-specific reply mechanics.

## Design Rules

- Enum code values must be `UPPER_SNAKE_CASE`; field annotated with `@EnumValue`  
- `fromCode` must throw `IllegalArgumentException` for unknown codes (never `null` or default)  
- DB `CHECK` constraints must match enum codes exactly, including case  
- Always use `mapper.insertOrUpdateSelective()` for saves — no separate `insert`/`update` branches  
- Repositories expose a single `save()`, never persistence details  
- Atomic operations: use `UPDATE ... WHERE column >= ?` instead of check-then-act patterns  
- All VOs must be Java `record` (immutable)  
- Entities: `@Data` + `@NoArgsConstructor`, **no** `staticConstructor`  
- Entities must contain behavioral methods, not be pure data carriers  
- Domain entities must **not** inject repositories or depend on infrastructure layers  
- Every write method must be annotated with `@Transactional`  
- Avoid copy-paste across services — extract shared logic into `@Component` helpers  

## Development Flow

1. `domain/` — Entity, enum, VO
2. `db/migration/` — Flyway migration (`V1.0.XX__description.sql`), include DML + `COMMENT ON`
3. `infrastructure/` — Mapper interface + Repository + type handlers if needed
4. `service/` — internal method (`Long userId`) + public method with `@Authenticated` returning `ServiceResult<T>`
5. `handle/command/` — VO → text formatter (implements `CommandGroup`)
6. `handle/listener/` — listener with both OneBotV11 + QQ platform methods, uses `ReplyHelper`

## Agent skills

### Issue tracker

GitHub Issues (`StillMisty/XianTao`). See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default canonical labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout (`CONTEXT.md` + `docs/adr/` at repo root). See `docs/agents/domain.md`.
