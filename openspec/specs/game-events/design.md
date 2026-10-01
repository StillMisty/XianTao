# 异步事件系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 核心问题

`UserStateService.loadUser()` 是一个"带副作用的读取"——`tryCompleteTravel()`、`tryHpRecovery()` 默默改了 DB，异步反馈没有任何渠道传递给玩家。

**解决：GameEvent 事件总线**——将"后台偷偷完成的事"全部转为"玩家可见的故事"。

---

## 2. GameEvent 基础设施

### 2.1 事件表

实际迁移：`V1.0.22__create_game_event.sql`（表名 `game_event`，无 `xt_` 前缀；用户外键指向 `player(id)`）。

```sql
CREATE TABLE game_event (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES player(id),
    category        VARCHAR(64) NOT NULL,
    occurred_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered       BOOLEAN NOT NULL DEFAULT FALSE,
    narrative_key   VARCHAR(128),
    narrative_args  JSONB DEFAULT '{}'::jsonb,
    effects         JSONB DEFAULT '{}'::jsonb
);

CREATE INDEX idx_game_event_undelivered ON game_event(user_id, delivered) WHERE delivered = FALSE;
CREATE INDEX idx_game_event_occurred ON game_event(user_id, occurred_at);
CREATE INDEX idx_game_event_cleanup ON game_event(occurred_at) WHERE delivered = TRUE;
```

**字段语义（代码为准）**：`narrative_key` 实际存储的是**叙事模板文本**（由 `event_type.description` 解析或调用方直接写入的含 `{{占位符}}` 文本），投递时由 `NotificationAppender.renderTemplate` 用 `narrative_args` 填充渲染——不存在按 key 查模板的二次查表，叙事文本是随事件持久化的。`effects` 用于承载选择事件（CHOICE）的选项快照。

### 2.2 事件类型

`GameEventCategory` 枚举（category 级别，具体事件名由 `event_type.code` 注册）：

| category | 节标题（sectionTitle） |
|---|---|
| TRAVEL_ARRIVED / TRAVEL_EVENT / TRAVEL_HIDDEN | 旅途见闻 |
| TRAINING_COMPLETE / TRAINING_EVENT / TRAINING_HIDDEN / TRAINING_INTERRUPTED | 历练收获 |
| BOUNTY_COMPLETE / BOUNTY_SIDE_MODIFIER / BOUNTY_HIDDEN / BOUNTY_READY | 悬赏完成 |
| LEVEL_UP | 突破 |
| WORLD_EVENT / WORLD_EVENT_PARTICIPATION | 世界事件 |
| SECT_EVENT | 宗门动态 |
| DUNGEON_ENTER / DUNGEON_EXPLORE / DUNGEON_HIDDEN | 秘境探索 |
| DUNGEON_COMPLETE | 秘境结算 |
| GUIDE | 初入仙途 |
| HP_RECOVERED / BUFF_EXPIRED / DYING_RECOVERED / FORTUNE | 纯文本提示，无框线（sectionTitle = null） |

与文档的差异：`BOUNTY_READY` 在代码中归属「悬赏完成」节（有框线），并非纯文本；新增 `FORTUNE`（每日运势）、`SECT_EVENT`（宗门动态）、`GUIDE`（新手指引）三个文档未列出的 category。

**实际生产者**：

| 生产者 | category |
|---|---|
| TravelCompletionHandler → TravelCompleter | TRAVEL_ARRIVED / TRAVEL_EVENT / TRAVEL_HIDDEN |
| TrainingSettlementHandler / TrainingSettler / TrainingCompleter | TRAINING_EVENT / TRAINING_COMPLETE / TRAINING_HIDDEN / TRAINING_INTERRUPTED |
| BountyCompleter（领奖链路） | BOUNTY_COMPLETE / BOUNTY_SIDE_MODIFIER / BOUNTY_HIDDEN |
| HpRecoveryHandler / DyingRecoveryHandler | HP_RECOVERED / DYING_RECOVERED |
| WorldEvent 相关服务 | WORLD_EVENT / WORLD_EVENT_PARTICIPATION |
| DailyFortuneHandler | FORTUNE |
| UserService（新手引导） | GUIDE |
| SectSharedSkillService | SECT_EVENT |
| EnlightenmentProcessor（顿悟） | TRAINING_EVENT |
| ChoiceService（玩家选择结果回复，不入库） | — |

**未生产（仅枚举/兜底文案）**：`LEVEL_UP`（突破成功回复走命令回复，不产事件）、`BUFF_EXPIRED`（`BuffExpiryHandler` 只删除过期 buff，不产事件）、`BOUNTY_READY`（`BountyCompleter.produceReadyEvent` 存在但无调用方）、`DUNGEON_ENTER / DUNGEON_EXPLORE / DUNGEON_HIDDEN / DUNGEON_COMPLETE`（秘境服务不产 GameEvent）。

### 2.3 NotificationAppender

回复拦截器，在每条回复发送前查询该用户的未投递事件，按 category 分组格式化后追加到回复尾部。调用点在平台处理器层：`QQPlatformHandler.replyText` → `prepareAppend` → 发送成功后 `markDelivered`。

**投递流程（代码）**：

1. 查询 `game_event WHERE user_id=? AND delivered=false ORDER BY occurred_at ASC`
2. 按节标题分组（`LinkedHashMap`，保持首次出现顺序）
3. 渲染：`narrative_key` 模板 + `narrative_args` 替换；未覆盖的 `{{xxx}}` 渲染为 `？`
4. 拼接：原回复非空时先追加分隔线，再追加通知正文；同一节内多条事件之间也追加分隔线
5. **CHOICE 事件保持未投递**：从首个选择事件开始（含之后的所有事件）不进入本次投递标记列表，玩家做出选择前每次回复都重新展示选项
6. 为待选择事件构建按钮键盘（`QqKeyboard`，点击发送「选 X」；平台限制最多 5×5 个按钮，超出部分只保留文本形式）
7. 发送成功后批量 `UPDATE delivered=true WHERE id IN (...)`

