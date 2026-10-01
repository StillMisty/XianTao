# AI 对话系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

所有 LLM 对话（地灵、商铺掌柜、宗灵、秘境之灵）共享统一的对话基础设施：单一历史表 `chat_history`、基于 Spring AI `ChatMemory` 的内存管理、按类型限制窗口大小的自动修剪，以及用户级频控与模型降级链。

### 设计原则

- **统一持久化**：所有对话类型共用 `chat_history` 表，通过 `chat_type` 区分
- **按类型限容**：不同对话类型独立配置消息窗口上限
- **自动修剪**：每次保存后自动删除超出窗口的旧条目，防止 DB 无限增长
- **Spring AI 集成**：使用 `ChatMemory` + `MessageChatMemoryAdvisor` 实现对话记忆的加载与保存
- **频控与降级**：入口统一限流，主模型失败时自动切换备用模型，全部失败时返回各服务降级文案

---

## 2. 数据表 (chat_history)

```sql
CREATE TABLE chat_history (
    id              BIGSERIAL PRIMARY KEY,
    chat_type       VARCHAR(16) NOT NULL,   -- SPIRIT / SHOP / SECT / TRAVELER / DUNGEON
    conversation_id BIGINT,                 -- 实体ID（福地ID / 商铺NPC ID / 宗门ID / 秘境实例）
    user_id         BIGINT NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    role            VARCHAR(16) NOT NULL,   -- user / assistant / system / tool
    content         TEXT NOT NULL,
    extra_data      JSONB DEFAULT '{}'::jsonb,   -- 扩展数据（如 DeepSeek reasoning_content）
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_chat_history_type CHECK (chat_type IN ('SPIRIT','SHOP','SECT','TRAVELER','DUNGEON')),
    CONSTRAINT chk_chat_history_role CHECK (role IN ('user','assistant','system','tool'))
);

CREATE INDEX idx_chat_history_lookup
    ON chat_history (chat_type, conversation_id, user_id, create_time DESC, id DESC);
```

> 实际表名为 `chat_history`（无 `xt_` 前缀）；`role` 为 VARCHAR(16)（原文档写 24）；新增 `extra_data` JSONB 列用于持久化 DeepSeek 推理内容（key = `reasoning_content`）。

---

## 3. ChatType 枚举（以代码为准）

| 枚举值 | 说明 | 窗口大小 | 使用场景 |
|--------|------|:------:|---------|
| `SPIRIT` | 地灵对话 | 25 | 福地精灵 |
| `SHOP` | 商铺对话 | 20 | 商铺掌柜 |
| `SECT` | 宗灵对话 | 10 | 宗门意志 |
| `TRAVELER` | 旅行商人 | 20 | 预留 |
| `DUNGEON` | 秘境之灵 | 25 | 秘境精灵（原文档未收录） |

> 原文档把 TRAVELER 标为「—（无窗口）」；实际 `PerTypeChatMemory.TRAVELER_MAX = 20`。

---

## 4. 核心组件

### 4.1 ConversationId

```java
public record ConversationId(ChatType chatType, Long userId, Long entityId) {
    public String value() {
        return chatType.getCode() + ":" + userId + ":" + entityId;
    }

    public static ConversationId from(String conversationId) {
        // 解析 "SPIRIT:123:456" → new ConversationId(SPIRIT, 123L, 456L)
    }
}
```

组合键 `(chatType, userId, entityId)`，序列化为 `"SPIRIT:123:456"` 格式。`from()` 解析时校验格式：段数不足或数字解析失败抛出 `BusinessException(ErrorCode.PARAM_INVALID)`。

### 4.2 PerTypeChatMemory

实现 `ChatMemory` 接口，内部为每种 `ChatType` 维护独立的 `MessageWindowChatMemory` 实例（`ConcurrentHashMap` 按类型名缓存，实例间完全隔离）：

- **SPIRIT**: 最多 25 条
- **SHOP**: 最多 20 条
- **SECT**: 最多 10 条
- **TRAVELER**: 最多 20 条
- **DUNGEON**: 最多 25 条

窗口超限时由 Spring AI 的 `MessageWindowChatMemory` 在内存中自动淘汰旧消息。

### 4.3 ChatMemoryRepositoryAdapter

实现 Spring AI 的 `ChatMemoryRepository` 接口（`@Primary`），桥接到 `ChatHistoryRepository`：

| 方法 | 行为 |
|------|------|
| `findByConversationId()` | 解析 `ConversationId`，按 `(chatType, entityId, userId)` 查询，按 `createTime ASC` 排序 |
| `saveAll()` | **Append-only**：统计 DB 已有条数，只插入内存窗口之外尚未落库的新消息（跳过被内存窗口淘汰的旧消息），随后在总条数超过窗口时调用 `deleteOldestEntries()` 修剪 |
| `deleteByConversationId()` | 删除指定会话的全部历史 |
| `findConversationIds()` | 返回空列表（未实现枚举会话） |

**自动修剪机制**：`deleteOldestEntries()` 使用 PostgreSQL CTE 实现——

