# 历练系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

历练系统是玩家挂机获取修为和物品的核心玩法。玩家在历练区地图中自动遇怪战斗，结算时获得修为与物品奖励（**不发放灵石**）。

**核心流程：**
1. 玩家在历练区地图使用 `历练` 命令开始历练
2. 系统记录开始时间，玩家状态变为 `TRAINING`
3. 系统每 60 分钟（懒结算）自动执行一次中途结算并推送通知
4. 玩家使用 `历练结算` 命令结束历练，系统计算未结算时长、模拟战斗并结算奖励

**设计原则：**
- 历练只能在玩家当前所在地图进行，不支持指定其他地图
- 旅行系统提供移动成本，历练系统提供挂机收益，两者形成完整的游戏循环
- 文字 MUD 挂机游戏，结算输出只统计数据，不逐场输出战斗过程

---

## 2. 已实现功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 开始历练 | ✅ | 记录开始时间，设置状态 |
| 结束历练 | ✅ | 计算时长，结算奖励 |
| 统一事件循环 | ✅ | COMBAT + NUMERIC + CHOICE 一个池加权抽取 |
| 动态遇怪间隔 | ✅ | encounter_richness + 等级匹配 + 地图凶险 |
| SQL 配置化 | ✅ | 新增事件只需 INSERT 一条 SQL，零 Java 改动 |
| 等级衰减机制 | ✅ | 高于地图等级 5 级后开始衰减，每级衰减 4% |
| 精简输出格式 | ✅ | 文字 MUD 风格，只统计数据 |
| 高光战斗识别 | ✅ | 识别长回合、残血、技能多样的战斗 |
| 技能效果系统 | ✅ | 效果类型全部实现（见战斗系统） |
| 灵兽参战 | ✅ | 出战灵兽入队，可使用技能；阵亡转休养 |
| 战斗日志统计 | ✅ | 记录遇敌场次、击杀数、战败次数等 |
| 中途自动结算 | ✅ | 每 60 分钟一次（懒结算，附带通知） |
| 顿悟 | ✅ | 悟性触发，可能获得修为或悟得技能 |

---

## 3. 核心组件

### 3.1 服务层

**TrainingService** — 历练核心服务 + 结算编排
- `startTraining(userId)` / `startTrainingInternal(userId)` - 开始历练
- `endTraining(userId)` / `endTrainingFlow(userId)` - 结束历练（短事务内结算与落库 → 事务提交后 LLM 叙述）
- 修为计算（地图等级 / 悟性 / 身法效率 / 等级衰减 / 运势）、物品掉落判定、中断处理、结算输出组装

**TrainingSettler** — 统一历练事件循环，`TrainingService` 与 `UserStateService`（中途结算）共用
- `settleChunk(userId, user, mapNode, fromMinute, toMinute)` - 对一段未结算时间执行事件循环
- 读 `activity_event`（TRAINING + map_id）统一池 → COMBAT 走 `CombatEventHandler`，NUMERIC 走 `TrainingCompleter.handleNumericEvent`，CHOICE 写入 `game_event`
- 返回 `SettlementResult`（`CombatSummary` + 灵兽是否参战）

**CombatEventHandler** — 单次遇怪处理
- 从 `ActivityEvent.params` 读取 `monster_template_id` / `min_count` / `max_count`
- 委托 `CombatService`（CombatEngine）执行回合战斗
- 战后：HP 回写、掉落分发、顿悟判定（可能悟得技能）、灵兽休养/觉醒
- 每场战斗前玩家恢复 5% 最大气血

**EncounterCalculator** — 动态间隔计算
- `compute(userId, user, mapNode, durationMinutes)` → `EncounterParams(slots, perRollChance)`
- 公式：`interval = (12 - richness) × levelMismatch / mapDanger`，clamp [3, 20]

**TrainingCompleter** — 叙事产出 + 子事件/隐藏事件代理
- `produceCompletionEvent()` / `produceInterruptedEvent()` / `checkHiddenEvents()` / `applyEnvironmentalEvents()`
- 完成通知文案：无收益时为「你在{地图}历练了 N 分钟，本次未遭遇战斗，无收益。」