**兜底文案**（无 `narrative_key` 时按 category 渲染）：

| category | 兜底文案 |
|---|---|
| TRAVEL_ARRIVED | 你到达了目的地。 |
| HP_RECOVERED | 你的气血已完全恢复。 |
| DYING_RECOVERED | 你从重伤中恢复了过来。 |
| BUFF_EXPIRED | 身上的增益效果已消失。 |
| BOUNTY_READY | 悬赏任务已完成，请使用「悬赏结算」领取奖励。 |
| TRAINING_INTERRUPTED | 你在历练中受了重伤，不得不中断。 |
| LEVEL_UP | 你突破了！ |
| 其他 | 你有了新的经历。（并记录告警，保证事件能被投递，避免僵尸事件反复查询） |

**世界事件加粗**：`WORLD_EVENT` / `WORLD_EVENT_PARTICIPATION` 渲染时把首个「：」之前的部分加粗。

---

## 3. Activity 模型

旅行、历练、悬赏、秘境共享技术管道，但玩法触发规则完全不同。

### 3.1 User 字段统一

实际 `player` 表（`V1.0.2__create_player.sql`）直接包含：

```sql
activity_type       VARCHAR(16)
    CHECK (activity_type IS NULL OR activity_type IN ('TRAVEL', 'TRAINING', 'BOUNTY', 'DUNGEON', 'BOUNTY_SIDE'));
activity_start_time TIMESTAMP;
activity_target_id  BIGINT;
last_settlement_minute BIGINT NOT NULL DEFAULT 0;  -- 历练中途结算的已处理分钟数
last_fortune_date   DATE;                           -- 每日运势生成日期
```

`activity_target_id` 根据 `activity_type` 引用不同表：

- `TRAVEL` / `TRAINING` → `map_node.id`
- `BOUNTY` → `user_bounty.id`
- `DUNGEON` → `dungeon_instance.id`

### 3.2 状态

`UserStatus`：`IDLE`（空闲）、`TRAINING`（历练）、`TRAVELING`（赶路）、`BOUNTY`（悬赏）、`DUNGEON`（秘境）、`DYING`（濒死）。

文档中的历史重命名（`RUNNING → TRAVELING`、`EXERCISING → TRAINING`）已在代码中完成。

### 3.3 触发规则

`UserStateService.resolveState()` 在每次加载用户（`loadUser` / `loadUsersByIds`）时按 `@Order` 依次调用 `StateHandler.tryResolve()`；稳定状态快速跳过。处理器顺序：

| Order | Handler | 职责 |
|---|---|---|
| 1 | TravelCompletionHandler | 旅行到达自动结算 |
| 2 | DyingRecoveryHandler | 濒死超时恢复 |
| 3 | HpRecoveryHandler | 空闲自然回血 |
| 4 | BuffExpiryHandler | 删除过期 buff（不产事件、不置脏） |
| 5 | TrainingSettlementHandler | 历练每 60 分钟中途结算 |
| 6 | DailyFortuneHandler | 每日运势事件 |

| 活动 | 自动完成 | 完成时机 | 玩家操作 |
|---|---|---|---|
| 旅行 | ✅ auto-resolve | 时间到 → 下一次 `resolveState()` | 不需额外命令 |
| 历练 | ❌ 主动结算（另有每小时中途结算） | "历练结算"时结算剩余时长 | 必须主动结束 |
| 悬赏 | ❌ 手动领奖（时间到前拒绝） | "悬赏结算"且时长已满 | 必须手动领 |
| 秘境 | ❌ 手动探索 | "秘境探索"完成 POI，"秘境继续"推进区域 | 回合制逐步推进（当前不产 GameEvent） |

**历练保留主动结算的理由**：核心玩点是"再挂一会儿会不会收益更高？"auto-resolve 会消灭这个 risk/reward tension。战斗中 HP 归零会导致历练中断——角色进入 DYING 状态，`activity_start_time` 清空，结算到死亡点为止的收益，剩余时间丢弃。事件产出 `TRAINING_INTERRUPTED`，归入「历练收获」节。

**代码补充**：历练期间每 60 分钟自动执行一次中途结算（`TrainingSettlementHandler`，结算 `last_settlement_minute` 到当前分钟的增量）并产出 `TRAINING_EVENT`；玩家主动结算时只结算剩余时长。最短历练 ≤ 5 分钟无收获。

**限制**：只允许同时接一个悬赏（接取要求空闲状态，活动字段单值）。

**悬赏设计（文档）**：auto-complete + manual claim——时间到了自动标记完成，但奖励必须手动领，保留"选任务"和"领奖"的仪式感。`resolveState()` 检测到超时但不改变 user.status，仅产出 `BOUNTY_READY` 事件提示玩家领奖。**代码现状**：该自动完成与提示链路未实现（无悬赏 StateHandler，`produceReadyEvent` 无调用方），玩家需在时长满后自行发送「悬赏结算」。

---

## 4. 奖励身份

### 4.1 旅行 — "缘"（低频高风险高回报）

旅行事件设计为**触发概率低（基础 30% × 命运修正）但单次效果强劲**。使用 `ADD_EXP_PERCENT`/`TAKE_DAMAGE_PERCENT` 等百分比效果代替固定值，随玩家等级自动缩放，确保在任何境界都保持"高风险高回报"的感受。

