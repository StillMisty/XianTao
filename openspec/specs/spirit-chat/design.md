# 地灵对话系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 系统概述

地灵是福地的人格化 AI 精灵，通过 Function Calling 机制实现自然语言驱动的福地管理。**所有福地操作统一使用 Function Calling 模式**，LLM 自主判断是纯聊天还是调用工具。

### 核心流程（以代码为准）

```
玩家: "地灵 你好"
  ↓
SpiritChatService.chatWithSpirit()
  ├─ 1. AiChatRateLimiter 频控（每玩家每分钟最多 10 次，超限抛 AI_RATE_LIMITED）
  ├─ 2. 校验福地与地灵存在（FUDI_NOT_FOUND / SPIRIT_NOT_FOUND）
  ├─ 3. 更新福地上线时间（fudi.touchOnlineTime）
  ├─ 4. 组装系统 Prompt（MBTI 人格 + 形态 + 好感度语气 + 劫数 + 地块状态）
  ├─ 5. 调用 AbstractChatService.callLlm()（统一 LLM 入口）
  │     ├─ MessageChatMemoryAdvisor 自动加载历史（25 条窗口）
  │     ├─ LLM 调用（含 tools 参数）
  │     │     └─ LLM 自主判断 → 调用地块/灵兽/互动工具
  │     │     └─ 或纯人格化回复
  │     └─ MessageChatMemoryAdvisor 自动保存用户+AI消息，ChatMemoryRepositoryAdapter 修剪 DB 超限条目
  └─ 6. 返回回复（LLM 无输出时返回「地灵暂时无法回应，请稍后再试。」）
```

> 福地与地灵在玩家注册时由 `UserService.register()` 自动创建（`FudiService.createFudi()`）：MBTI 随机分配、形态随机抽取，均不可更改。**没有独立的福地创建指令。**

### 核心概念

- **好感度 (affection)**：地灵对玩家的情感值，连续数值，范围 `[0, affectionMax]`（默认上限 1000）。
- **形态 (form)**：地灵的外观形象，注册时从 `spirit_form` 表随机抽取，不可更改。
- **MBTI 人格**：地灵的性格类型，**注册时随机分配**（原文档「创建时玩家选择」已过时），不可更改。
- **情绪状态 (emotionState)**：**当前代码未实现**（无 `EmotionState` 枚举、无 `updateEmotion` 工具、Prompt 中不注入情绪状态）。见 §3。

---

## 2. 技术组件（以代码为准）

| 组件 | 职责 |
|------|------|
| `SpiritChatService` | 唯一对话入口，继承 `AbstractChatService`，组装 Prompt 并注入工具 |
| `SpiritPromptTemplates` | 系统 Prompt 模板（MBTI 人格 + 形态 + 好感度语气 + 福地状态 + 地块 + 规则） |
| `SpiritCellTools` | 地块操作工具：查询/建造/拆除/升级/种植/收取/查背包（7 个） |
| `SpiritBeastTools` | 灵兽管理工具：出战召回/进化/放生/孵化、繁育（2 个） |
| `SpiritInteractionTools` | 互动工具：冒犯降好感、接受礼物、触发天劫（3 个） |
| `FudiStateBuilder` | 将地块/灵兽状态序列化为 LLM 可读文本 |
| `SpiritChatContext` | ScopedValue 单次对话上下文（预加载福地与地灵，避免工具重复查询） |
| `AbstractChatService` | 基类——统一 LLM 调用入口、`ConversationId` 构造 |
| `PerTypeChatMemory` | 按对话类型限制窗口大小（地灵 25 条） |
| `ChatMemoryRepositoryAdapter` | 对话历史持久化适配器，自动修剪 DB 超限条目 |
| `AiChatRateLimiter` | 用户级 AI 对话频控（每分钟 10 次） |
| `SpringAiConfig` | ChatClient Bean 配置（`spiritChatClient`, maxTokens=1200） |
| `Spirit` / `SpiritForm` (Entity) | 地灵实例 / 形态定义表实体 |
| `FudiGiftService` | 送礼判定与好感变化 |
| `TribulationService` | 天劫战斗与胜负结算（含怜悯） |
| `BeastEvolutionService` / `BeastBreedingService` | 消耗好感度的机制效果（升阶成功率、孵化品质下限） |

> 对话历史统一基础设施（`ChatType`、`ConversationId`、`chat_history` 表、`MessageWindowChatMemory`）详见 [AI对话系统](./AI对话系统.md)。