**SubEventEffectExecutor** — NUMERIC 事件效果引擎
- 读 `params.effects` JSONB 数组（或 `branches` 分支）→ 分发到 effect handler（策略模式）
- 效果类型：ADD_EXP / ADD_EXP_PERCENT / TAKE_DAMAGE_PERCENT / TAKE_DAMAGE_FLAT / HEAL_FLAT / ADD_ITEM / ADD_RANDOM_ITEM / CREATE_EQUIPMENT / DROP_SPECIALTY / ADD_SPIRIT_STONES / TAKE_SPIRIT_STONES / MULTIPLY_BOUNTY_REWARD / PURE_NARRATIVE

**TrainingSettlementHandler** — 定期中途结算处理器（`StateHandler`，@Order(5)）
- 用户状态为 TRAINING 且距上次结算 ≥ 60 分钟时触发
- 结算后推送通知（有战斗：「你在{地图}已修炼 N 分钟，期间遭遇 M 场战斗，获得 +E 修为，继续精进中。」；无战斗：「你在{地图}已修炼 N 分钟，继续精进中。」）

**DropProcessor** - 掉落处理器
- `processMonsterDrops(MonsterTemplate tmpl, Long userId)` - 从怪物模板计算掉落
- 掉落统一经 `RewardGrant.grant()` 分发（装备实例化、其余堆叠入包）