其中 CHOICE 型事件（如旅行商人 `travel_curious_merchant`）触发概率更低（weight 2-4 vs 普通 10-20），但产出为稀有品类的完整物品（玉简/锻造图/兽卵/装备），不可通过常规子事件获取。

| 维度 | 设计 |
|---|---|
| 玩家动机 | 到达新地图 |
| 核心产出 | 叙事 > 奖励（NUMERIC）/ 稀有物品（CHOICE） |
| 触发概率 | 基础 0.30，命运修正后 0.21~0.39（不触发时无事发生）；CHOICE 额外低至基础 0.02~0.08 |
| 方差 | 极高 — 低概率、大效果 |
| 奖励来源 | 专属事件奖励表 |
| 效果类型 | 百分比优先：ADD_EXP_PERCENT（2-8%升级修为）、TAKE_DAMAGE_PERCENT（5-15%最大生命） |
| 叙事 | "你意外发现…" |

```
触发时（~30% 概率）:
━━━ 旅途见闻 ━━━
一颗流星在你前方不远处坠落了——赶去看看有没有陨铁。
天外陨铁×1 | 修为 +3%

不触发时（~70% 概率）:
（无额外事件，仅到达叙事）
```

代码：`TravelCompleter` 在到达时以 `0.30` 触发概率调用统一子事件池（NUMERIC + CHOICE 一起加权抽取），概率再乘 `FortuneService.getFateMultiplier`（0.7 ~ 1.308，钳制 [0,1]）。

### 4.2 历练 — "力"

| 维度 | 设计 |
|---|---|
| 玩家动机 | 升级、刷材料 |
| 核心产出 | **大量 exp** + 地图特产 |
| 方差 | 低 — 时间正相关 |
| 奖励来源 | exp 公式 + 地图特产 + 战斗掉落 |
| 叙事 | "你战斗获得了…" |

```
━━━ 历练收获 ━━━
你在青云山外门历练了 47 分钟。
遇敌 5 场 | 击杀 12 只 | 修为 +482
获得：聚灵草×3、玄铁×2
```

### 4.3 悬赏 — "利"

| 维度 | 设计 |
|---|---|
| 玩家动机 | 拿装备/灵石/蛋 |
| 核心产出 | 装备、灵石、灵兽蛋 — **无 exp** |
| 方差 | 零 — 接取时预定 |
| 奖励来源 | 悬赏独立配置的奖励池 |
| 叙事 | "你领取了报酬…" |

```
━━━ 悬赏完成 ━━━
委托"清理山精"已完成。
你从发布人处领取了约定的报酬：
✨ 桃木符×1 | 灵石 +50
```

---

## 5. 随机子事件

旅行、历练、悬赏在真实场景中都会遇到"插曲"。子事件共享加权随机抽取机制，但**每个活动对奖励的影响方式不同**。

### 5.1 事件池存储 — 统一事件系统

所有事件（遇怪战斗 + 数值子事件）统一存储在 `activity_event`，通过 `event_type` 区分类型，`params` JSONB 存储纯数据驱动效果。**新增事件只需 INSERT 一条 SQL，零 Java 改动。**

```sql
CREATE TABLE event_type (
    id              BIGSERIAL PRIMARY KEY,
    activity_type   VARCHAR(32) NOT NULL,  -- TRAVEL / TRAINING / BOUNTY_SIDE / DUNGEON
    code            VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,                  -- 叙事模板，{{key}} 占位符自动填充
    UNIQUE (activity_type, code)
);

CREATE TABLE activity_event (
    id              BIGSERIAL PRIMARY KEY,
    activity_type   VARCHAR(32) NOT NULL CHECK (activity_type IN ('TRAVEL', 'TRAINING', 'BOUNTY_SIDE', 'DUNGEON')),
    owner_id        BIGINT NOT NULL,                  -- map_id / bounty_id / dungeon_id
    code            VARCHAR(64) NOT NULL REFERENCES event_type(code),
    event_type      VARCHAR(16) NOT NULL DEFAULT 'NUMERIC' CHECK (event_type IN ('NUMERIC', 'COMBAT', 'CHOICE')),
    weight          INT NOT NULL DEFAULT 100 CHECK (weight >= 0),
    is_hidden       BOOLEAN NOT NULL DEFAULT FALSE,   -- 是否为隐藏事件
    trigger_type    VARCHAR(32),                      -- 隐藏事件触发条件
    trigger_params  JSONB DEFAULT '{}'::jsonb,
    params          JSONB DEFAULT '{}'::jsonb,        -- 效果参数
    prerequisite_code VARCHAR(64),                    -- 前置隐藏事件 code（文档未列，代码新增）
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_event_lookup ON activity_event(activity_type, owner_id, is_hidden);
CREATE INDEX idx_activity_event_type_lookup ON activity_event (activity_type, owner_id, event_type, is_hidden);
```

**params JSONB 效果格式**（NUMERIC 事件）：

```json
// 简单效果
{"effects": [{"type": "ADD_EXP", "min": 20, "max": 50}]}

// 复合效果
{"effects": [
  {"type": "ADD_ITEM", "template_id": 15, "min": 2, "max": 4},
  {"type": "TAKE_DAMAGE_PERCENT", "amount": 0.12}
]}

// 分支 (50%赢/50%输)
{"branches": [
  {"chance": 0.5, "effects": [{"type": "ADD_EXP", "min": 25, "max": 80}]},
  {"chance": 0.5, "effects": [{"type": "TAKE_DAMAGE_PERCENT", "amount": 0.08}]}
]}
```

