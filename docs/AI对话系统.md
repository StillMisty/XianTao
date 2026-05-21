# AI 对话系统

## 1. 概述

所有 LLM 对话（地灵、商铺掌柜、宗灵、旅行商人）共享统一的对话基础设施：单一历史表 `xt_chat_history`、基于 Spring AI `ChatMemory` 的内存管理、按类型限制窗口大小的自动修剪。

### 设计原则

- **统一持久化**：所有对话类型共用 `xt_chat_history` 表，通过 `chat_type` 区分
- **按类型限容**：不同对话类型独立配置消息窗口上限
- **自动修剪**：每次保存后自动删除超出窗口的旧条目，防止 DB 无限增长
- **Spring AI 集成**：使用 `ChatMemory` + `MessageChatMemoryAdvisor` 实现对话记忆的加载与保存

---

## 2. 数据表 (xt_chat_history)

```sql
CREATE TABLE xt_chat_history (
    id              BIGSERIAL PRIMARY KEY,
    chat_type       VARCHAR(16) NOT NULL,   -- SPIRIT / SHOP / SECT / TRAVELER
    conversation_id BIGINT NOT NULL,        -- 实体ID（福地ID / 商铺ID / 宗门ID）
    user_id         BIGINT NOT NULL,
    role            VARCHAR(16) NOT NULL,   -- USER / ASSISTANT / SYSTEM / TOOL
    content         TEXT NOT NULL,
    extra_data      JSONB,
    create_time     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_history_composite
    ON xt_chat_history (chat_type, conversation_id, user_id, create_time DESC);
```

---

## 3. ChatType 枚举

| 枚举值 | 说明 | 窗口大小 | 使用场景 |
|--------|------|:------:|---------|
| `SPIRIT` | 地灵对话 | 25 | 福地精灵 |
| `SHOP` | 商铺对话 | 20 | 商铺掌柜 |
| `SECT` | 宗灵对话 | 10 | 宗门意志 |
| `TRAVELER` | 旅行商人 | — | 预留 |

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

组合键 `(chatType, userId, entityId)`，序列化为 `"SPIRIT:123:456"` 格式。`from()` 解析时校验格式，格式不合法抛出 `BusinessException(ErrorCode.PARAM_INVALID)`。

### 4.2 PerTypeChatMemory

实现 `ChatMemory` 接口，内部为每种 `ChatType` 维护独立的 `MessageWindowChatMemory` 实例：

- **SPIRIT**: 最多 25 条
- **SHOP**: 最多 20 条
- **SECT**: 最多 10 条

每个类型仅创建一个 `MessageWindowChatMemory` 实例（`ConcurrentHashMap` 缓存），实例间完全隔离。窗口超限时由 Spring AI 的 `MessageWindowChatMemory` 在内存中自动淘汰旧消息。

### 4.3 ChatMemoryRepositoryAdapter

实现 Spring AI 的 `ChatMemoryRepository` 接口，桥接到 `ChatHistoryRepository`：

| 方法 | 行为 |
|------|------|
| `findByConversationId()` | 解析 `ConversationId`，按 `(chatType, entityId, userId)` 查询，按 `createTime ASC` 排序 |
| `saveAll()` | 逐条保存新消息，然后调用 `deleteOldestEntries()` 修剪超出窗口的旧条目 |
| `deleteByConversationId()` | 删除指定会话的全部历史 |

**自动修剪机制**：`saveAll()` 保存后执行 `DELETE WHERE id NOT IN (SELECT ... ORDER BY create_time DESC LIMIT N)`，保证每个会话在 DB 中最多保留 N 条记录，与内存窗口大小一致。修剪逻辑使用 PostgreSQL CTE 实现。

### 4.4 AbstractChatService

所有对话服务的基类，提供统一的 `callLlm()` 方法：

