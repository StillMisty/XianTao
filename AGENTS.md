# AGENTS.md - Developer & AI Agent Guide for XianTao (仙道)

This document provides essential architectural rules, coding conventions, and context for both human developers and AI Agents (e.g., Cursor, Windsurf, Cline via MCP) working on the **XianTao** project. 

**⚠️ CRITICAL AI AGENT INSTRUCTION (ANTI-HALLUCINATION):** 
You MUST read and adhere to these guidelines. Because this project uses bleeding-edge technologies (Java 25, Spring Boot 4, Spring AI 2), your pre-trained knowledge is likely outdated. 
**BEFORE generating any code using these frameworks, you MUST use your MCP tools (e.g., context7, web search, doc readers) to query the official documentation.** Do NOT guess APIs.

## 1. Project Overview & Tech Stack

XianTao is a long-term AFK (Away From Keyboard) MUD game played via QQ groups. It features extreme low-frequency operations, high-frequency offline calculations, and deep AI NPC immersion (Tavern standard).

- **Java**: 25 (Strictly use Virtual Threads and modern Java features)
- **Framework**: Spring Boot 4.x
- **ORM**: MyBatis-Flex (Lightweight, chainable queries)
- **Database**: PostgreSQL 18 (Heavy reliance on `JSONB` for schemaless data)
- **Bot Engine**: SimBot (Simple Robot for QQ group interactions)
- **AI Integration**: Spring AI 2.x (Function Calling, Prompt Management)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

## 2. Mandatory MCP & Documentation Workflow (Strict Rule)

Whenever a user requests a feature involving the core tech stack, the AI Agent MUST follow this workflow:
1. **Spring Boot 4 & Spring AI 2**: DO NOT assume the API matches Spring Boot 3 or Spring AI 1.0. You MUST search the latest official Spring Docs (e.g., `ChatClient` builder patterns, Tool/Function Callback registrations).
2. **MyBatis-Flex**: DO NOT confuse it with MyBatis-Plus. You MUST query the official MyBatis-Flex documentation for `QueryChain`, `UpdateChain`, and `JacksonTypeHandler` usage.
3. **Java 25**: Verify the syntax for the latest Pattern Matching, Record capabilities, and Virtual Thread execution paradigms.
4. If you encounter an API error during compilation, **stop guessing**. Use your MCP tools to search the specific error or framework release notes immediately.

## 3. Architecture & Directory Structure (DDD + Ports & Adapters)

The codebase strictly follows a Domain-Driven Design (DDD) approach.
Base package: `top.stillmisty.xiantao`

```text
src/main/java/top/stillmisty/xiantao/
├── config/                  # Spring & framework configurations
├── handle/                  # SimBot message listeners (Entry points for QQ commands)
│   └── AllHandle.java       # e.g., parses "#status", "#afk" and routes to domain services
├── domain/                  # Core Business Logic (Divided by bounded contexts)
│   ├── user/                # User Context (Example)
│   │   ├── entity/          # DB Entities (e.g., User, UserAuth)
│   │   ├── enums/           # Domain Enums (e.g., UserStatus)
│   │   ├── repository/      # Repository Interfaces (Ports - DO NOT implement here)
│   │   ├── service/         # Business Logic & Usecases
│   │   └── vo/              # Value Objects / DTOs
│   ├── combat/              # (Planned) TTK Combat Engine & D20 events
│   ├── item/                # (Planned) Inventory, Crafting, Alchemy
│   └── land/                # (Planned) "Fudi" (Blessed Land) AI-driven management
└── infrastructure/          # Infrastructure Implementation (Adapters)
    ├── mapper/              # MyBatis-Flex BaseMapper interfaces
    └── repository/          # Implementation of domain repository interfaces
```

**Resource Directory:**
```text
src/main/resources/
├── application.yml          # Main configuration
├── db/migration/            # Flyway scripts (e.g., V0.0.1__create_user_system.sql)
└── simbot-bots/             # SimBot bot configurations (e.g., sanqing.bot.json)
```

## 4. Coding Conventions & Rules

### 4.1 Naming Conventions
- **Classes**: `PascalCase` (Note: Ensure files in `handle/` like `allHandle.java` are renamed to `AllHandle.java` or `GlobalCommandHandler.java` to follow Java standards).
- **Methods/Variables**: `camelCase`.
- **Database Tables/Columns**: `snake_case` (e.g., `xt_user`).
- **Flyway Migrations**: `V<Version>__<Description>.sql` (e.g., `V0.0.2__add_inventory_jsonb.sql`).

### 4.2 Concurrency & Java 25 Features
- **Virtual Threads**: Since SimBot handles concurrent chat messages, all service-level operations MUST be compatible with Virtual Threads. Avoid `ThreadLocal` unless it is virtual-thread-safe. Do not block platform threads.
- **Modern Syntax**: Use `Records` for DTOs/VOs, `sealed classes` for game events, and Pattern Matching (`switch` expressions) for command parsing in `handle/`.

### 4.3 Domain & Infrastructure Separation
- **Dependency Rule**: `domain` MUST NOT depend on `infrastructure`.
- **Repositories**: `domain/user/repository/UserRepository.java` is an interface. `infrastructure/repository/UserRepositoryImpl.java` implements it and injects `UserMapper`.

## 5. Game Domain Specifics (MUD Rules)

AI Agents generating game logic must respect the following mathematical models:
- **Combat (TTK Model)**: Combat is instant (Time-To-Kill). No turn-based loops. Calculate `Player TTK` vs `Enemy TTK` via `HP / Max(1, ATK - DEF)`.
- **AFK Calculations**: Do not run background cron jobs for every player. Calculate AFK rewards **lazily** when the player checks their `#status` or sends `#stop_afk`, based on `currentTime - afkStartTime`.

## 6. Database & PostgreSQL 18 Guidelines

- **JSONB is First-Class**: For flexible schemas like player inventory (`xt_inventory`), NPC Tavern cards (`xt_npc_card`), and extensible player stats, use PostgreSQL `JSONB` columns.
- **MyBatis-Flex Mapping**: Map `JSONB` columns using MyBatis-Flex's `JacksonTypeHandler`.
- **Indexing**: Always create `GIN` indexes in Flyway scripts when querying inside JSONB fields.

## 7. Build & Test Commands

Use the Gradle Wrapper (`./gradlew` or `gradlew.bat`) from the root directory.

- **Run locally**: `./gradlew bootRun`
- **Build & Test**: `./gradlew build`
- **Run specific test**: `./gradlew test --tests "top.stillmisty.xiantao.domain.user.service.UserServiceTest"`
- **Database Migrations**: Automatically handled by Flyway on startup.

## 8. AI Agent System Prompt Injection (Action Plan)

When you (the AI) are asked to implement a new feature:
1. Identify the bounded context (e.g., `combat`, `user`, `land`).
2. Use MCP/Search tools to read the latest docs for Spring Boot 4 / Spring AI 2 / MyBatis-Flex before writing the code.
3. Create the Entity and Repository Interface in `domain/`.
4. Create the Flyway migration script in `src/main/resources/db/migration/`.
5. Implement the Mapper and Repository in `infrastructure/`.
6. Implement the Business Logic in `domain/*/service/`.
7. Add the command trigger in `handle/`.
8. Write tests using JUnit 5 and Mockito.