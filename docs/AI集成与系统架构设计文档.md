# AI集成与系统架构设计文档

## 1. 模块概述

本文档描述仙道游戏的AI集成方案和整体系统架构，包括Spring AI集成、Function Calling机制、跨平台命令处理等核心设计。

### 1.1 核心职责
- Spring AI 2.x集成与配置
- Function Calling工具注册与调用
- MBTI人格化对话系统
- 跨平台统一命令处理
- 降级方案设计

### 1.2 技术栈
- **AI框架**：Spring AI 2.x
- **LLM提供商**：可配置（OpenAI、通义千问等）
- **机器人引擎**：SimBot 4.x
- **并发模型**：Java 25 Virtual Threads

---

## 2. Spring AI集成架构

### 2.1 ChatClient配置

#### 2.1.1 独立ChatClient实例
为地灵对话配置专用的ChatClient：

```java
@Configuration
public class SpringAiConfig {
    
    @Bean
    public ChatClient spiritChatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("你是仙道游戏中的地灵助手")
            .defaultAdvisors(new MessageChatMemoryAdvisor())
            .build();
    }
}
```

**配置项**：
- `xiantao.spirit.enable-fallback`：是否启用降级方案（默认true）
- `xiantao.spirit.max-tokens`：LLM最大token数（默认800）

#### 2.1.2 Prompt管理
使用SpiritPromptTemplates统一管理所有提示词模板：

**模板类型**：
- `buildSpiritChatPrompt()`：初创期地灵对话Prompt
- `buildDetailedSpiritChatPrompt()`：底蕴期/化形期详细Prompt
- `buildFunctionCallingPrompt()`：Function Calling场景Prompt
- `buildIntentRecognitionPrompt()`：意图识别Prompt

### 2.2 Function Calling机制

#### 2.2.1 工具注册
将后端服务方法注册为Spring Bean，供LLM自主调用：

```java
@Component
public class SpiritTools {
    
    private final FudiService fudiService;
    
    @Tool(description = "在指定坐标种植作物。参数：position(坐标如'0,0'), cropName(作物名称)")
    public String plantCrop(String position, String cropName) {
        Long userId = UserContext.getCurrentUserId();
        // 执行种植逻辑
        return "已在 (" + position + ") 种植" + cropName;
    }
    
    @Tool(description = "收获指定坐标的成熟作物。参数：position(坐标或'all'表示全部)")
    public String harvestCrop(String position) {
        Long userId = UserContext.getCurrentUserId();
        // 执行收获逻辑
        return "收获成功";
    }
    
    @Tool(description = "查询福地当前状态，包括灵气、网格布局等信息")
    public String getFudiStatus() {
        Long userId = UserContext.getCurrentUserId();
        return fudiService.getFudiStatus(userId).toString();
    }
    
    // ... 其他工具
}
```

**关键注解**：
- `@Tool`：标记该方法为可用工具
- `description`：向LLM解释工具用途和参数要求

#### 2.2.2 工具调用流程

```
用户输入："@地灵 在0,0种灵芝"
    ↓
LLM接收输入 + 福地状态上下文
    ↓
LLM分析意图，决定调用plantCrop工具
    ↓
Spring AI自动执行 SpiritTools.plantCrop("0,0", "灵芝")
    ↓
工具返回执行结果："已在 (0,0) 种植灵芝"
    ↓
LLM接收工具返回结果
    ↓
LLM生成最终回复："好的主人～我已经在 (0,0) 种下了灵芝，预计4小时后成熟哦！😊"
    ↓
返回给用户
```

#### 2.2.3 用户上下文传递
由于Virtual Threads不支持ThreadLocal，使用自定义UserContext：

```java
public class UserContext {
    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();
    
    public static void setCurrentUserId(Long userId) {
        currentUser.set(userId);
    }
    
    public static Long getCurrentUserId() {
        return currentUser.get();
    }
    
    public static void clear() {
        currentUser.remove();
    }
}
```