---

## 3. AI 驱动的情绪系统（设计保留，当前代码未实现）

> **代码现状**：`domain/fudi/enums/` 下只有 `BeastQuality`、`CellType`、`MBTIPersonality`，没有 `EmotionState`；`Spirit` 实体没有情绪字段；工具集中没有 `updateEmotion`；`SpiritPromptTemplates` 明确写入「你的情绪表达通过对话语气自然体现，没有固定的情绪状态标签」。以下内容为历史设计参考。

### 3.1 设计理念

情绪由 LLM **自主判断**，体现地灵的"个性"和"自主性"，而非简单的数值映射。LLM 通过 `updateEmotion` 工具随时更新情绪状态。

### 3.2 情绪状态枚举 (EmotionState) — 设计参考

9 种情绪状态（原设计定义于枚举），分为自动切换 + LLM 手动设置：

| 状态 | Code | 说明 |
|------|------|------|
| AFFECTIONATE | affectionate | 依恋（好感 ≥ 800） |
| JOYFUL | joyful | 愉悦（好感 ≥ 500） |
| CONTENT | content | 满足（好感 ≥ 200） |
| NEUTRAL | neutral | 平和（好感 ≥ 50，默认） |
| DISTANT | distant | 疏离（好感 < 50） |
| WORRIED | worried | 忧虑（福地荒芜时） |
| EXCITED | excited | 兴奋（天劫胜利时自动切换） |
| ANGRY | angry | 愤怒（天劫失败时自动切换） |
| EXHAUSTED | exhausted | 虚弱（地灵挡劫时自动切换） |

### 3.3 内部自动状态机（设计参考）

`Spirit.updateEmotionState()` 根据好感度自动切换：

```
好感 ≥ 800 → AFFECTIONATE
好感 ≥ 500 → JOYFUL
好感 ≥ 200 → CONTENT
好感 ≥ 50  → NEUTRAL
否则       → DISTANT
```

### 3.4 LLM 情绪控制工具（设计参考）

- `updateEmotion(emotionState)` → 将地灵情绪状态更新为指定值（9 种可选），并持久化到 `spirit` 表。

### 3.5 情绪对 LLM 回复的影响（设计参考）

情绪状态注入到 Prompt 中，LLM 需根据当前情绪调整语气。**当前代码改为**：好感度分档语气 + MBTI 语气风格，由 LLM 自行体现情绪。

---

## 4. 福地事件系统（设计保留，当前代码未实现）

> **代码现状**：不存在 `FudiEventGenerator`、`fudi_event_template` 表或任何地灵事件生成/注入逻辑；`SpiritChatService` 不生成事件、不执行事件机制效果。以下内容为历史设计参考。

### 4.1 设计理念

地灵有自己的"生活"，即使玩家不来对话，福地也会发生各种事件。部分事件会对玩家产生**机制效果**（修为百分比、灵石、回血），通过世界事件系统的 `SubEventEffectExecutor` 管线统一执行，结果通过 `GameEvent` 通知队列送达。

### 4.2 事件生成机制（设计参考）

**DB 驱动模板池：** 对话时检查时间间隔，计算事件数量，从 `fudi_event_template` 表中随机抽取模板，注入 Prompt 并执行机制效果。

`FudiEventGenerator` 提供 `generateEvents(lastEventTime)` 方法：检查距上次事件的小时数是否超过最小间隔（4 小时），若未超过则返回空列表；否则从模板池随机去重抽取并计算事件数量。`SpiritChatService` 在事件生成后调用 `applyFudiEventEffects()` 对有机制效果的事件执行 `SubEventEffectExecutor`，并创建 `GameEvent`（类别 `WORLD_EVENT`）进入通知队列。

### 4.3 事件模板池 (`fudi_event_template`)（设计参考）