```sql
WITH to_keep AS (
    SELECT id FROM chat_history
    WHERE chat_type = ? AND conversation_id = ? AND user_id = ?
    ORDER BY create_time DESC, id DESC
    LIMIT ?
)
DELETE FROM chat_history
WHERE chat_type = ? AND conversation_id = ? AND user_id = ?
  AND id NOT IN (SELECT id FROM to_keep);
```

保证每个会话在 DB 中最多保留 N 条记录，与内存窗口大小一致。

**DeepSeek reasoning 持久化**：`saveAll()` 从消息列表中提取 `DeepSeekAssistantMessage.reasoningContent`，写入 assistant 消息的 `extra_data.reasoning_content`；读取时还原为 `DeepSeekAssistantMessage`。

### 4.4 AbstractChatService

所有对话服务的基类，提供统一的 `callLlm()` 方法（以代码为准）：

```java
protected String callLlm(
    String systemPrompt,
    String userInput,
    ChatType chatType,
    Long userId,
    Long entityId,
    Object... tools) {
  String conversationId = new ConversationId(chatType, userId, entityId).value();
  ChatResponse chatResponse =
      chatClient.prompt()
          .system(systemPrompt)
          .user(userInput)
          .tools(tools)
          .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
          .call()
          .chatResponse();

  if (chatResponse == null || chatResponse.getResult() == null) return null;
  AssistantMessage output = chatResponse.getResult().getOutput();
  if (output == null) return null;
  String content = output.getText() != null ? output.getText() : "";
  return content.isEmpty() ? null : content;
}
```

`MessageChatMemoryAdvisor` 会自动：
1. 调用前从 `ChatMemory` 加载历史消息注入上下文
2. 调用后将用户输入 + LLM 回复保存到 `ChatMemory`，再由 `ChatMemoryRepositoryAdapter` 持久化

> 与文档不同：advisor 通过 ChatClient 的 `defaultAdvisors(toolCallingAdvisor, messageChatMemoryAdvisor)` 注册（见 `SpringAiConfig`），`callLlm()` 不再逐次构造 advisor；返回值统一做空响应处理，空内容返回 `null` 由上层降级。

### 4.5 ChatClient Bean 配置 (SpringAiConfig)

| Bean | maxTokens | 用途 |
|------|:--------:|------|
| `npcChatClient` (Primary) | 1000 | 宗灵创建时的宗门身份生成（诗号/道统/人格） |
| `spiritChatClient` | 1200 | 地灵对话 |
| `shopChatClient` | 800 | 商铺掌柜对话 |
| `sectChatClient` | 600 | 宗灵日常对话 |
| `dungeonChatClient` | 1500 | 秘境之灵对话（原文档未收录） |
| `chatClient` | 400 | 通用短文本生成（原文档写 150，实际为 400） |

所有 ChatClient 共用同一个 `ChatMemory` bean（`PerTypeChatMemory`）。不同 maxTokens 按对话场景的复杂度分配：秘境之灵/地灵信息量最大，宗灵最精简。

**模型降级链**：每个 ChatClient 通过 `FallbackChatModel` 包装多个 `ChatModel` 委托，按 `xiantao.ai.chat-models` 配置顺序依次尝试；非首个委托使用对应 tier 的备用模型名（`ChatOptionsAdapter` 适配 DeepSeek/OpenAI 的 options 类型）。全部失败时抛 `IllegalStateException`，由各对话服务降级为文案。

**AI 分级配置 (`AiTierConfig`, prefix `xiantao.ai`)**：`chat-models`（必填，缺失启动失败）、`tiers.heavy/standard/light`（各含 `deepseek`/`openai` 模型名）。地灵/秘境用 heavy，商铺/宗灵用 standard，通用/宗门身份用 light。

### 4.6 频控 (AiChatRateLimiter)

- 内存滑动窗口（Caffeine，`expireAfterAccess` 2 分钟），每用户每分钟最多 **10 次** AI 对话
- 超限抛 `BusinessException(ErrorCode.AI_RATE_LIMITED)` →「道友请稍安勿躁，每分钟最多 %d 次传音」
- 应用于地灵、掌柜、宗灵、秘境之灵四个对话入口，重启即重置

---

## 5. 对话类型实现

### 5.1 地灵 (SpiritChatService)

- 入口：`地灵 [自然语言]`
- 工具：`SpiritCellTools` + `SpiritBeastTools` + `SpiritInteractionTools`（共 12 个，含地块/灵兽/互动）
- Prompt：MBTI 人格 + 形态 + 好感度语气 + 福地状态 + 地块详情
- 详见 [地灵对话](./地灵对话.md)

### 5.2 商铺掌柜 (ShopChatService)

- 入口：`掌柜 [自然语言]`
- 工具：`ShopTools`（商品浏览、购买、出售、鉴定、砍价、调货）
- Prompt：掌柜人设 + 商品清单 + 价格规则 + 进行中的世界事件
- 详见 [交易系统设计](./交易系统设计.md)

### 5.3 宗灵 (SectSpiritChatService)