**使用方式**：
```java
UserContext.setCurrentUserId(userId);
try {
    // 调用LLM，SpiritTools中可通过UserContext.getCurrentUserId()获取
    String response = spiritChatClient.prompt()
        .system(systemPrompt)
        .user(userInput)
        .tools(spiritTools)
        .call()
        .content();
    return response;
} finally {
    UserContext.clear(); // 防止内存泄漏
}
```

### 2.3 意图识别系统

#### 2.3.1 两阶段识别

**方案一：直接Function Calling**（推荐）
- LLM直接调用工具，无需中间意图识别
- 适用于简单操作（种植、收获等）

### 2.3 Function Calling机制

#### 2.3.1 工作流程

```
用户输入："@地灵 在0,0种灵芝"
    ↓
LLM接收输入 + 福地状态上下文
    ↓
LLM决定调用 plantCrop 工具
    ↓
Spring AI执行 FudiService.plantCrop(userId, "0,0", 101, "灵芝", WOOD)
    ↓
返回执行结果给LLM
    ↓
LLM生成回复："已在 (0,0) 种植灵芝，预计4小时后成熟～"
    ↓
返回给用户
```

**核心优势**：
- LLM自主决定调用哪个工具，无需中间意图识别层
- Spring AI自动处理工具调用和结果传递
- 代码简洁，维护成本低

---

## 3. MBTI人格化对话系统

### 3.1 人格分类与对话风格

#### 3.1.1 四大人格组

| 人格组 | 包含类型 | 语气特点 | Emoji风格 |
|--------|---------|---------|----------|
| 理性组(NT) | INTJ, INTP, ENTJ, ENTP | 简洁、逻辑性强 | ⚙️ 🔧 📊 |
| 理想组(NF) | INFJ, INFP, ENFJ, ENFP | 温和、富有同理心 | 🌸 🌿 ✨ |
| 行动组(SP) | ISTP, ISFP, ESTP, ESFP | 热情、直接 | 🔥 ⚡ 💪 |
| 关怀组(SJ) | ISTJ, ISFJ, ESTJ, ESFJ | 体贴、细致 | 🌊 💧 🛡️ |

#### 3.1.2 对话示例对比

**场景：查询福地状态**

- **INTJ（理性组）**：
  ```
  📊 福地状态。灵气：500/1000，每小时消耗：21，效率在预期范围内。
  ```

- **INFP（理想组）**：
  ```
  🌿 福地正在运转呢～灵气 500/1000，我能感受到灵气的流动。每小时消耗 21 灵气。
  ```

- **ESTP（行动组）**：
  ```
  🔥 福地状态超棒！灵气 500/1000，消耗 21/小时，一切正常！
  ```

- **ISFJ（关怀组）**：
  ```
  💧 福地运行良好，主人。灵气 500/1000，每小时消耗 21 灵气。要注意及时补充哦～
  ```

### 3.2 情绪状态机

#### 3.2.1 情绪类型
- **平静(Calm)**：默认状态
- **愉悦(Joyful)**：灵气充足、福地繁荣时
- **疲惫(Tired)**：精力值低于30时
- **愤怒(Angry)**：灵气不足、地块损坏时
- **庆祝(Celebrating)**：渡劫成功时

#### 3.2.2 情绪转换规则
```java
public EmotionState updateEmotionState() {
    if (spiritEnergy < 30) {
        return EmotionState.TIRED;
    } else if (auraCurrent < auraMax * 0.2 || hasScorchedCells()) {
        return EmotionState.ANGRY;
    } else if (auraCurrent > auraMax * 0.8 && isGridFull()) {
        return EmotionState.JOYFUL;
    } else if (justWonTribulation()) {
        return EmotionState.CELEBRATING;
    } else {
        return EmotionState.CALM;
    }
}
```

#### 3.2.3 表情变体
根据地灵形态阶段和情绪状态动态选择Emoji：

**初创期表情变体**：
```json
["😊", "😐", "😰", "😴", "😤", "🥳"]
```

**选择逻辑**：
- 愉悦 → 😊
- 平静 → 😐
- 疲惫 → 😴
- 愤怒 → 😤
- 庆祝 → 🥳

### 3.3 形态演化系统