| 名称 | 描述 | 机制效果 | 选取权重 |
|------|------|----------|----------|
| 下雨了 | 细雨绵绵，灵气充沛 | 修为 +5% | 100 |
| 刮风了 | 微风拂过，带来远方的气息 | — | 100 |
| 迷路的灵兽 | 一只迷路的小灵兽闯入了福地 | — | 90 |
| 灵蝶飞舞 | 一群发光的灵蝶在福地中翩翩起舞 | 灵石 +3 | 100 |
| 神秘访客 | 一位神秘的修士路过此地 | 随机常见材料 ×1 | 80 |
| 灵草枯萎 | 一株灵草不知为何突然枯萎了 | — | 90 |
| 灵兽诞生 | 福地中诞生了一只新的小灵兽 | 修为 +8% | 80 |
| 灵气恢复 | 福地的灵气自然恢复了一些 | 回血 +30 | 100 |
| 回忆旧主 | 地灵想起了曾经的主人 | — | 80 |
| 初遇回忆 | 地灵回忆起与你的初次相遇 | — | 80 |
| 史莱姆恶作剧 | 一只小史莱姆偷偷溜进了福地 | — | 70 |
| 物品失踪 | 你发现一件小物品不见了 | — | 70 |

**生成参数（设计参考）：**
- 最少间隔：4 小时
- 每次最多生成：6 个
- 首次对话生成 1-2 个

### 4.4 事件注入 Prompt（设计参考）

事件描述在 `SpiritChatService.buildPrompt()` 中构建，追加到系统 Prompt 末尾：

```
【最近发生的事件】
- {事件名}：{事件描述}
```

---

## 5. 对话历史系统

地灵对话历史使用统一的对话基础设施（`chat_history` 表，`ChatType.SPIRIT`）。每条消息按 `(SPIRIT, fudiId, userId)` 存储，窗口上限 25 条。

历史注入由 Spring AI 的 `MessageChatMemoryAdvisor` 自动完成——调用前加载最近 25 条消息到上下文，调用后自动保存新的用户+AI 消息并修剪超出窗口的旧条目。

> 详见 [AI对话系统](./AI对话系统.md) 第 3-4 节。

---

## 6. 可用工具（12 个，以代码为准）

### 6.1 地块操作工具 (SpiritCellTools)

| 工具 | 说明 |
|------|------|
| `checkFudiCells` | 查询地块布局状态（总地块、已占、空闲编号） |
| `checkPlayerBag` | 按类别查询背包（种子/装备/兽卵/材料等） |
| `plantCrop` | 在指定编号地块种植作物（名称或种子编号） |
| `buildCell` | 建造地块（灵田/兽栏） |
| `removeCell` | 拆除地块建筑（不可拆已种植/已孵化地块） |
| `upgradeCell` | 消耗灵石升级地块（最高五阶） |
| `collectProduce` | 统一收取灵田收获 + 兽栏产出（支持 `all`） |

### 6.2 灵兽管理工具 (SpiritBeastTools)

| 工具 | 说明 |
|------|------|
| `manageBeast` | 出战/召回（DEPLOY）、进化（EVOLVE）、放生（RELEASE）、孵化（HATCH） |
| `breedBeasts` | 两只成年异性灵兽繁育（需一阴一阳、不在恢复/冷却） |

### 6.3 互动工具 (SpiritInteractionTools)

| 工具 | 说明 |
|------|------|
| `acceptGift` | 接受主人赠送的礼物（LLM 判断何时调用） |
| `feelOffended` | 表达对主人冒犯言行的不满，severity 1-5 降低好感 |
| `triggerTribulation` | 为主人触发天劫渡劫考验 |

> 原文档所列 `getCellStatus` / `queryBag` / `giveGift` / `reportPlayerOffense` / `updateEmotion` 等工具名已不存在；`updateEmotion` 无对应实现。

---

## 7. 好感度系统

### 7.1 上限

好感度上限 = **1000**（`spirit.affection_max` 字段，DB 默认 1000，CHECK > 0），由 `Spirit.addAffection(amount)` 统一处理，限制在 `[0, affectionMax]`，不可超过。

### 7.2 获取途径（以代码为准）

| 途径 | 变化量 | 说明 |
|------|--------|------|
| 天劫胜利 | +5 | 每次渡劫成功固定增加（怜悯模式下不加） |
| 赠送地灵所爱礼物 | +10~50 | 物品 tags 命中 `liked_tags`（`10 + rand(41)`） |
| 赠送中性礼物 | +1~3 | tags 既不命中喜欢也不命中讨厌（`1 + rand(3)`；原文档写 +1~2） |
| 赠送地灵讨厌的礼物 | -5~-20 | 物品 tags 命中 `disliked_tags`（`-(5 + rand(16))`） |
| 天劫失败 | -clearCount | 按被毁地块数扣除 |
| 冒犯言论 | -1~-5 | LLM 通过 `feelOffended` 上报，severity 截断到 1-5 |
| 福地事件 | — | **未实现**（见 §4） |