- 入口：`宗灵 [自然语言]`
- 工具：`SectMemberTools`（全员）+ `SectElderTools`（长老/宗主）+ `SectLeaderTools`（宗主），按调用者职位选择性注册
- Prompt：宗门状态（名称/道统/等级/资金/成员/宗主）+ 成员身份 + 公告 + 事件 + 权限指引
- 对话前惰性结算灵脉产出
- 详见 [宗门系统设计](./宗门系统设计.md)

### 5.4 秘境之灵 (DungeonChatService)（原文档未收录）

- 入口：`秘灵 [自然语言]`（需在秘境实例中）
- 工具：探索/移动/好感等秘境工具
- Prompt：秘境状态 + 探索进度

---

## 6. 对话流程

```
玩家: "地灵/掌柜/宗灵/秘灵 xxx"
  ↓
Service.chat(platform, openId, input)
  ├─ 0. AiChatRateLimiter 频控（超限直接返回限流提示）
  ├─ 1. 认证用户身份
  ├─ 2. 加载业务上下文（福地状态 / 商铺状态 / 宗门状态 / 秘境状态）
  ├─ 3. 组装系统 Prompt（人格 + 状态 + 规则 + 事件）
  ├─ 4. 调用 AbstractChatService.callLlm()
  │     ├─ 构造 ConversationId
  │     ├─ MessageChatMemoryAdvisor 自动加载历史
  │     ├─ LLM 调用（含 tools 参数；主模型失败自动降级备用模型）
  │     ├─ MessageChatMemoryAdvisor 自动保存新的用户+AI消息
  │     └─ ChatMemoryRepositoryAdapter 修剪 DB 超限条目
  └─ 5. 返回 LLM 回复（空回复/异常时返回该服务的降级文案）
```

---

## 7. 各对话服务降级文案（以代码为准）

| 服务 | LLM 空回复 / 异常时文案 |
|------|------------------------|
| 地灵 | `地灵暂时无法回应，请稍后再试。` |
| 宗灵 | `宗灵暂时无法回应，请稍后再试。` |
| 掌柜 | `掌柜暂时不在，请稍后再来。` |
| 秘境之灵 | `秘境之灵暂时无法回应，请稍后再试。`（空回复为 `秘境之灵暂时无法回应...`） |
| 宗门创建（身份生成失败） | LLM 失败回退默认身份，不阻断创建 |

`BusinessException`（业务失败，如频控/无福地）优先返回其格式化消息；其他异常返回上述降级文案并记录日志。

---

## 8. 关键技术决策（以代码为准）

- **统一表替代分散表**：`chat_history` 替代了原有的 `xt_spirit_history` 等独立历史表，所有对话类型共用一个表
- **窗口修剪在 DB 层**：`saveAll()` 之后立即执行 `deleteOldestEntries()`，防止 DB 无限增长，而非依赖定时任务或内存驱逐
- **Append-only 写入**：`saveAll()` 只补插未落库的新消息，避免每轮重复插入窗口内全部消息
- **按类型固定上限**：不再使用动态公式（如按劫数增长），简化为固定窗口大小，配置清晰可预测
- **Spring AI 内存管理**：依赖 `MessageWindowChatMemory` 管理内存窗口，`PerTypeChatMemory` 按类型分发，避免重复加载
- **ConversationId 格式校验**：使用统一 `BusinessException(ErrorCode.PARAM_INVALID)` 处理格式错误，符合项目错误处理规范
- **模型降级**：`FallbackChatModel` + `ChatOptionsAdapter` 支持 DeepSeek/OpenAI 多模型按序降级
- **用户级频控**：入口统一 `AiChatRateLimiter`，防止连发刷成本与上游限流

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名与结构**：实际为 `chat_history`（无 `xt_` 前缀）；`role` 为 VARCHAR(16)（非 24）；新增 `extra_data` JSONB（持久化 DeepSeek `reasoning_content`）；索引 `(chat_type, conversation_id, user_id, create_time DESC, id DESC)`；`chat_type` CHECK 含 `DUNGEON`。
- **ChatType 枚举**：新增 `DUNGEON`（窗口 25）；`TRAVELER` 实际窗口 20（文档标「—」作废）。
- **ChatClient maxTokens**：`chatClient` 实际 400（文档写 150）；新增 `dungeonChatClient` 1500；`npcChatClient` 用途扩展为宗门身份生成。
- **callLlm 实现**：使用 `ChatResponse` 并统一处理空响应/空内容（返回 `null`）；advisor 由 ChatClient 默认注册，非每次调用构造。
- **saveAll 语义**：实际为 append-only（按 DB 已有条数补插），文档「逐条保存新消息」不准确；修剪用 CTE `ORDER BY create_time DESC, id DESC`。
- **模型降级与频控**：文档未收录的 `FallbackChatModel`、`ChatOptionsAdapter`、`AiTierConfig`、`AiChatRateLimiter`（10 次/分钟）已补入正文。
- **对话类型与降级文案**：新增秘境之灵（DUNGEON）；各服务降级文案以 §7 为准。
- **ConversationId**：格式与校验与文档一致，无差异。

### B. 保留代码设计

无

### C. 按设计修正（待修）

无

### D. 未实现（待办）

无

### E. 缺陷修复

无