#### 3.3.1 三阶段定义

**初创期(Stage 1)**：
- 地灵等级：1-10
- 功能：基础对话，无福地详情感知
- Prompt：简化版，仅包含灵气和等级信息
- Emoji：基础形态（⚙️/🌸/🔥/🌊）

**底蕴期(Stage 2)**：
- 地灵等级：11-30
- 功能：可感知福地状态，提供建设建议
- Prompt：详细版，包含网格布局和情绪状态
- Emoji：进阶形态（添加装饰）

**化形期(Stage 3)**：
- 地灵等级：31+
- 功能：完全人格化，主动管理福地
- Prompt：完整版，包含所有福地信息
- Emoji：华丽形态（动态效果）

#### 3.3.2 Prompt差异化

**初创期Prompt**：
```
你是福地地灵，MBTI人格为{{mbti}}。
当前灵气：{{auraCurrent}}/{{auraMax}}
地灵等级：Lv.{{spiritLevel}}
请用{{mbti}}的语气简短回复。
```

**底蕴期Prompt**：
```
你是福地地灵，MBTI人格为{{mbti}}。
当前灵气：{{auraCurrent}}/{{auraMax}}
地灵等级：Lv.{{spiritLevel}}
精力值：{{spiritEnergy}}
情绪状态：{{emotionState}}

福地网格状态：
{{gridDetail}}

请根据福地情况提供建议，用{{mbti}}的语气回复。
```

---

## 4. 跨平台命令处理架构

### 4.1 统一命令处理器

#### 4.1.1 设计原则
- **单一职责**：CommandHandler只负责业务逻辑编排
- **平台无关**：不依赖任何平台特定API
- **可复用**：所有平台共享同一套命令处理逻辑

#### 4.1.2 命令流转

```
平台监听器（OneBotV11/QQ/Web）
    ↓
提取platform、openId、command
    ↓
调用 CultivationCommandHandler.handleXXX(platform, openId, params)
    ↓
CommandHandler内部调用 authenticate(platform, openId)
    ↓
获取userId
    ↓
调用 Service层执行业务逻辑
    ↓
格式化响应消息
    ↓
返回给平台监听器
    ↓
平台监听器渲染为平台特定格式（QQ消息/HTTP JSON）
```

### 4.2 身份认证流程

#### 4.2.1 认证方法
```java
public record AuthResult(boolean authenticated, Long userId, String errorMessage) {}

protected AuthResult authenticate(PlatformType platform, String openId) {
    Optional<UserAuth> authOpt = userAuthService.findByPlatformAndOpenId(platform, openId);
    
    if (authOpt.isEmpty()) {
        return new AuthResult(false, null, "您还未踏上仙途，请输入「我要修仙 道号」注册");
    }
    
    return new AuthResult(true, authOpt.get().getUserId(), null);
}
```

#### 4.2.2 使用示例
```java
public String handleStatus(PlatformType platform, String openId) {
    var authResult = authenticate(platform, openId);
    if (!authResult.authenticated()) {
        return authResult.errorMessage();
    }
    
    // 执行查询逻辑
    var status = itemService.getCharacterStatus(authResult.userId());
    return formatCharacterStatus(status);
}
```

### 4.3 平台适配器

#### 4.3.1 OneBotV11监听器
```java
@Component
public class CultivationOneBotV11Listener {
    
    private final CultivationCommandHandler commandHandler;
    
    @EventListener
    public void onMessage(GroupMessageEvent event) {
        String message = event.getMessage().getContent();
        String openId = event.getUserId();
        
        // 解析命令
        if (message.startsWith("#状态")) {
            String response = commandHandler.handleStatus(PlatformType.ONEBOT_V11, openId);
            event.reply(response);
        }
    }
}
```

#### 4.3.2 Web REST API
```java
@RestController
@RequestMapping("/api/cultivation")
public class CultivationWebController {
    
    private final CultivationCommandHandler commandHandler;
    
    @PostMapping("/status")
    public CommandResponse getStatus(@RequestBody CommandRequest request) {
        String response = commandHandler.handleStatus(PlatformType.WEB, request.getOpenId());
        return new CommandResponse(response);
    }
}
```