详见 [战斗系统 design](../combat/design.md)（原 `docs/战斗系统.md`）。
遇怪公式见本文件 §5.1 与 [地图旅行 design](../map-travel/design.md#63-动态遇怪事件间隔)。

---

## 4. 数据结构

`TrainingRewardVO` 字段：`userId`、`mapId`、`mapName`、`durationMinutes`、`efficiencyMultiplier`、`levelDecayMultiplier`、`spiritStones`（保留字段，当前不发放）、`exp`、`items`、`summary`（可为 LLM 美化后的叙述文本）。

结算内部奖励包含：修为、物品掉落、战斗统计（遇敌场次、击杀数、战败数、总回合数、顿悟次数、掉落汇总、战斗日志、技能触发统计、高光/战败记录）。灵兽获得独立修为当前**未实现**（`BeastCombatService.addExpToDeployedBeasts` 无调用方）。

战斗统计详见 [战斗系统 design](../combat/design.md)。

---

## 5. 计算公式

### 5.1 动态遇怪/事件间隔

```
interval = baseInterval × levelMismatch / mapDanger
```

- `baseInterval` = 12 - map.encounter_richness → 2~11 分钟（地图配置）
- `mapDanger` = 1 + (mapLevel - 1) × 0.015 → 1.0~2.5（高级图更凶险）
- `levelMismatch` = 1 + |playerLevel - mapLevel| / max(mapLevel, 1) × 2.5 → 1.0~3.5（等级匹配时密集，偏离时稀少）

**最小间隔：** 3 分钟　　**最大间隔：** 20 分钟

**设计意图：**
- 地图越凶 → 越频繁（mapDanger）
- 等级越匹配 → 越频繁（levelMismatch 接近 1）
- 高手去低级地图 → 几乎碰不到事件（levelMismatch 惩罚大）
- 新手去高级地图 → 也碰不到（同样惩罚大）
- 装备不参与间隔计算 → 装备的作用是打赢，不是躲开

**per-roll 触发概率：**
```
perRollChance = min(1.0, 0.4 × 10 / interval)
slots = duration / interval（向下取整）
实际触发概率 = min(1.0, perRollChance × 机缘修正)
```

机缘（fate）修正：`getFateMultiplier(fate) = 0.7 + fate / 166`（0.7~1.302）。

**示例：**

| 场景 | richness | interval | 60分钟slots | 期望触发 |
|------|----------|----------|------------|---------|
| Lv1玩家 Lv1图 (5) | 5 | 7.0 | 8 | ~4.5 |
| Lv50玩家 Lv50图 (7) | 7 | 4.0 | 15 | ~15 |
| Lv50玩家 Lv10图 (5) | 5 | 16.0 | 3 | ~0.8 |
| Lv10玩家 Lv50图 (5) | 5 | 17.5 | 3 | ~0.7 |

### 5.2 等级衰减机制

```
expMultiplier = max(0.1, 1 - max(0, (playerLevel - mapLevel - 5)) × 0.04)
```

- 玩家等级 ≤ 地图等级 + 5：无衰减（100% 修为）
- 玩家等级 > 地图等级 + 5：每高 1 级衰减 4%
- 最低衰减到 10%（保留保底修为）

示例：玩家等级 50，地图等级 30 → 等级差 = 50 - 30 - 5 = 15 → 衰减 = 15 × 0.04 = 0.60 → `expMultiplier` = 0.40（40% 修为）。

### 5.3 基础修为

```
baseExpPerMinute = max(mapLevel × 5, floor(sqrt(有效悟性) × 12))
baseExp = baseExpPerMinute × 未结算分钟数 × 效率倍率 × 等级衰减 × 机缘修正
```

- 机缘修正：`getLuckMultiplier(luck) = 0.8 + luck / 250`（0.8~1.2）
- 击杀修为另计：`exp_reward × 怪物数量 × 等级修正`，等级修正 = `clamp(1 + (怪物等级 - 玩家等级) × 0.05, 0.1, 3.0)`
- 所有修为入账受修为存储上限截断（`Player.addExp`）

### 5.4 效率倍率

```
efficiencyMultiplier = 1.0 + min(有效身法 × 0.01, 2.0)
```

即最高 3.0x，展示为「效率N.Nx」。

### 5.5 特产掉落判定

```
dropChances = max(1, floor(min(未结算分钟数, 720) / 10.0 × efficiencyMultiplier))
```

- 单次结算按分钟数封顶 720 分钟（12 小时），防止超长离线滚动数千次
- 每次判定从地图 `specialties` 按 weight 加权选取一个模板，数量 1~3，同名合并
- 无特产配置时不掉落

---

## 6. 结算节奏

| 规则 | 说明 |
|------|------|
| 开始门槛 | 状态 IDLE + 当前地图为 TRAINING_ZONE |
| 结算门槛 | 状态 TRAINING 或 DYING |
| 最短时长 | ≤ 5 分钟：提示「历练时间过短毫无收获」，清除活动并回空闲 |
| 无开始时间 | 提示「您当前没有在历练」，清除活动 |
| 中途结算 | 每 60 分钟一次，懒结算（下次加载用户状态时执行），推送通知 |
| 增量结算 | 只结算 `last_settlement_minute` 之后的未处理分钟数 |
| 单次封顶 | 物品掉落判定按 720 分钟封顶 |
| 濒死 | 状态 DYING 也可执行 `历练结算`；结算后保留濒死状态 |

---

## 7. 中断条件

```
玩家 HP ≤ 0 → 进入濒死状态，事件循环立即中断
              已获得的修为/掉落照常发放
灵兽阵亡 → 取消出战并进入按品质计时的休养期
灵兽全灭 → 后续战斗按剩余可战灵兽继续（solo），不再补位
```

濒死结算后状态保留 DYING，活动清除；30 分钟后由 `DyingRecoveryHandler` 自动恢复（气血恢复到 20% 最大气血，至少 1）。结算文案附：「你力战至脱力昏迷，30 分钟后自愈苏醒。重伤前所获之物已收入囊中。」

---

## 8. 结算输出格式

**设计原则：** 文字 MUD 挂机游戏，输出要简洁，只统计数据。

**输出策略：**
- **普通战斗**：只统计数据，不输出详细过程
- **叙事美化**：结算时调用 LLM 将本次经历改写为仙侠旁白；失败时由叙述模块兜底为通用描述，不阻塞结算
- **高光战斗**：每次历练最多 1 场战斗的日志进入高光渲染

**结算回复结构（代码实际格式）：**

```
【历练结算】幽暗沼泽 | 30分钟 | 效率1.2x（| 衰减N%）
<summary：LLM 美化叙述或结算原文>
  修为+460
  物品：灵草×5 兽骨×2 破损的木剑×1
```

- 摘要（summary）包含：历练时长、修为、遇敌统计（`遇敌8场 | 击杀22只 | 战败1场`）、物品列表、战败叙述等
- 修为为 0 且无物品时追加「本次未遭遇战斗，暂无收益。」
- 无开始时间 / 时长过短 / 地图缺失的提前退出路径只返回单行摘要文本，不带标题行

**高光战斗示例（LLM 渲染）：**

```
【历练结算】幽暗沼泽 | 30分钟 | 效率1.2x
遇敌8场 | 击杀22只 | 战败1场
修为+460 | 灵草×5 兽骨×2 破损的木剑×1

━━━ 高光时刻 ━━━
第5场战斗，你遭遇了一只剧毒蟾蜍王。……
```

**高光战斗触发条件（三者其一）：**
- 战斗回合数 ≥ 10
- 玩家方任一成员战后气血比例 ≤ 30%（势均力敌）
- 技能触发记录（不同出手者+技能组合）≥ 3 条（技能纷呈）

---

## 9. 顿悟

每次获胜战斗后判定：

```
chance = 0.02 + 有效悟性 × 0.0005
```

触发后按权重分支：

| 分支 | 概率 | 效果 |
|------|------|------|
| 灵光一闪 | 50% | 修为 +（3%~8% 升级所需） |
| 心有所感 | 30% | 尝试随机习得可学技能；失败则修为 +（1%~3% 升级所需） |
| 天机乍现 | 15% | 修为 +（8%~20% 升级所需），并尝试习得技能 |
| 天人交感 | 5% | 修为补足至多半级（受存储上限约束），并尝试习得技能 |

顿悟通过 `game_event` 通知推送。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **状态名**：`EXERCISING` → `TRAINING`（`UserStatus.TRAINING`，显示「历练」）。

### B. 保留代码设计

- **无灵石奖励**：历练产出修为与物品，灵石由悬赏/世界事件/回收等产出——玩法分工清晰，避免挂机成为无限货币来源；spec 已明确「MUST NOT 发放灵石或铜币」，`TrainingRewardVO.spiritStones` 为兼容保留字段。
- **等级衰减起点**：与文档 §6.4 公式一致（§2 表格「15 级」为笔误）；+5 级起衰减更早抑制高等级玩家无风险刷低级图。
- **中途自动结算**：每 60 分钟懒结算并推送通知（增量结算不重复发放），挂机期间有阶段反馈与击杀修入账，不必等最终结算才知道进展。
- **最短时长与提前退出**：≤5 分钟判定无收获并清活动，防止误触秒结算刷判定；无开始时间/地图缺失各有独立文案，反馈明确。
- **720 分钟封顶**：物品掉落判定按 12 小时封顶（防超长离线滚动数千次），修为仍按实际时长照常发放，对正常玩家无影响。
- **修为公式与效率倍率**：悟性决定基础修为、身法决定效率（上限 3.0x）、机缘修正收益，三条养成线都有意义；与 spec「悟性与地图等级取高」一致。
- **事件循环含 CHOICE**：统一事件池除 COMBAT/NUMERIC 外含 CHOICE，选择事件以可交互通知送达，挂机历练也能参与决策。
- **结算输出**：LLM 美化 + 结构化收益（时长/效率/衰减/修为/物品/战斗统计），失败兜底不阻塞结算，反馈比固定格式更完整。
- **濒死恢复**：玩家濒死立即中断但已获收益照发，30 分钟后自动恢复 20% 气血，阵亡灵兽按品质休养——代价可承受，无永久损失与卡死。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

无。

> 已实现（本轮）：出战灵兽随历练获得「分钟 × 2」修为；战斗胜利按「怪物等级 × 10」追加；练功房 +3%/级 与师徒加成一并接入修为乘数（统一一次取整）。

### E. 缺陷修复

- **高光战斗判定**：文档「双方血量都低于 30%」过严、「同一技能触发 ≥3 次」与「稀有」语义相反；现实现已修为「回合 ≥10、玩家方任一成员残血 ≤30%、技能触发组合 ≥3」三者其一（与战斗系统 design 一致）——已修。