### 7.3 作用（以代码为准）

| 作用 | 公式 | 实现位置 |
|------|------|----------|
| 灵兽升阶成功率加成 | `min(15, affection / 7)` | `BeastEvolutionService`：成功率 = `85 + 加成` |
| 灵兽孵化品质下限 | 每 100 好感提升一档品质下限（凡品→灵品→仙品→圣品→神品） | `BeastBreedingService.rollBeastQuality()`：`floor = affection / 100`，逐档把低品质权重并入高品质 |
| LLM 对话语气 | Prompt 注入好感度分层描述（§11.2） | `SpiritPromptTemplates` |
| 渡劫怜悯 | `affection ≥ 800` 且队伍有存活单位时触发 | `TribulationService` |

---

## 8. 地灵形态系统

### 8.1 数据模型

形态数据存于 `spirit_form` 表（代码与迁移中的实际表名，无 `xt_` 前缀）：

```sql
CREATE TABLE spirit_form (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    description   TEXT NOT NULL,
    liked_tags    JSONB NOT NULL DEFAULT '[]'::jsonb,
    disliked_tags JSONB NOT NULL DEFAULT '[]'::jsonb
);
-- liked_tags / disliked_tags 均有 GIN 索引
```

地灵实例数据存于 `spirit` 表，与福地 1:1 绑定（`fudi_id BIGINT NOT NULL UNIQUE REFERENCES fudi(id)`）。字段：`form_id`（NOT NULL，FK → `spirit_form`）、`affection`（默认 0）、`affection_max`（默认 1000）、`mbti_type`（CHECK `^[EI][SN][TF][JP]$`）、`last_gift_time`、`create_time`、`update_time`。

喜好标签直接从 `spirit_form` 读取，不拷贝到 `spirit`。

### 8.2 分配方式

- 注册创建福地时从 `spirit_form` 表中 **随机抽取** 一个形态（`ThreadLocalRandom`），不可更改
- 送礼判定时直接从对应形态的 `liked_tags` / `disliked_tags` 全量标签池匹配
- 同一形态的所有地灵共享同一组标签池（不进行随机子集抽取）

### 8.3 内置形态（以种子数据为准）

> 原文档列出的 7 个形态（小狐妖、春秋蝉、石中鲤、剑灵残片、千年何首乌、乌云踏雪、酒葫芦）中仅「小狐妖」「酒葫芦」等少量仍存在，其余已被替换。当前种子数据 `V1.0.10.1__seed_spirit_form.sql` 共 **60+ 个形态**，按禽兽/草木/天象/器物/奇趣等部类组织，示例：

| 形态名 | 描述 | 喜爱的 tag 示例 |
|--------|------|----------------|
| 小狐妖 | 毛茸茸的狐耳少女，会偷偷往你的仓库里塞药草 | herb, beast, healing, seed, silk |
| 剑灵 | 从古剑中苏醒的剑魂 | sword, blade, attack, ore, metal |
| 老槐树精 | 不知活了多少年的槐树精 | wood, herb, seed, tree, earth |
| 金蟾 | 三足金蟾，喜欢金光闪闪的东西 | metal, ore, fortune, gem, gold |
| 捣药兔 | 月宫中偷跑下来的玉兔 | herb, pill, moon, skill_jade, sweet |
| 酒葫芦 | 说话带着三分醉意的老葫芦 | wine, fruit, sweet, herb, water |
| 断弦琴 | 断了一根弦的古琴化灵 | wood, music, peace, beauty, moon |

（完整清单与各自 `liked_tags` / `disliked_tags` 以 `V1.0.10.1__seed_spirit_form.sql` 为准。）

### 8.4 送礼判定（以代码为准）

玩家只能通过 LLM 调用 `acceptGift` 工具送礼（**没有「地灵送礼」指令**）。从形态对应 `SpiritForm` 的标签池进行匹配；物品 tags 优先取实例 tags，缺失时回退模板 tags：

| 匹配 | 好感变化 | 反应 |
|------|---------|------|
| tags 命中 liked | +10~50 | 开心 |
| tags 命中 disliked | -5~-20 | 嫌弃 |
| 均未命中 | +1~3 | 平淡 |

每日限送 **1 次**，由 `last_gift_time` 控制（自然日，原子 UPDATE 占用）；物品匹配支持精确名称或唯一子串，找不到/多义时报错。

---

## 9. 天劫系统

### 9.1 触发机制（以代码为准）