```java
protected String callLlm(
    String systemPrompt,
    String userInput,
    ChatType chatType,
    Long userId,
    Long entityId,
    Object... tools
) {
    String conversationId = new ConversationId(chatType, userId, entityId).value();
    return chatClient
        .prompt()
        .system(systemPrompt)
        .user(userInput)
        .tools(tools)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build()))
        .call()
        .content();
}
```

`MessageChatMemoryAdvisor` 会自动：
1. 调用前从 `ChatMemory` 加载历史消息注入上下文
2. 调用后将用户输入 + LLM 回复保存到 `ChatMemory`，再由 `ChatMemoryRepositoryAdapter` 持久化

### 4.5 ChatClient Bean 配置 (SpringAiConfig)

| Bean | maxTokens | 用途 |
|------|:--------:|------|
| `npcChatClient` (Primary) | 1000 | 宗灵创建时的宗门命名生成 |
| `spiritChatClient` | 1200 | 地灵对话 |
| `shopChatClient` | 800 | 商铺掌柜对话 |
| `sectChatClient` | 600 | 宗灵日常对话 |
| `chatClient` | 150 | 通用美化（短文本生成） |

所有 ChatClient 共用同一个 `ChatMemory` bean（`PerTypeChatMemory`）。不同 maxTokens 按对话场景的复杂度分配：地灵信息量最大（福地状态 + 地块 + 工具），宗灵最精简。

---

## 5. 对话类型实现

### 5.1 地灵 (SpiritChatService)

- 入口：`地灵 [自然语言]`
- 工具：`SpiritTools`（8个福地操作工具）+ `SpiritEmotionTools`（2个情绪工具）
- Prompt：MBTI 人格 + 形态 + 情绪 + 福地状态 + 地块详情 + 事件
- 详见 [地灵对话](./地灵对话.md)

### 5.2 商铺掌柜 (ShopChatService)

- 入口：`掌柜 [自然语言]`
- 工具：`ShopTools`（商品浏览、购买、出售、鉴定、砍价、调货）
- Prompt：掌柜人设 + 商品清单 + 价格规则
- 详见 [交易系统设计](./交易系统设计.md)

### 5.3 宗灵 (SectSpiritChatService)

- 入口：`宗灵 [自然语言]`
- 工具：`SectSpiritTools`（宗门管理操作，按权限注册）
- Prompt：宗门状态 + 成员身份 + 权限信息
- 详见 [宗门系统设计](./宗门系统设计.md)

---

## 6. 对话流程

```
玩家: "地灵/掌柜/宗灵 xxx"
  ↓
Service.chat(platform, openId, input)
  ├─ 1. 认证用户身份
  ├─ 2. 加载业务上下文（福地状态 / 商铺状态 / 宗门状态）
  ├─ 3. 组装系统 Prompt（人格 + 状态 + 规则 + 事件）
  ├─ 4. 调用 AbstractChatService.callLlm()
  │     ├─ 构造 ConversationId
  │     ├─ MessageChatMemoryAdvisor 自动加载历史
  │     ├─ LLM 调用（含 tools 参数）
  │     ├─ MessageChatMemoryAdvisor 自动保存新的用户+AI消息
  │     └─ ChatMemoryRepositoryAdapter 修剪 DB 超限条目
  └─ 5. 返回 LLM 回复
```

---

## 7. 关键技术决策

- **统一表替代分散表**：`xt_chat_history` 替代了原有的 `xt_spirit_history` 等独立历史表，所有对话类型共用一个表
- **窗口修剪在 DB 层**：`saveAll()` 之后立即执行 `deleteOldestEntries()`，防止 DB 无限增长，而非依赖定时任务或内存驱逐
- **按类型固定上限**：不再使用动态公式（如按劫数增长），简化为固定窗口大小，配置清晰可预测
- **Spring AI 内存管理**：依赖 `MessageWindowChatMemory` 管理内存窗口，`PerTypeChatMemory` 按类型分发，避免重复加载
- **ConversationId 格式校验**：使用统一 `BusinessException(ErrorCode.PARAM_INVALID)` 处理格式错误，符合项目错误处理规范