分支由 `BranchResolver` 按累计概率抽取（`roll < cumulative`，累计接近 1.0 时兜底取当前分支）。

**params JSONB 效果格式**（COMBAT 事件）：

```json
// 遇怪战斗
{"monster_template_id": 5, "min_count": 2, "max_count": 4}
```

**params JSONB 效果格式**（CHOICE 事件）：

```json
// 交互选择 — 玩家用「选 A/B/C」指令选择
{"options": [
  {"key": "A", "text": "上前救治", "effects": [{"type": "HEAL_FLAT", "amount": 50}]},
  {"key": "B", "text": "转身离去", "effects": [{"type": "ADD_SPIRIT_STONES", "amount": 20}]}
]}
```

注意：CHOICE 事件在 `game_event.effects` 中的实际 JSON 结构为 `{"choice": {"options": [...]}}`（`EffectData.ChoiceOptions`）。

**模板变量规则**：每个 `activity_event.params.effects` 中 handler 返回的 key 必须覆盖 `event_type.description` 中所有 `{{key}}` 模板变量。遗漏的占位符会被 `NotificationAppender.renderTemplate` 渲染为 `？`。

**13 种效果类型**（`SubEventEffectType`，文档列为 12 种，缺 `TAKE_SPIRIT_STONES`）：

| type | 参数 | 说明 |
|------|------|------|
| `ADD_EXP` | `amount` 或 `min`+`max` | 固定/随机修为 |
| `ADD_EXP_PERCENT` | `percent` (0.0~1.0) | 升级所需差值百分比 |
| `TAKE_DAMAGE_PERCENT` | `amount` (0.0~1.0) | 百分比扣血 |
| `TAKE_DAMAGE_FLAT` | `min`+`max` | 固定扣血 |
| `HEAL_FLAT` | `min`+`max` | 回血 |
| `ADD_ITEM` | `template_id`, `count`或`min`+`max` | 加物（ID） |
| `ADD_RANDOM_ITEM` | `template_ids[]`, `chance` | 概率抽物品池 |
| `CREATE_EQUIPMENT` | `template_id` | 创建装备 |
| `DROP_SPECIALTY` | `count`(默认1) | 地图特产加权掉落 |
| `ADD_SPIRIT_STONES` | `min`+`max` | 加灵石 |
| `TAKE_SPIRIT_STONES` | `min`+`max`/`amount` | 扣灵石（种子配置中大量使用） |
| `MULTIPLY_BOUNTY_REWARD` | `multiplier` | 悬赏灵石倍率 |
| `PURE_NARRATIVE` | 无 | 纯叙事 |

**COMBAT 事件**：调用 CombatEngine 执行完整回合战斗（含顿悟、死亡检测、灵兽HP追踪），结果累加到 CombatSummary。COMBAT 事件在历练统一循环中处理；战斗 HP 归零会中断历练。

**地图 `encounter_richness`**（1-10）控制事件间隔密度：

```sql
ALTER TABLE map_node ADD COLUMN encounter_richness INT NOT NULL DEFAULT 5 CHECK (encounter_richness BETWEEN 1 AND 10);
```

| activity_type | owner_id | 预计行数（设计估算） |
|---|---|---|
| TRAVEL | map_id | ~80（含 NUMERIC + CHOICE） |
| TRAINING | map_id | ~240 (NUMERIC) + ~200 (COMBAT) |
| BOUNTY_SIDE | bounty_id | ~30000 |
| DUNGEON | dungeon_id | ~10 (NUMERIC) + ~2 (CHOICE) |

实际种子规模（按迁移文件中的事件条目数）：TRAVEL 88 条、TRAINING 40 条、COMBAT 196 条、BOUNTY_SIDE 41 条、DUNGEON 10 条。文档表格中的「预计行数」为早期设计估算，以实际配置为准。

### 5.2 事件类型

所有事件类型由 DB `event_type` 表注册，Java 侧**不再硬编码**。新增事件 = INSERT 一条 `event_type` + 一条 `activity_event`。

**COMBAT 事件命名**：`combat_monster_<怪物名>`（如 `combat_monster_野狼`）<br>
**NUMERIC 事件命名**：保留前缀约定 `travel_*` / `training_*` / `bounty_*`<br>
**CHOICE 事件命名**：无特殊前缀，由 `activity_type` 决定分类（如 `travel_curious_merchant`）

### 5.3 子事件对奖励的影响

| | 旅行 | 历练 | 悬赏 | 秘境 |
| |---|---|---|---|
| **作用目标** | 玩家状态 | 玩家状态 | **主奖励本身** | 玩家状态 |
| **可修改** | HP、exp、物品、灵石 | HP、exp、物品 | 悬赏灵石倍率 | HP、exp、物品、灵石 |
| **产出方式** | 给新东西 | 给新东西 + 遇怪战斗 | **context 传参调节已有奖励** | 探索后加权随机抽取 |
| **玩家感知** | "路上捡到个东西" | "修炼中遇到…" | "任务完成得不太顺/特别顺" | "发现了隐藏的密室…"

**旅行子事件**：独立发奖。撞到的宝箱就是额外赚的，与"到达"无关。

**历练子事件**：统一池含 COMBAT + NUMERIC（+ CHOICE）。COMBAT 走 CombatEngine 真战斗，NUMERIC 走 `SubEventEffectExecutor` 加数值，CHOICE 写入待选择事件。均在一个循环加权抽取。遇怪可致死中断历练。

**悬赏子事件**：**不产独立奖励，而是调节主奖励**。通过 `EventContextKeys.BOUNTY_REWARD`（long[]）传入传出，`MULTIPLY_BOUNTY_REWARD` effect 修改倍率；领奖时以 `1.0` 触发概率抽取，抽中后主奖励灵石按倍率变化，之后再乘当日财运倍率。