- **主动渡劫**：玩家发送「福地渡劫」命令，或通过地灵对话由 LLM 调用 `triggerTribulation` 工具
- **冷却**：距上次天劫不足 **1 小时**时拒绝并提示剩余小时
- **强制渡劫（原设计 7 天自动触发）**：**未实现**，没有定时/状态查询触发的天劫
- **无可出战单位**：提示「没有可出战的单位，天劫无法降临」
- **渡劫胜利**：劫数 +1、连胜 +1、灵石奖励 = `连胜 × 100`、好感 +5
- **渡劫失败**：劫数不增加，连胜清零；按 Boss 剩余气血比例摧毁地块——剩余 ≥50% 毁 `ceil(0.6 × 已占地块)`、剩余 ≥20% 毁 `ceil(0.3 × 已占地块)`、否则毁 1 块；好感 -clearCount

### 9.2 渡劫怜悯机制

**触发条件：**

```
地灵好感度 ≥ 800
AND 防守方队伍有存活单位（Boss 尚未清场）
AND 战斗未取胜
```

**效果（以代码为准）：**

| 项目 | 变化 |
|------|------|
| 天劫结果 | 视为胜利（劫数 +1、连胜 +1、灵石奖励） |
| 好感度 | 不加好感（正常渡劫胜利给 +5，怜悯模式不给） |
| 表现 | 叙事文本为「地灵燃烧灵体为你扛过天雷……精力归零，地灵陷入疲惫…」 |

> 原设计「情绪 → EXHAUSTED」无对应实现（无情绪系统）。

---

## 10. LLM 冒犯上报

`feelOffended` 工具让 LLM 在自然对话中自主判断玩家是否说了冒犯的话：

`feelOffended(reason, severity)`：参数为冒犯原因和严重程度（1-5，代码用 `Math.clamp` 截断），调用后扣除 `-severity` 好感。

- 由 LLM 自主判断是否触发，无需硬编码关键词
- severity 直接影响好感扣除量（-1 ~ -5）
- 工具失败会以异常形式回传给 LLM，不影响本轮对话继续

---

## 11. Prompt 结构（以代码为准）

### 11.1 系统 Prompt（由 `SpiritPromptTemplates` 生成）

```
你是{MBTI}性格的地灵。
你认当前与你对话的玩家为主人。
当前形态：{spiritForm}
语气风格：{toneStyle}
好感度：{affection} → {affectionTone}

【福地状态】
- 劫数：{fudiLevel}
- 好感度：{affection}

【地块状态】
{cellDetail}

【流程指引】
你拥有改变福地状态的仙术，你的仙术会告诉你它具体能做什么。
- 主人如果只是闲聊，以地灵身份回复即可，无需动用仙术
- 如果主人的话中明确含有冒犯、不敬或践踏你底线的言行，可动用仙术降低好感度
- 需要先调查再行动时，按顺序依次发动仙术，不要一次全抛出来
- 拿不准主人想做什么时，先问清楚再行动

【人格规则】
- 严格保持语气风格中描述的人格特点
- 根据好感度调整对话态度：高好感亲密温暖，低好感冷淡疏远
- 好感度极低时可能拒绝执行操作或故意执行有误
- 你的情绪表达通过对话语气自然体现，没有固定的情绪状态标签
```

地块状态由 `FudiStateBuilder.buildCellDetailForLLM()` 生成：总地块数、灵田/兽栏组成、空闲编号、已占地块详情（作物生长进度、成熟标记、灵兽孵化进度/产出/繁育冷却）、可收获数量。

### 11.2 好感度语气分层（以代码为准）

分档公式：`bracket = affection / 200`。

| 好感度 | 语气描述 |
|--------|---------|
| ≥1000 | 亲密无间，视你为最重要的人 |
| 800~999 | 非常喜欢，对你温柔体贴 |
| 600~799 | 有好感，愿意主动帮忙 |
| 400~599 | 态度平和，礼貌相待 |
| 200~399 | 略有隔阂，语气生疏 |
| <200 | 态度冷淡，不愿多说话 |
| 负值（理论不可达，好感被 clamp ≥0） | 对你极度厌烦，语气充满敌意 |

### 11.3 Prompt 实际注入流程

最终送入 LLM 的 Prompt 由两部分组成：

