# 地灵 MBTI 人格化对话系统 - 使用指南

## 📋 功能概述

已成功接入 LLM API，实现地灵的 MBTI 人格化对话和意图识别功能。主要特性：

✅ **16 种 MBTI 人格差异化对话** - 每种人格有独特的对话风格和语气  
✅ **自然语言意图识别** - 解析玩家指令并自动执行操作  
✅ **单次会话无记忆** - 降低 Token 消耗  
✅ **降级方案** - LLM 不可用时回退到规则匹配  
✅ **形态阶段适配** - 根据地灵阶段提供不同详细程度的上下文  

---

## 🎯 使用方式

### 1. 自然语言交互（推荐）

玩家发送：
```
地灵 帮我把中间那颗药收了
```

系统流程：
1. 解析意图 → `HARVEST` (收获)，坐标 `1,1`
2. 执行收获操作
3. 根据地灵 MBTI 人格生成个性化回复

**示例回复**：
- **INTJ**: "根据分析，中央灵田已成熟。执行收获...获得灵芝×3，产量符合预期。"
- **ESFP**: "好哒好哒！我已经把中间的灵药收好啦～哇！获得了3份灵芝！超棒的！✨"
- **INFJ**: "我能感受到灵田的成熟气息...已经为你收获了，希望这些灵芝能帮到你 🌙"

### 2. 纯对话模式

玩家发送：
```
地灵 今天福地怎么样？
```

地灵会根据当前福地状态和 MBTI 人格回复，不执行任何操作。

---

## 📝 支持的自然语言指令

### 种植相关
- `"地灵 在左上角种灵芝"`
- `"地灵 帮我在 0,0 种植人参"`
- `"地灵 想种点火莲"`

### 收获相关
- `"地灵 把中间的药收了"`
- `"地灵 收获 1,1"`
- `"地灵 全部收获"` / `"地灵 收获所有"`

### 建造相关
- `"地灵 在 2,0 建造灵田"`
- `"地灵 建个兽栏在右下角"`
- `"地灵 在这里放个阵眼 1,2"`

### 拆除相关
- `"地灵 拆除 0,1 的地块"`
- `"地灵 把左上角那个拆了"`

### 献祭相关
- `"地灵 献祭铁剑"`
- `"地灵 把所有白色装备都转化了"`
- `"地灵 献祭 all"`

### 喂养相关
- `"地灵 喂养 2,0 的灵兽"`
- `"地灵 给兽栏里的灵兽喂点吃的"`

### 查询相关
- `"地灵 福地状态"`
- `"地灵 灵气还有多少"`
- `"地灵 你叫什么名字"`

### 普通聊天
- `"地灵 你好呀"`
- `"地灵 今天辛苦了"`
- `"地灵 你觉得这个布局怎么样"`

---

## ⚙️ 配置说明