```
主奖励玄铁×2，触发了 SABOTAGE：
━━━ 悬赏完成 ━━━
委托"清理山精"已完成。
你从发布人处领取了约定的报酬：
✨ 玄铁×1   ← 少了一件
你在追踪过程中发现目标已被其他猎手抢先一步，损失了一部分战利品。

主奖励灵石+50，触发了 BONUS_PAY：
━━━ 悬赏完成 ━━━
委托"清理山精"已完成。
你从发布人处领取了约定的报酬：
✨ 灵石 +75   ← 多了
委托人很满意你的效率，额外给了赏钱。
```

没有一个新东西入包。东西还是那个东西，只是变多/变少/拿得快/拿得慢了。

**旅行 CHOICE 事件**：可选交互 —— 玩家用「选 A/B/C」指令决定走向。触发时事件保持未投递，等待玩家输入后执行对应的 effects。旅行中的 CHOICE 事件示例：

- `travel_curious_merchant` — 旅行商人。极低触发概率（weight=2-4），携带玉简/锻造图/兽卵/装备四类稀有物品。商人叙事中随机报价（极贵或极便宜），玩家可选择购买不同品类的物品，或拒绝但获得商人的赏识奖励。

```
旅行商人出现在 map 6 (云来村):
━━━ 旅途见闻 ━━━
一位背着竹篓的旅行商人主动上前搭话。
他摊开篓子里的物什：
  A. 万剑归宗玉简 — 八百灵石
  B. 火蟾卵 — 五百灵石
  C. 摇头不买
→ 选 A：获得万剑归宗玉简×1
→ 选 B：获得火蟾卵×1
→ 选 C：获得灵石 +50 | 修为 +30
```

CHOICE 事件的效果使用与 NUMERIC 相同的 effect type，options 中每条分支独立配置 effects 数组。渲染时 `NotificationAppender` 为 CHOICE 事件追加选项编号和提示文本，并附带按钮。

### 5.4 触发节奏

```
旅行：到达时 roll 1 次（NUMERIC + CHOICE 统一池加权抽取）
      密度：1 次 / 整段旅程
      方式：SubEventSelector 加权随机，基础概率 0.30 × 命运倍率

历练：遇怪与子事件统一循环，动态间隔
      interval = clamp((12 - map.encounter_richness) × levelMismatch / mapDanger, 3, 20)
      slots = duration / interval
      perRollChance = min(1.0, min(1.0, 4 / interval) × 命运倍率)
      密度：多次 / 整段历练，interval 越短越密集
      方式：一个池（COMBAT + NUMERIC + CHOICE）加权随机抽取

悬赏：领奖时 roll 1 次
      密度：0-1 次 / 整段悬赏
      方式：SubEventSelector 加权随机，触发概率 1.0

秘境（DUNGEON）：进入时总是触发区域叙事 — 推进区域时总是触发区域叙事 — 探索每个 POI 后 20% 概率触发子事件 — 通关时总是触发结算叙事
      密度：多次 / 整场秘境
      方式：固定叙事直接写 GameEvent，子事件通过 SubEventSelector 加权随机
      （代码现状：秘境服务不产 GameEvent，此管道未接入）
```

- `encounter_richness`：地图配置 (1-10)，10=频发
- `levelMismatch`：`1 + |playerLv - mapLv| / mapLv × 2.5`，等级匹配时密集
- `mapDanger`：`1 + (mapLv-1) × 0.015`，高级图更密集
- 实际间隔被钳制在 [3, 20] 分钟，并在此基础上以 `perRollChance` 做每个时段的二次掷骰（而非每个 slot 必触发）

### 5.5 叙事呼应活动调性

```
旅行子事件 → "你途经竹林时，发现…"       ← 经过、偶遇
历练子事件 → "你拨开密林深处，发现…"     ← 主动探索、发现
悬赏子事件 → "追击目标时，你发现…"       ← 任务过程、插曲
```

---

## 6. 隐藏事件

子事件是随机的、被动的、可重复的。隐藏事件有**触发条件**、可**被发现**、带**专属奖励**、**每人只能触发一次**。

### 6.1 核心心理模型

```
子事件：骰子决定 → "路上捡到个东西"
隐藏事件：你满足了某个条件 → "原来还可以这样！？"
```

三个活动都有隐藏事件，但风格不同：

| | 旅行 | 历练 | 悬赏 | 秘境 |
| |---|---|---|---|
| 动机 | 探索世界 | 修炼精进 | 完成任务 | 探宝寻秘 |
| 隐藏风格 | 发现隐藏地图 | 触发修炼机缘 | 发现任务线索 | 发现隐藏密室 |
| 条件 | 持有某物/特定路线 | 属性阈值/多次历练 | 前置条件/特定装备 | 满足条件后探索触发 |
| 奖励 | 专属地图入口 | 专属功法/丹方 | 专属物品 | 稀有物品/大量灵石 |

### 6.2 触发条件类型

设计类型（文档）：

- `HAS_SKILL` — 装备了指定法决
- `HAS_ITEM` — 背包里有指定物品
- `HAS_EQUIPMENT` — 穿戴了指定装备
- `STAT_THRESHOLD` — 某属性 ≥ N（如悟性≥50 发现隐藏线索）
- `LOCATION` — 途经/位于特定地图
- `PREVIOUS_COMPLETION` — 完成过某个前置
- `TIME_OF_DAY` — 特定时间段（子时、午时…）
- `BEAST_DEPLOYED` — 出战了特定灵兽
- `LEVEL_RANGE` — 等级在特定区间

