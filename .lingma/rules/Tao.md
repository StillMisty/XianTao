# AGENTS.md - Developer & AI Agent Guide for XianTao (仙道)

This document provides essential architectural rules, coding conventions, and context for human developers and AI Agents (e.g., Cursor, Windsurf, Cline) working on the **XianTao** project.

**⚠️ CRITICAL AI INSTRUCTION:**
1. **MCP Context Activation**: You MUST actively utilize the **`context7`** MCP to read and understand the project's dynamic context.
2. **Anti-Hallucination (Strict)**: This project uses cutting-edge tech stacks (**Java 25, Spring Boot 4.x, Spring AI 2.x, MyBatis-Flex, simbot**). You MUST NOT rely solely on your pre-trained weights. Whenever you implement API calls, annotations, or core features for these frameworks, **YOU MUST SEARCH/CHECK OFFICIAL DOCUMENTATION via your web search tools or MCP** to ensure accurate syntax.

## 1. Project Overview & Tech Stack

XianTao is a cross-platform, text-based AFK (Away From Keyboard) MUD game. It features extremely low-frequency operations, lazy-evaluated offline calculations, and deep AI-driven NPC immersion.

- **Java**: 25 (Strictly use Virtual Threads, Records, Sealed Classes)
- **Framework**: Spring Boot 4.x
- **ORM**: MyBatis-Flex (Lightweight, chainable queries)
- **Database**: PostgreSQL 18 (Heavy reliance on `JSONB` for schemaless data)
- **Bot Engine**: SimBot (Cross-platform: OneBotV11, Official QQ, Web)
- **AI Integration**: Spring AI 2.x (Function Calling, Prompt Management)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

## 2. Architecture & Directory Structure (DDD + Ports & Adapters)

The codebase strictly follows Domain-Driven Design (DDD).
Base package: `top.stillmisty.xiantao`

```text
src/main/java/top/stillmisty/xiantao/
├── config/                  # Spring, MyBatis-Flex, and Spring AI configurations
├── handle/                  # Presentation Layer / Multi-platform Entry & View
│   ├── command/             # Centralized Command Implementations & Dispatchers (Platform-agnostic)
│   ├── enums/               # Command Enums (e.g., CommandType, ActionCode)
│   ├── onebotv11/           # OneBotV11 Listeners (Captures QQ input -> calls command/ -> returns view)
│   ├── qq/                  # Official QQ Listeners (Captures QQ input -> calls command/ -> returns view)
│   └── web/                 # Web REST APIs (Captures HTTP -> calls command/ -> returns JSON view)
├── domain/                  # Core Business Logic (Bounded Contexts)
│   ├── user/                # User Context 
│   │   ├── entity/          # DB Entities (e.g., User, UserAuth)
│   │   ├── enums/           # Domain Enums (e.g., UserStatus)
│   │   ├── repository/      # Repository Interfaces (Ports - DO NOT implement here)
│   │   ├── service/         # Business Logic (Strictly protocol-agnostic & command-agnostic)
│   │   └── vo/              # Value Objects / DTOs
│   ├── combat/              # TTK Combat Engine & D20 events
│   ├── item/                # Inventory (JSONB), Crafting, Alchemy
│   └── land/                # "Fudi" (Blessed Land) AI-driven management
└── infrastructure/          # Infrastructure Implementation (Adapters)
    ├── mapper/              # MyBatis-Flex BaseMapper interfaces
    └── repository/          # Implementation of domain repository interfaces
```

## 3. Core Architectural Rules

### 3.1 Cross-Platform Command Flow (`handle/`)
The `handle/` package handles all external inputs. A single `#afk` command might come from OneBotV11, Official QQ, or Web.
- **Rule**: Platform-specific packages (`onebotv11/`, `qq/`, `web/`) are ONLY responsible for parsing platform I/O and returning platform-specific Views (e.g., QQ message segments, HTTP JSON).
- **Flow**: `Platform Listener` -> `handle/command/ (Dispatcher)` -> `domain/*/service/` -> `handle/command/ (Format Text/Data)` -> `Platform Listener (Render View)`.
- **Command Implementations**: The actual orchestration of user intent must be centralized in `handle/command/`.

### 3.2 Domain & Infrastructure Separation
- **Dependency Rule**: `domain` MUST NOT depend on `infrastructure` or `handle`.
- **Repositories**: `domain/user/repository/UserRepository.java` is an interface. `infrastructure/repository/UserRepositoryImpl.java` implements it and injects MyBatis-Flex's Mapper.

### 3.3 Concurrency & Java 25 Features
- **Virtual Threads**: SimBot processes highly concurrent chat messages. All `handle/` listeners and `service/` methods MUST be compatible with Virtual Threads. Avoid `ThreadLocal` blocking operations.

## 4. Game Domain Specifics (MUD Rules)

AI Agents generating game logic must respect the mathematical models:
- **Combat (TTK Model)**: Combat is instant (Time-To-Kill). No turn-based loops. Calculate `Player TTK` vs `Enemy TTK` via `HP / Max(1, ATK - DEF)`.
- **AFK Calculations (Lazy Evaluation)**: Do not use cron jobs for offline rewards. Calculate AFK gains lazily when the player invokes `#status` or `#stop_afk`, based on `currentTime - afkStartTime`.

## 5. Database & PostgreSQL 18 Guidelines

- **JSONB is First-Class**: For extensible structures like player inventory (`xt_inventory`), NPC Tavern cards (`xt_npc_card`), and item traits, use PostgreSQL `JSONB` columns.
- **MyBatis-Flex Mapping**: Map `JSONB` columns using MyBatis-Flex's `JacksonTypeHandler`.
- **Indexing**: Always create `GIN` indexes in Flyway scripts when querying inside JSONB fields.

## 6. Spring AI 2 & LLM Integration

The "Earth Spirit" (地灵) and NPC Tavern interactions are powered by LLMs.
- **Function Calling**: Tools available to the AI must be defined as `java.util.function.Function` Spring `@Bean`s. Use `@Description` to explicitly explain the tool's purpose to the LLM (e.g., `executePlantSeed`).
- **Safety**: The AI ONLY outputs intents/JSON. State mutations (deducting items, adding stats) MUST be executed by the Java backend codebase, never directly by AI.

## 7. AI Agent Development Workflow (For Cursor/Windsurf/Cline)

When you (the AI) are asked to implement a new feature, follow this exact sequence:
1. **Search Docs**: Query official docs for Spring Boot 4 / Spring AI 2 / MyBatis-Flex / simbot if syntax is uncertain.
2. **Domain**: Create Entity, VO, and Repository Interface in `domain/`.
3. **Database**: Create the Flyway migration script in `src/main/resources/db/migration/`.
4. **Infra**: Implement Mapper and RepositoryImpl in `infrastructure/`.
5. **Logic**: Implement pure Business Logic in `domain/*/service/`.
6. **Command**: Implement the cross-platform command logic in `handle/command/`.
7. **Entry/View**: Add the listener trigger in the appropriate platform package (`onebotv11/`, `qq/`, `web/`) and call the command logic.