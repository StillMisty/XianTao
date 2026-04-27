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
├── config/
├── docs/
├── handle/                  # Presentation Layer — I/O parsing + text formatting only
│   ├── command/             # View formatters (VO → plain text String)
│   ├── onebotv11/           # OneBotV11 Listeners (parse QQ input → call service → call formatter)
│   └── web/                 # Web REST Controllers (parse HTTP → call service → wrap as JSON)
├── domain/                  # Core Domain Logic (Bounded Contexts)
│   ├── user/                # User Context
│   │   ├── entity/          # DB Entities (e.g., User, UserAuth)
│   │   ├── enums/           # Domain Enums (e.g., UserStatus)
│   │   ├── repository/      # Repository Interfaces (Ports - DO NOT implement here)
│   │   └── vo/              # Value Objects / DTOs
│   ├── map/                 # Map Context
│   ├── item/                # Inventory (JSONB), Crafting, Alchemy
│   └── land/                # "Fudi" (Blessed Land) AI-driven management
├── service/                 # Application Service Layer (auth + orchestration + business logic)
│   ├── AuthenticationService.java  # Unified identity auth (platform + openId → userId)
│   ├── ServiceResult.java          # sealed Success<T> | Failure<T> — all public APIs return this
│   └── *Service.java               # Public (含认证) + internal (需预先完成认证) methods
└── infrastructure/          # Infrastructure Implementation (Adapters)
    ├── mapper/              # MyBatis-Flex BaseMapper interfaces
    └── repository/          # Implementation of domain repository interfaces
```

## 3. Core Architectural Rules

### 3.1 Cross-Platform Command Flow (`handle/`)
The `handle/` package handles all external inputs. A single `#afk` command might come from OneBotV11, Official QQ, or Web.
- **Rule**: Platform-specific packages (`onebotv11/`, `qq/`, `web/`) are ONLY responsible for parsing platform I/O and calling the **Service layer**.
- **Rule**: `handle/command/` classes are **pure View formatters** — they receive structured VO/DTO from Service layer and convert them to plain text. They contain **zero business logic, zero auth logic**.
- **Flow**: `Platform Listener` → `Service (auth + business → VO)` → `handle/command/ (format VO → text)` → `Platform Listener (render view)`.
- **Auth**: All identity verification is centralized in `service/AuthenticationService`. Service public methods accept `(PlatformType, String openId, ...)`, resolve identity internally, and return `ServiceResult<T>` (`Success<T>` or `Failure<T>`).

### 3.2 ServiceResult<T> — Unified Return Type
All service-layer public methods return `ServiceResult<T>`, a sealed interface with two variants:
- `Success<T>(T data)` — auth passed, business executed, carries domain VO
- `Failure<T>(String errorMessage)` — auth failed, carries user-readable message
- **Handle layer resolves via pattern matching** (`switch` exhaustiveness check), never by null-checking VO fields.

### 3.3 Service Layer Two-Tier API
Each `*Service.java` exposes two tiers of methods:
- **Public** (`(PlatformType, String openId, ...)`): Performs auth via `AuthenticationService`, then delegates to internal method. Returns `ServiceResult<T>`.
- **Internal** (`(Long userId, ...)`): Assumes pre-authenticated userId. Returns raw VO/DTO. Called by other services and LLM tools.

### 3.4 Domain & Infrastructure Separation
- **Dependency Rule**: `domain` MUST NOT depend on `infrastructure` or `handle`.
- **Repositories**: `domain/user/repository/UserRepository.java` is an interface. `infrastructure/repository/UserRepositoryImpl.java` implements it and injects MyBatis-Flex's Mapper.

### 3.5 Concurrency & Java 25 Features
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
5. **Logic**: Implement pure Business Logic in `service/` — internal methods taking `Long userId`.
6. **Auth**: Add public entry method `(PlatformType, String openId, ...)` in the same Service, calling `AuthenticationService` and returning `ServiceResult<T>`.
7. **Command**: Create `handle/command/XxxCommandHandler` — call Service public method, pattern-match `ServiceResult`, format VO to text. Zero auth logic.
8. **Entry/View**: Add the listener trigger in the appropriate platform package (`onebotv11/`, `qq/`, `web/`) and call the command handler.