**代码现状**：`TriggerConditionChecker` 仅实现 `HAS_SKILL`（`skill_id`）、`HAS_ITEM`（`item_template_id`）、`STAT_THRESHOLD`（`stat` + `min`，支持 STR/CON/AGI/WIS）；其他类型走 `default -> true`（即未知条件一律视为满足）。「完成过前置」由 `activity_event.prerequisite_code` + `hidden_completion` 的 `checkPrerequisite` 独立实现（相当于 PREVIOUS_COMPLETION）。实际种子配置只使用了 HAS_SKILL / HAS_ITEM / STAT_THRESHOLD 三种。

### 6.3 配置

`activity_event` 加标记字段：

- `is_hidden` — 是否为隐藏事件
- `trigger_type` — 触发条件类型
- `trigger_params` — 条件参数 JSONB
- `prerequisite_code` — 前置隐藏事件 code（代码新增）

### 6.4 唯一性跟踪

```sql
CREATE TABLE hidden_completion (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES player(id),
    activity_type   VARCHAR(32) NOT NULL,
    owner_id        BIGINT NOT NULL,
    code            VARCHAR(64) NOT NULL,
    completed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, activity_type, owner_id, code)
);
```

结算时的检查逻辑（`ActivitySubEventPipeline.checkHiddenEvents`）：遍历该活动的隐藏事件 → 前置已完成 → `hidden_completion` 无记录 → 满足触发条件 → 写入完成记录 + 执行效果 + 产出事件。否则跳过。玩家之间不互斥（每人一份记录）。

### 6.5 触发时机

| 活动 | 检查时机 | 检查者 |
|---|---|---|
| 旅行隐藏 | 到达时，子事件之后 | TravelCompleter |
| 历练隐藏 | 结算时，子事件循环之后 | TrainingCompleter |
| 悬赏隐藏 | 领奖时，子事件之后 | BountyCompleter |
| 秘境隐藏 | 探索 POI 时，checkHiddenEvents | DungeonEventCompleter（未实现） |

### 6.6 各活动示例

**旅行隐藏**：前提是背包里有某物，或者悟性达标，触发后发现隐藏地点。

```
你来到落霞坊市，发现角落里有一个老头在摆摊。
他瞥了你一眼，目光扫过你腰间的龙鳞："这东西你哪来的？"
→ 你背包里有龙鳞 → 触发隐藏
  老头带你去了坊市地下密室，里面竟有一件上古法器残片。
  获得：上古法器残片（专属物品，不可通过其他途径获得）
→ 之后再经过落霞坊市，老头还是那个老头，但不再触发特殊剧情
```

**历练隐藏**：满足属性或装备条件后触发，发现修炼捷径。

```
你在万妖谷历练时，突然感受到一股凌厉的剑气。
你顺着剑意的指引，在谷底发现了一条隐秘的山洞。
洞壁上刻着一套残缺的剑法...
（触发条件：悟性≥50 且装备了剑类法器）
获得：万妖谷剑诀（专属法决）
→ 第一次触发，给专属法决
→ 之后只是普通历练
```

**悬赏隐藏**：文档设计为两阶段——接取时检测触发条件 → 给线索 → 领奖时检测条件是否仍满足 → 给隐藏奖励。

```
接取"清理山精"：
发布人看了一眼你腰间的火灵符：
"你要是真有点本事，做完之后去山精巢穴里面看看。"

领奖时（背包里仍有火灵符，或条件已记录在 user_bounty 中）：
你想起发布人的话，用火灵符炸开了巢穴深处封死的石门，
发现了一处隐秘的灵石矿脉！
额外获得：火灵石×5 | 灵石 +50

→ 之后再做这个悬赏 → 巢穴已经被翻过了，不再触发
→ 玩家之间不互斥 → 每人一次
→ 不显示进度 → 保持神秘感
```

线索存储在 `user_bounty.hidden_clues`（JSONB）：
接取时若条件匹配，写入 `{code: "FIRE_LING_MINE", hint_key: "bounty.hidden.fire_ling_mine"}`。
领奖时读取该字段校验条件并产出入库事件。

**代码现状**：`user_bounty.hidden_clues` 为「预留字段，当前未使用」（接取时固定写入空 Map），两阶段线索机制未实现；悬赏隐藏事件实际在**领奖时一次性检查条件**（`BountyCompleter.checkHiddenEvents`），无接取时线索提示。

### 6.7 设计规则

| 规则 | 原因 |
|---|---|
| 隐藏事件**每人只触发一次** | 奖励额外丰富，不限制会乱套 |
| 玩家之间**不互斥** | 隐藏奖励不是限量一件，是每人一次的惊喜 |
| **不显示进度/勾选框** | 保持神秘感 |
| 接取时**显示线索** | 给玩家"这次可能有什么"的期待（未实现） |
| 触发后**叙事不同** | 第一次给专属剧情，之后给普通叙事 |
| **条件类型丰富** | 覆盖法决、物品、装备、属性等多个维度（已实现 3 种） |
| **专属奖励** | 某些物品只通过隐藏事件产出，驱动探索和传播 |

---

## 7. 子事件 vs 隐藏事件

| | 子事件 | 隐藏事件 |
|---|---|---|
| **用于** | 随机决定触发了哪个插曲 | 玩家满足条件后触发 |
| **触发方式** | 权重加权随机 | **条件匹配** |
| **重复性** | 每次可重复 | **每人一次** |
| **奖励深度** | 浅 | 深（专属奖励） |
| **叙事权重** | 中 | 高 |
| **范围** | 所有活动 | 所有活动 |
| **可传播** | ❌ | ✅ "你知道清理山精带火灵符有隐藏吗？" |

