# AGENTS.md — XianTao (仙道)

A text-based idle Cultivation MUD with minimal interaction

## Tech Stack

Java 25 · Spring Boot 4 · Spring AI 2 · MyBatis-Flex · PostgreSQL 18 (JSONB) · Flyway 12 · SimBot 4 · Lombok · Gradle + KTS

## Architecture

```
src/main/java/top/stillmisty/xiantao/
├── config/                          # Spring beans, ChatClient config
├── handle/                          # I/O parse + text format, no business logic
│   ├── command/                     # VO → text formatters (7 handlers)
│   ├── onebotv11/                   # QQ listeners (@Listener + @Filter)
│   └── web/                         # REST controllers (TBD)
├── domain/                          # Entities, enums, repository interfaces, VOs
│   ├── user/ item/ beast/ bounty/ fudi/ map/ monster/ pill/ skill/
│   │   └── item/handler/            # ItemUseHandler strategy implementations
├── service/                         # Auth + business logic
│   ├── ai/                          # SpiritChatService, SpiritTools, emotion engine
│   ├── annotation/                  # @ConsumeSpiritEnergy
│   ├── aspect/                      # AuthenticatedAspect, SpiritEnergyAspect
│   ├── ServiceResult.java           # sealed: Success<T> | Failure<T>
│   ├── UserContext.java             # ScopedValue<Long> CURRENT_USER
│   └── ...Service.java             # 15+ domain services
└── infrastructure/                  # MyBatis-Flex mappers + repository impls
    ├── mapper/                      # 21 Mapper interfaces
    ├── repository/                  # 21 RepositoryImpl classes
```

## Key Patterns

- **Dual-method Service**: Public `(PlatformType, String openId, ...)` → AOP auth → `ServiceResult<T>`; Internal `(Long userId, ...)` → raw VO
- **Auth AOP**: `AuthenticatedAspect` auto-authenticates via `AuthenticationService`, binds userId to `ScopedValue` via `UserContext.CURRENT_USER`
- **ServiceResult**: sealed `Success<T> | Failure<T>` — used with pattern matching in command handlers
- **Item Use Strategy**: `ItemUseHandler` per `ItemType`; `consumesInternally()` controls consumption
- **AI**: Two `ChatClient` beans (spirit/general), function calling via `SpiritTools`, emotion engine
- **Concurrency**: Virtual threads + `ScopedValue<Long> CURRENT_USER` (no ThreadLocal)

## Development Flow

1. `domain/` — Entity, enum, VO, Repository interface
2. `db/migration/` — Flyway migration (DML + COMMENT ON)
3. `infrastructure/` — Mapper + RepositoryImpl + type handlers
4. `service/` — internal method (`Long userId`) + public method (`ServiceResult<T>`)
5. `handle/command/` — VO → text formatter
6. `handle/onebotv11/` or `web/` — listener/controller trigger