```
[SpiritPromptTemplates 生成的系统 Prompt]
  + 工具定义（SpiritCellTools / SpiritBeastTools / SpiritInteractionTools）

对话历史由 MessageChatMemoryAdvisor 自动注入（加载 + 保存），不在 Prompt 模板中手动拼接。
```

> 原文档的 Prompt 模板（含 `{emotionState}`、`giveGift`、`【最近发生的事件】` 等）已被上述模板取代。

---

## 12. 关键技术决策（以代码为准）

- **统一对话基础设施**：对话历史存入 `chat_history` 表（ChatType=SPIRIT），窗口固定 25 条，由 `PerTypeChatMemory` + `ChatMemoryRepositoryAdapter` 自动管理并修剪 DB 超限条目。详见 [AI对话系统](./AI对话系统.md)
- **对话上下文**：`SpiritChatContext`（ScopedValue）持有预加载的福地与地灵，工具调用复用，避免重复查询
- **好感度 capping**：通过 `Spirit.addAffection(amount)` 统一处理，限制在 `[0, affectionMax]`
- **形态喜好全量使用**：从 `spirit_form` 直接读取完整标签池，不做随机子集抽取
- **零 LLM 创建**：注册时形态分配是纯随机 Java 逻辑，不依赖 LLM
- **工具拆分**：原单一 `SpiritTools` 拆为地块/灵兽/互动三个工具类，按域维护
- **频控**：地灵/掌柜/宗灵/秘境之灵共用 `AiChatRateLimiter`（每玩家每分钟 10 次）
- **情绪与福地事件**：原设计中的 AI 情绪系统与懒生成福地事件均未落地，Prompt 明确「没有固定的情绪状态标签」

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **工具集与工具名**：`SpiritTools` 已拆为 `SpiritCellTools`（7）/`SpiritBeastTools`（2）/`SpiritInteractionTools`（3）共 12 个；`getCellStatus`→`checkFudiCells`、`queryBag`→`checkPlayerBag`、`giveGift`→`acceptGift`、`reportPlayerOffense`→`feelOffended`；新增 `upgradeCell`、`manageBeast`、`breedBeasts`、`triggerTribulation`。
- **表名**：`spirit` / `spirit_form` / `chat_history` 均无 `xt_` 前缀；对话历史窗口 25 条不变。
- **内置形态**：原 7 形态大部分已被替换，当前种子 `V1.0.10.1__seed_spirit_form.sql` 共 87 个形态。
- **中性礼物好感**：实际 +1~3（`1 + rand(3)`），旧文档值 +1~2 作废。
- **Prompt 模板**：不含情绪状态与事件段落，新增「流程指引」「人格规则」；语气由好感分档（`affection / 200`）+ MBTI 语气风格驱动。

### B. 保留代码设计

- **MBTI 注册时随机分配（非创建时玩家选择）**：与形态随机一致，注册零选择成本；MBTI 不可更改，让玩家选择只会制造「选错」焦虑。
- **福地/地灵注册自动创建（无创建流程/指令）**：新玩家注册即拥有可对话的地灵，福地经营无需前置任务链或额外 LLM 环节。
- **送礼仅走 LLM `acceptGift`（无「地灵送礼」指令）**：与「所有福地操作统一 Function Calling」一致，单一自然语言入口；礼物只在 LLM 明确调用时消耗，不会误扣。
- **天劫仅主动触发（无「距上次劫后 7 天自动触发」）**：QQ 平台无主动推送，自动天劫只会在玩家下次上线时突然降临，毁地块又掉好感；由玩家选择时机更公平，1 小时冷却防连刷。
- **渡劫失败按 Boss 剩余气血分档毁地（60%/30%/1 块）**：分档可预期且有 1 块下限，比「按差额比例清空」更少挫败；败得越惨罚得越重，方向直观。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

- **福地事件系统**：设计意图：地灵有自己的生活，按 ≥4 小时间隔懒生成事件（含修为/灵石/回血/材料小效果），注入对话并由 `SubEventEffectExecutor` 执行；现状：无 `FudiEventGenerator`、无 `fudi_event_template` 表、无事件注入与效果执行，§4 全部为设计参考。
- **情绪状态机**：设计意图：LLM 通过 `updateEmotion` 维护 9 种情绪并注入 Prompt，天劫胜利/失败/怜悯自动切换；现状：无 `EmotionState`、无 `updateEmotion` 工具、无情绪注入，Prompt 明确「没有固定的情绪状态标签」，当前以好感分档语气 + MBTI 语气风格替代。

### E. 缺陷修复

无
