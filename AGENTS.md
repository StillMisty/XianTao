# AGENTS.md — XianTao (仙道)

## Tech Stack
Java 25 · Spring Boot 4.x · Spring AI 2.x · MyBatis-Flex · PostgreSQL 18 (JSONB) · SimBot · Gradle + KTS

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
│   └── item/handler/ # ItemUseHandler strategy implementations
├── service/         # Auth + business logic
│   ├── AuthenticationService.java
│   ├── ServiceResult.java    # sealed: Success<T> | Failure<T>
│   └── ItemUseService.java   # Item use dispatcher
└── infrastructure/  # MyBatis-Flex mappers, repository impls
```

## Service API Pattern
- **Public** method: `(PlatformType, String openId, ...)` → auth → returns `ServiceResult<T>`
- **Internal** method: `(Long userId, ...)` → returns raw VO/DTO

## Item Use System (Strategy Pattern)

统一使用指令：`使用 [物品名] [参数]`

```
domain/item/handler/
├── ItemUseHandler.java           # Interface: supports(), use()
├── PillUseHandler.java           # POTION → 服用丹药
├── SkillJadeUseHandler.java      # SKILL_JADE → 学习法决
├── RecipeScrollUseHandler.java   # MATERIAL+recipe → 学习丹方
└── EvolutionStoneUseHandler.java # EVOLUTION_STONE → 灵兽进化
```

扩展方式：创建新的 `XxxUseHandler implements ItemUseHandler` + `@Component` 即可自动注册。

## Development Flow
1. domain/ — Entity, VO, Repository interface
2. db/migration/ — Flyway migration
3. infrastructure/ — Mapper + RepositoryImpl
4. service/ — internal method (`Long userId`) + public method (`ServiceResult<T>`)
5. handle/command/ — VO → text formatter
6. handle/onebotv11/ or web/ — listener/controller trigger