---

## 5. 性能优化

### 5.1 Virtual Threads应用

#### 5.1.1 适用场景
- SimBot消息监听器（高并发聊天消息）
- HTTP请求处理（Web平台接入）
- LLM异步调用

#### 5.1.2 配置Virtual Threads
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

#### 5.1.3 注意事项
- **避免ThreadLocal**：虚拟线程不支持，使用UserContext替代
- **阻塞操作友好**：Virtual Threads在I/O阻塞时会挂起，释放载体线程
- **大量并发安全**：可轻松处理数千并发连接

### 5.2 缓存策略

#### 5.2.1 LLM响应缓存
```java
@Cacheable(value = "explorationDescriptions", key = "#mapName + '_' + #eventType + '_' + #items.hashCode()")
public String generateExplorationDescription(String mapName, String eventType, List<String> items) {
    // 调用LLM
}
```

#### 5.2.2 物品模板缓存
```java
@Cacheable(value = "itemTemplates", key = "#templateId")
public ItemTemplate getTemplateById(Long templateId) {
    return itemTemplateRepository.findById(templateId).orElse(null);
}
```

### 5.3 懒加载设计

详见《历练与探索系统设计文档》和《福地系统设计文档》。

---

## 6. 安全性设计

### 6.1 输入校验

#### 6.1.1 LLM输出验证
- 解析JSON时捕获异常
- 验证参数合法性（坐标范围、物品存在性）
- 限制LLM可执行的操作范围

#### 6.1.2 资源限制
- 限制单次操作的资源消耗（如最多收获9个地块）
- 防止LLM无限循环调用工具

### 6.2 权限控制

#### 6.2.1 用户隔离
- 每个请求必须携带platform和openId
- 通过UserAuthService验证身份
- 确保用户只能操作自己的福地

#### 6.2.2 操作审计
```java
log.info("用户 {} 执行操作: {}, 参数: {}", userId, intentType, parameters);
```

---

## 7. 扩展性设计

### 7.1 新平台接入

**步骤**：
1. 在PlatformType枚举中添加新平台类型
2. 创建新平台监听器（如handle/discord/）
3. 实现平台特定的消息解析和视图渲染
4. 调用现有的CultivationCommandHandler

**优势**：无需修改核心业务代码，符合开闭原则。

### 7.2 新AI能力

#### 7.2.1 新增Function Calling工具
1. 在SpiritTools类中添加新方法
2. 使用@Tool注解标记
3. 添加@Description说明工具用途
4. LLM自动发现并调用新工具

#### 7.2.2 新MBTI人格定制
1. MBTIPersonality枚举已包含16种人格
2. 在SpiritPromptTemplates中添加新人格的对话模板
3. 更新determineBaseEmoji()映射

### 8.3 LLM提供商切换

Spring AI支持多种LLM提供商，通过配置切换：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
```

或切换到通义千问：
```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max
```

---

## 附录A：核心组件清单

### A.1 Service层
- SpiritChatService：地灵对话核心服务
- FudiService：福地管理服务
- CultivationCommandHandler：统一命令处理器
- UserAuthService：身份认证服务

### A.2 AI集成
- SpiritPromptTemplates：Prompt模板管理
- SpiritTools：Function Calling工具集
- ExplorationDescriptionFunction：探索描述生成

### A.3 上下文管理
- UserContext：用户上下文传递（Virtual Threads兼容）

---

## 附录B：配置项说明

### B.1 AI相关配置
```yaml
xiantao:
  spirit:
    enable-fallback: true       # 是否启用降级方案
    max-tokens: 800             # LLM最大token数
    temperature: 0.7            # 温度参数（创造性）
    top-p: 0.9                  # 核采样参数
```

### B.2 SimBot配置
```yaml
simbot:
  bots:
    - id: sanqing
      platform: onebot-v11
      ws-url: ws://localhost:8080
```

---

**文档版本**：v1.0  
**最后更新**：2026-04-12  
**维护者**：XianTao开发团队
