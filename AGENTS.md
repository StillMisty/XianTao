# AGENTS.md — XianTao (仙道)

## Tech Stack
Java 25 · Spring Boot 4.x · Spring AI 2.x · MyBatis-Flex · PostgreSQL 18 (JSONB) · SimBot

## Architecture

```
src/main/java/top/stillmisty/xiantao/
├── config/
├── handle/          # I/O parse + text format, no business logic
│   ├── command/     # VO → text formatters
│   ├── onebotv11/   # QQ listeners
│   └── web/         # REST controllers
├── domain/          # Entities, enums, repository interfaces, VOs
│   ├── user/ bounty/ item/ land/ map/
├── service/         # Auth + business logic
│   ├── AuthenticationService.java
│   └── ServiceResult.java    # sealed: Success<T> | Failure<T>
└── infrastructure/  # MyBatis-Flex mappers, repository impls
```

## Service API Pattern
- **Public** method: `(PlatformType, String openId, ...)` → auth → returns `ServiceResult<T>`
- **Internal** method: `(Long userId, ...)` → returns raw VO/DTO

## Development Flow
1. domain/ — Entity, VO, Repository interface
2. db/migration/ — Flyway migration
3. infrastructure/ — Mapper + RepositoryImpl
4. service/ — internal method (`Long userId`) + public method (`ServiceResult<T>`)
5. handle/command/ — VO → text formatter
6. handle/onebotv11/ or web/ — listener/controller trigger