### application.yml 关键配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-placeholder}  # LLM API 密钥
      base-url: ${OPENAI_BASE_URL:https://api.deepseek.com}  # API 地址
      chat:
        options:
          model: ${OPENAI_MODEL:deepseek-chat}  # 模型名称
          temperature: 0.7  # 创造性 (0.0-1.0)
          max-tokens: 1000  # 最大 Token 数

xiantao:
  spirit:
    max-tokens: 800  # 地灵单次会话最大 Token
    enable-fallback: true  # 降级开关（LLM 不可用时回退规则匹配）
```

### 环境变量

使用时需设置：
```bash
export OPENAI_API_KEY="your-api-key-here"
export OPENAI_BASE_URL="https://api.deepseek.com"  # 或其他 OpenAI 兼容 API
export OPENAI_MODEL="deepseek-chat"
```

---

## 🔧 技术实现

### 核心组件

| 文件 | 说明 |
|------|------|
| `config/SpringAiConfig.java` | Spring AI ChatClient 配置 |
| `config/SpiritPromptTemplates.java` | MBTI 人格化 Prompt 模板 |
| `service/SpiritChatService.java` | 地灵对话核心服务 |
| `service/SpiritTools.java` | Function Calling 工具函数 |
| `domain/land/vo/SpiritIntentVO.java` | 意图识别结果 VO |
| `handle/command/FudiCommandHandler.java` | 命令处理器（新增自然语言处理） |
| `handle/onebotv11/FudiHandle.java` | OneBotV11 监听器（更新） |

### LLM 调用流程

```
玩家消息
  ↓
FudiHandle.handleFudiSpirit()
  ↓
FudiCommandHandler.handleSpiritChat()
  ↓
SpiritChatService.processSpiritInteraction()
  ├─ 1. parseIntent() - 意图识别
  │    └─ 调用 LLM（系统 Prompt：意图识别模板）
  │    └─ 解析 JSON 返回 → SpiritIntentVO
  │
  ├─ 2. executeIntent() - 执行操作
  │    └─ 根据意图类型调用 FudiService
  │    └─ 返回操作结果
  │
  └─ 3. chatWithSpirit() - 人格化回复
       └─ 调用 LLM（系统 Prompt：MBTI 人格模板 + 福地状态）
       └─ 返回个性化回复
  ↓
回复玩家
```

### 降级方案

当 LLM API 不可用时（网络错误、配额耗尽等），自动启用规则匹配：

1. **意图识别降级**：
   - 关键词匹配（种植、收获、建造等）
   - 正则表达式提取坐标
   - 方位词转换（中间→1,1、左上→0,0 等）

2. **对话回复降级**：
   - 根据 MBTI 人格生成模板回复
   - 针对不同意图提供差异化回复

---

## 🎭 MBTI 人格对话示例

### INTJ（战略家）🧠
```
玩家：地灵 福地状态如何？
INTJ：福地运转效率在预期范围内。灵气 800/1000，每小时消耗 95。
     根据计算，当前资源利用率达到 80%，建议优化灵田布局以提升产出。
```

### ESFP（表演者）🎭
```
玩家：地灵 今天怎么样？
ESFP：哇！今天超开心的！灵气满满呢～ 800/1000！
     虽然每小时要消耗 95 灵气，但我感觉精力充沛！有什么要我帮忙的吗？✨
```

### INFJ（倡导者）🌙
```
玩家：地灵 帮我种点药
INFJ：我能感受到你对灵药的期待呢～ 想种什么呢？
     灵芝、人参还是火莲？告诉我位置和作物名称，我会用心帮你种植的 🌸
```

---

## 🚀 后续优化建议

### 短期优化
1. **用户上下文传递** - 当前 `SpiritTools` 中 `getCurrentUserId()` 为占位实现，需改为从 ThreadLocal 或 SecurityContext 获取
2. **作物/物品数据库** - 完善作物、物品、饲料的查询逻辑
3. **批量献祭** - 实现 `#献祭 all` 功能
4. **Prompt 优化** - 根据实际使用数据优化 Prompt 模板

### 中期优化
1. **对话历史** - 可选启用短期记忆（最近 3-5 条对话）
2. **情感演化** - 根据地灵好感度调整对话风格
3. **主动建议** - 地灵主动提示福地优化建议
4. **多模型支持** - 支持切换不同 LLM 提供商

### 长期优化
1. **地灵立绘** - 阶段三化形后接入图像生成
2. **语音合成** - 不同 MBTI 人格的语音风格
3. **羁绊系统** - 玩家长期互动解锁专属对话
4. **地灵成长** - MBTI 人格微调（非重塑）

---

## 🐛 故障排查

### LLM 调用失败
- 检查 `OPENAI_API_KEY` 是否正确
- 检查 `OPENAI_BASE_URL` 是否可达
- 查看日志中的错误信息
- 确认 `enable-fallback: true` 已启用降级

### 意图识别不准确
- 提高 LLM `temperature` 参数（增加创造性）
- 优化 Prompt 模板中的示例
- 检查玩家输入是否清晰

### 编译错误
- 确认已添加 `jackson-databind` 依赖
- 确认所有枚举类导入正确（`CellType`, `WuxingType` 等）
- 运行 `gradlew clean build` 清理缓存

---

## 📞 技术支持

如有问题，请查看：
- 日志文件：`logs/xiantao.log`
- 地灵交互日志：搜索关键字 `"地灵对话"` 或 `"意图识别"`
- LLM 调用日志：搜索关键字 `"SpiritChatService"`

---

**开发完成日期**: 2026-04-12  
**开发优先级**: 高（文档需求第 3 项）  
**测试状态**: 编译通过，待集成测试