子事件重复配置：`activity_event` 无 UNIQUE 约束（除 id），允许同 code 多行，不同 params 的不同变体各自独立。

---

## 8. 数据流

```
玩家发任意命令
  → 命令处理器生成回复
     → QQPlatformHandler.replyText
        → NotificationAppender.prepareAppend
           → 查询 game_event WHERE user_id=? AND delivered=false ORDER BY occurred_at ASC
           → 按 category 选择节标题
           → 渲染 narrative_key 模板 + narrative_args（未覆盖占位符 → ？）
           → 追加到原回复尾部（含分隔线）
           → 发送最终文本（可附带 CHOICE 按钮）
        → 发送成功后批量 UPDATE delivered=true WHERE id IN (...)（CHOICE 及其后事件除外）

事件产出入口：
  resolveState()    → 旅行到达（子事件 + 隐藏）、HP恢复、DYING恢复、每日运势、历练中途结算...
  TrainingService   → 历练结算（统一事件循环：COMBAT + NUMERIC + CHOICE）、历练中断
  BountyService     → 悬赏领奖（子事件调节主奖励、隐藏事件、完成叙事）
  CultivationService→ 升级/突破（LEVEL_UP）
  WorldEvent 相关服务→ 世界事件与参与结果
  CHOICE 事件        → 触发后保持未投递等待「选 A/B/C」指令，ChoiceService 执行对应 effects 并标记投递

重要约束：
  - 事件 INSERT 与业务状态修改在同一事务中（各 Completer/Handler 标注 @Transactional）
  - delivered 标记在发送成功之后执行
  - delivered=true 的事件保留 7 天后清理（@Scheduled 每天凌晨 3 点，Asia/Shanghai）
  - narrative 模板文本随事件存储在 narrative_key，占位符在格式化时渲染
```

**ChoiceService 行为**：玩家发送「选 X」（单个字母 A-Z，大小写不敏感）→ 取首个未投递 CHOICE 事件 → 匹配选项 key → 执行该选项 effects（在空上下文）→ 标记该事件已投递 → 回复选择结果与效果摘要。非法字母、无待选事件、key 不存在均有明确提示。

---

## 9. 设计决策记录

| 决策 | 选择 | 理由 |
|---|---|---|
| 事件存储 | 独立表 `game_event` | 可查询可追溯，取代 extraData |
| 投递方式 | NotificationAppender 拦截回复 | handler 零改动 |
| 投递标记 | 发送成功后标记 delivered | 发送失败则下次重新投递，不丢事件 |
| 事件清理 | delivered=true 保留 7 天 | 不需要长留档，定期删除 |
| 事务边界 | 事件写入与业务变更同一事务 | 防止"状态变了但玩家不知道" |
| narrative | `narrative_key` 存模板文本 + `narrative_args` 渲染 | 避免硬编码，物品/地图改名后不受影响（模板在事件生成时解析并快照） |
| 事件命名 | `category`（大类） / `code`（具体类型码） | 区分两层概念，三表统一 code |
| 活动多态 | `activity_type` 列区分目标引用 | `activity_target_id` 按类型引用不同表 |
| 旅行触发 | auto-resolve | 到了就是到了 |
| 历练触发 | manual settle | 保留"再挂一会儿"的 tension |
| 历练中断 | 战斗中 HP 归零 → DYING，结算到死亡点 | 风险兑现，丢弃剩余时间 |
| 悬赏触发 | auto-complete + manual claim（设计） | 保留选任务和领奖的仪式感；代码仅实现 manual claim |
| 悬赏数量 | 同时只允许一个 | `activity_target_id` 单值设计 + 接取要求空闲 |
| 子事件作用 | 旅行：独立给 / 历练：COMBAT+NUMERIC+CHOICE 统一池 / 悬赏：context 调节 | 保持各自奖励身份 |
| 悬赏子事件 | **context 模式**，不产独立奖励 | `MULTIPLY_BOUNTY_REWARD` effect 修改倍率 |
| 子事件触发 | 统一 weight 加权随机，一个池抽取 | 遇怪和数值事件不再分两个独立循环 |
| 事件配置 | **纯 SQL 数据驱动** | 13 种 effect type 覆盖所有场景，新增事件零 Java 改动 |
| 遇怪配置 | 迁入 `activity_event` (event_type=COMBAT) | 遇怪与子事件统一管理 |
| 地图事件密度 | `encounter_richness` INT (1-10) | 更直观，配表成本更低 |
| 遇怪间隔公式 | `interval = clamp((12 - richness) × levelMismatch / mapDanger, 3, 20)` + 每段二次掷骰 | 等级匹配时密集，偏离时稀少；装备不躲怪 |
| 隐藏事件 | 条件触发、每人一次、专属奖励 | 驱动探索和玩家间传播 |
| 隐藏跟踪 | `hidden_completion` 独立表 | 灵活查询，跨活动统一 |
| 隐藏线索 | 接取时写入 `user_bounty.hidden_clues`（设计） | 两阶段检查有据可查；代码未使用该字段 |
| 隐藏条件检查 | `TriggerConditionChecker` 统一实现 | 消除 TrainingCompleter/TravelCompleter 重复逻辑（仅 3 种条件） |
| 子事件重复 | 无 UNIQUE 约束，允许同 code 多行 | 不同 params 的不同变体各自独立 |
| DUNGEON 事件 | 进入/推进/通关直接写 GameEvent，探索 POI 用 SubEventSelector（设计） | 固定叙事保证仪式感，子事件保持随机惊喜；代码未接入 |
| DUNGEON 隐藏事件 | 绑定 dungeon_id 为 owner_id，触发后记录 hidden_completion（设计） | 复用现有隐藏事件机制；代码未接入 |
| 旅行触发概率 | 基础 0.30，命运修正 0.21~0.39 | 低频让事件从"例行公事"变成"期待惊喜"，百分比效果保证任何境界都有体感 |
| 模板变量兜底 | 未覆盖的 {{xxx}} 渲染为 `？` | 避免暴露内部变量名，同时让开发者能发现配置遗漏 |
| CHOICE 事件 | 旅行子事件池同时包含 NUMERIC 和 CHOICE，统一加权抽取 | 保持触发模型的简单性；weight 控制稀缺度 |
| 旅行商人 | 仅旅行含 CHOICE 事件，物品为玉简/锻造图/兽卵/装备 | 旅行是唯一"一次 roll 一次"的活动，适合暂停等待玩家输入；产出为稀有品类完整物品，驱动探索期待 |
| CHOICE 未投递 | 选择事件在玩家作答前保持未投递并每次重展示 | 防止选项被后续回复顶掉后玩家无从选择 |

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名无 `xt_` 前缀**：`game_event`、`event_type`、`activity_event`、`hidden_completion`、`player`、`map_node`、`user_bounty`。
- **`narrative_key` 存模板文本而非 key**：生成时由 `ActivityEventHelper.resolveNarrativeKey` 把 `event_type.description` 快照写入，投递时直接渲染，无二次查表。
- **新增 category**：`FORTUNE`（每日运势，纯文本）、`SECT_EVENT`（宗门动态）、`GUIDE`（初入仙途）为文档未列出的类型。
- **效果类型 13 种**：文档 12 种表缺少 `TAKE_SPIRIT_STONES`（种子中大量使用）。
- **投递包装位置**：在平台处理器 `QQPlatformHandler.replyText` 中调用 `NotificationAppender`，非 QQ 监听器层。
- **`activity_type` 取值**：CHECK 还包含 `BOUNTY_SIDE`；`DUNGEON` 的 `activity_target_id` 指向 `dungeon_instance.id`。
- **`activity_event` 额外字段**：`prerequisite_code`（前置隐藏事件解锁）。
- **清理任务有定时调度**：每天凌晨 3 点（Asia/Shanghai）清理 7 天前的已投递事件，未投递不清理。
- **事件渲染兜底**：无叙事模板时按 category 输出固定兜底文案并记录告警，保证事件能被投递。

### B. 保留代码设计

- **`BOUNTY_READY` 归入「悬赏完成」节**：待领奖提示是行动号召，带节标题更醒目；文档「纯文本无框线」不利于提醒。
- **`LEVEL_UP` 不产事件**：突破只能由「突破」指令触发，指令回复即时呈现结果，再产事件是重复通知；枚举与兜底文案保留，未来若有后台突破再启用。
- **历练每 60 分钟中途结算**：长时间挂机有阶段性反馈，中断/遗忘时已结算时段不丢；剩余时长仍由玩家主动结算，保留「再挂一会儿」的 tension。
- **「前往 X」不隐式结算历练**：保留玩家对「何时结束历练」的决定权，避免出行时被动结算；与「主动结算」的设计身份一致。
- **遇怪间隔钳制 `[3, 20]` + 每时段二次掷骰**：避免公式极端值造成刷屏或长时间无事；`perRollChance` 让实际触发频率贴合期望，而非每 slot 必触发。
- **悬赏子事件触发概率 1.0**：每次领奖都抽取一次调节，避免「白板结算」；池空时不产，风险/增益分支本身提供方差。
- **CHOICE 事件保持未投递、每次重展示并附按钮**：防止选项被后续回复顶掉导致玩家无从作答；代价是阻塞其后事件投递，属可接受取舍。

### C. 按设计修正（待修）

无。

### D. 未实现（待办）

- **隐藏事件其余 5 种触发条件**：设计意图 = `HAS_EQUIPMENT` / `LOCATION` / `TIME_OF_DAY` / `BEAST_DEPLOYED` / `LEVEL_RANGE`；现状 = `TriggerConditionChecker` 仅实现 `HAS_SKILL` / `HAS_ITEM` / `STAT_THRESHOLD`（PREVIOUS_COMPLETION 由 `prerequisite_code` 机制实现），未知 `trigger_type` 走 `default -> true` 放行——接入新类型前需改为显式校验（spec 仅要求已实现的 3 种 + 前置）。
- **悬赏自动完成 + 领奖提示链路**：设计意图 = 时间到自动标记完成并产出 `BOUNTY_READY` 提示，保留「领奖」仪式感；现状 = 无悬赏 StateHandler，`produceReadyEvent` 无调用方，玩家需在时长满后自行发送「悬赏结算」。
- **`BUFF_EXPIRED` 事件**：设计意图 = 增益到期提醒（枚举与兜底文案已备）；现状 = `BuffExpiryHandler` 仅删除过期 buff，不产事件。
- **悬赏两阶段隐藏线索**：设计意图 = 接取时按条件写入线索（`user_bounty.hidden_clues`）、领奖时二段校验并给隐藏奖励，制造「这次可能有什么」的期待；现状 = 字段为预留（接取固定写空 Map），隐藏事件仅在领奖时一次性检查。
- **秘境（DUNGEON）事件管道**：设计意图 = 进入/推进/通关固定叙事 + 探索 POI 概率子事件 + 秘境隐藏事件；现状 = `DUNGEON_*` category 与 `activity_event` 的 DUNGEON 配置存在，但秘境服务不产 GameEvent，`DungeonEventCompleter` 不存在。

### E. 缺陷修复

无。
