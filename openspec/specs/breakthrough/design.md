# 突破雷劫系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 设计原则

- **大境界突破 = 战斗**：跨大境界突破和渡劫期内每一级，触发雷劫战斗
- **小境界突破 = RNG**：同一大境界内升级，保持原有概率骰子逻辑
- **复用战斗引擎**：雷劫 Boss 使用 `CombatService.simulate()`，与历练/秘境/PVP 共用同一引擎
- **丹药/护道转化为 Boss 削弱**：突破辅助丹药和护道者的 RNG 加成，在战斗场景中转化为雷劫 Boss 的属性削弱

---

## 2. 突破流程

### 2.1 小境界突破（RNG）

```
修为达标 → RNG骰子（基础率 + 保底 + 护道 + 丹药）→ 成功/失败
```

### 2.2 跨大境界 + 渡劫期（战斗）

```
修为达标 → 校验可出战单位 → 扣除修为 → 随机雷劫类型 → 生成雷劫Boss → 战斗（40回合）→ 胜=突破 / 败=失败
```

### 2.3 前置校验与资源扣除

- `attemptBreakthrough()` **不检查玩家当前状态**，任何状态下均可发起突破
- 修为不足：直接返回失败文案「修为不足，突破需要 X 修为，当前仅有 Y 修为」，不做任何扣减
- 大境界/渡劫期路径先构建玩家队伍（玩家 + 出战灵兽），若无可出战单位则返回「⚠️ 没有可出战的单位，雷劫无法降临」，**不扣修为、不计失败**
- 修为在战斗开始前扣除（无论胜败）

---

## 3. 境界命名（CultivationRealm）

「阶段制」（初窥/凝气/化液/圆满）已改为「每层独立命名」，每级 `realmDisplay()` 唯一，格式 `大境界名 · 层名`（如 `"炼气期 · 启灵"`、`"渡劫期 · 三劫"`）。

| 大境界 | rank | 等级区间 | 各层名称 |
|--------|------|----------|----------|
| 炼气期 | 0 | 1-10 | 启灵、引气、炼息、凝旋、聚海、通玄、化液、淬液、冲关、圆满 |
| 筑基期 | 1 | 11-20 | 开光、辟府、筑台、炼神、通脉、融合、化罡、洗髓、问心、圆满 |
| 金丹期 | 2 | 21-30 | 凝丹、养丹、固丹、淬丹、通灵、孕神、丹纹、丹火、碎丹、圆满 |
| 元婴期 | 3 | 31-40 | 孕婴、育婴、凝婴、开窍、固婴、化形、出窍、分神、感道、圆满 |
| 化神期 | 4 | 41-55 | 化神、凝神、锻神、化念、御神、合神、通幽、洞玄、知命、化道、悟真、证道、执道、感虚、圆满 |
| 炼虚期 | 5 | 56-70 | 初虚、入虚、窥虚、破虚、游虚、辟界、养界、定界、演界、掌界、破界、融界、归墟、化墟、圆满 |
| 合体期 | 6 | 71-90 | 合天、合地、合人、合道、融天、融地、融道、天心、地脉、人道、三才、归一、无我、有我、化身、执天、万象、超脱、望劫、圆满 |
| 大乘期 | 7 | 91-110 | 渡己、渡人、渡世、慈悲、般若、菩提、涅槃、因果、轮回、无相、空明、圆觉、法相、金身、天眼、宿命、漏尽、飞升、劫临、圆满 |
| 渡劫期 | 8 | 111+ | 一劫、二劫、三劫、四劫、五劫、六劫、七劫、八劫、九劫、飞升劫；第 11 层起按公式生成「第N劫」 |

- 共 120 个独立层名（渡劫期前 10 层有独立命名，之后走公式）
- `isMajorBreakthrough(old, new)`：`fromLevel(old) != fromLevel(new)`
- 大境界突破成功奖励常量：`MAJOR_BREAKTHROUGH_STAT_PERCENT = 20`，`MAJOR_BREAKTHROUGH_SPIRIT_STONES_BASE = 2000`，灵石奖励 = `(rank + 1) × 2000`

---

## 4. 雷劫类型（TribulationType）

共 7 种雷劫，按目标大境界 rank 解锁 + 概率加权：

| 类型 | 难度 | 最低解锁（rank） | 权重 | 技能（冷却/效果） |
|------|------|-----------------|------|-------------------|
| 三清雷劫 | ×1.0 | 筑基（1） | 0.55 | 无 |
| 紫霄神雷 | ×1.3 | 筑基（1） | 0.22 | 「紫霄贯体」CD 3：DAMAGE `attack*1.8` + EXECUTE（阈值 0.3，公式 `attack*1.5`） |
| 太乙青雷 | ×1.3 | 金丹（2） | 0.12 | 「青雷蚀骨」CD 4：DOT（每回合 0.15×攻击，3 回合，最多 3 层） |
| 九霄金雷 | ×1.6 | 化神（4） | 0.07 | 「金雷破罡」CD 4：ARMOR_BREAK(0.3, 3 回合) |
| 玄冥黑雷 | ×1.6 | 炼虚（5） | 0.03 | 「冥雷封魂」CD 5：FREEZE(2 回合) |
| 灭世神雷 | ×2.0 | 大乘（7） | 0.009 | 「灭世雷暴」CD 5：DAMAGE `attack*1.5` + AOE_DAMAGE `attack*1.3` |
| 九色神雷 | ×3.0 | 渡劫（8） | 0.001 | 「九色天罚」CD 4：DAMAGE `attack*2.5` + DOT(0.1, 3 回合, 5 层) + ARMOR_BREAK(0.2, 3 回合) + FREEZE(1 回合) |

境界解锁规则（按目标大境界 rank 过滤 `minRealmOrdinal <= rank`）：

- 目标筑基(1)：三清 + 紫霄
- 目标金丹(2)~元婴(3)：三清 + 紫霄 + 太乙（太乙在金丹解锁）
- 目标化神(4)：+ 九霄
- 目标炼虚(5)~合体(6)：+ 玄冥
- 目标大乘(7)：+ 灭世
- 目标渡劫(8)：+ 九色（全部 7 种）
- 渡劫期内部每一级（isTribulationRealm=true）：全部 7 种

选择算法（`randomForBreakthrough(targetRealmOrdinal, isTribulationRealm)`）：

- 候选 = `isTribulationRealm || minRealmOrdinal <= targetRealmOrdinal` 的类型
- 候选为空时兜底三清雷劫
- 按权重累加随机：`roll = random × 总权重`，取累计权重 ≥ roll 的第一个类型，兜底最后一个
- 技能列表由 `buildSkills()` 在内存构造（不写 DB）

---

## 5. Boss 数值公式

基于玩家队伍属性缩放（`TribulationBoss.forPlayerBreakthrough`）：

```
hpRate  = (1.5 + targetRealmOrdinal × 0.5) × type.difficultyMultiplier
atkRate = (0.8 + targetRealmOrdinal × 0.3) × type.difficultyMultiplier
defRate = (0.6 + targetRealmOrdinal × 0.25) × type.difficultyMultiplier
spdRate = (0.7 + targetRealmOrdinal × 0.2) × type.difficultyMultiplier
```

渡劫期每级额外难度（`tribulationLevel = newLevel − 渡劫期起始等级 + 1`）：

```
levelMultiplier = 0.5 + tribulationLevel × 0.5
hpRate  ×= levelMultiplier
atkRate ×= levelMultiplier
defRate ×= levelMultiplier × 0.7
spdRate ×= levelMultiplier × 0.7
```

削弱因子（独立相乘）：

```
combinedReduction = (1 - bossReduction)               // 丹药+护道 (0~0.5)
                  × (1 - pityReduction)               // 保底 (0~0.5，每次失败 +5%)
                  × max(0.1, 1 - tribulationResist)   // 雷劫抗性buff（上限 0.9）
```

- `bossReduction = clamp((突破丹药加成 + 护道加成) / 100, 0, 0.5)`（负加成不得反向增强雷劫）
- `pityReduction = min(0.5, 突破失败次数 × 0.05)`
- `tribulationResist = min(0.9, Σ活跃 TRIBULATION_RESIST buff 值 / 100)`

最终 Boss 属性 = 队伍统计 × 对应 rate × combinedReduction：

- `totalMaxHp` = 存活成员最大气血之和；`avgAttack/avgDef/avgSpeed` = 存活成员均值（至少按 1 人）
- 队伍由 `CombatService.buildPlayerTeam(user)` 构建（玩家 + 出战灵兽）
- Boss 命名：`天劫化身 · <雷劫类型>` + ` (一劫/…/飞升劫/第N劫)` 或 ` (N阶劫)`；Boss 队伍名为「天劫」
- 战斗回合上限 40；胜利判定为 `winner == "Player"`

另有福地渡劫构造（`TribulationBoss(defendingTeam..., tribulationStage, compassionMode)`）：`hp ×= 1+stage×0.5`、`atk ×= 0.6+stage×0.15`、`def ×= 0.4+stage×0.10`、`spd ×= 0.7+stage×0.08`，怜悯模式四项 ×0.7，与玩家突破雷劫互不影响。

---

## 6. 丹药体系

### 6.1 突破辅助丹（原有，仅小境界突破有效）

筑基丹/结丹丹/碎丹丹/化婴丹/炼神丹/化神丹/融虚丹/大乘丹：按原 RNG 逻辑加成成功率（`player_buff` 表 `breakthrough` 类型，持续 1 小时，同类最多叠 3 层）。跨大境界/渡劫期战斗中，突破丹药加成改为 Boss 削弱（见第 5 节）。

### 6.2 雷劫抗性丹（`tribulation_resist`）

| 丹药 | 效果 | 品级（tags） |
|------|------|--------------|
| 避雷丹 | tribulation_resist +15% | basic |
| 天劫丹 | tribulation_resist +25% | epic |
| 化劫丹 | tribulation_resist +40% | rare |
| 渡厄金丹 | tribulation_resist +60% | legendary |
| 招雷散 | tribulation_resist −40% | rare |

- 持续时间 3600 秒（丹药效果 `duration_seconds`）
- 实际 buff 数值受丹药成色倍率与品级衰减影响（`PillConsumptionService.applyBuff`：`amount × 成色倍率 × 品级衰减`，衰减系数 `GRADE_DECAY_COEFFICIENT = 0.2`，保底 0.1）
- 招雷散负值允许写入（`player_buff` CHECK `value >= -100`；`PillConsumptionService` 对 `TRIBULATION_RESIST` 放行非正值）
- 突破后清除 `breakthrough` 类 Buff；`tribulation_resist` 类 Buff **不清除**，到期自然过期

### 6.3 Buff 类型扩展

`PlayerBuffType` 含 `TRIBULATION_RESIST("tribulation_resist", "雷劫抗性")`；DB CHECK 约束同步（`player_buff.buff_type IN ('attack','defense','speed','breakthrough','tribulation_resist')`）。

---

## 7. 战后处理

### 7.1 战胜

- 等级提升（`level = newLevel`）
- 失败计数归零
- 气血回满
- 跨大境界：有效四维各 +20%，并发放灵石 `(rank+1) × 2000`
- 渡劫期每级（非跨大境界）：有效四维各 +5%
- 清除全部 `breakthrough` Buff 与被护道关系（战斗结束后、结算前已执行）
- 检查师徒出师（`masterApprenticeService.checkAndGraduate`）
- 回复文案：LLM 战斗叙事 + 属性/灵石奖励摘要

### 7.2 战败

- 修为已在战前扣除
- 失败计数 +1（提升后续保底削弱）
- 清除全部 `breakthrough` Buff 与被护道关系
- 回复文案：LLM 战败叙事

### 7.3 灵兽

- 雷劫战斗中出战灵兽参战（HP > 0 且未休养，上限 2 只）
- 当前实现**未将战斗后的玩家/灵兽气血写回持久层**（仅保存原状态）；文档所述「复用 `applyCombatHpToBeasts()`」属于 `PostCombatProcessor`，但突破流程未调用

### 7.4 LLM 叙事

战斗结束后由 `TribulationNarrativeGenerator.generateCombatNarrative()` 生成 AI 战斗过程描述：

- 输入：雷劫类型、道号、胜负、回合数、玩家/灵兽气血变化、技能触发统计、关键事件（伤害/击杀/重创）
- 输出：50-100 字仙侠古风叙事；失败时回退默认文案「雷劫已渡，道行精进！」/「雷劫降临，未能抵挡天劫化身……境界突破失败！」
- 跨大境界 RNG 成功另有贺词生成（40-80 字），失败时回退境界自带贺词

---

## 8. 雷劫预报（TribulationForecast）

修为足以尝试跨大境界/渡劫期突破时，随「状态」展示情报面板；否则返回 null（状态改为显示小境界成功率）。

- 展示：目标大境界名、候选天劫列表、突破丹药加成、护道加成、雷劫抗性（上限 90%）、历史失败次数
- 候选列表规则与 `randomForBreakthrough` 一致；渡劫期显示全部 7 种
- **不透露概率数值**——天数难测，唯备战可恃
- 文案包含备战引导：服突破丹削雷威、邀同图护道者、炼雷抗丹

---

## 9. 文件改动清单（历史记录，按代码现状修正）

| 文件 | 改动类型 | 说明 |
|------|---------|------|
| `domain/user/enums/CultivationRealm.java` | 重构 | 阶段制 → 每层独立命名（120 个名称，渡劫期第 11 层起按公式） |
| `domain/user/enums/TribulationType.java` | 新增 | 7 种雷劫类型 + 境界解锁 + 技能规格 |
| `domain/pill/enums/PlayerBuffType.java` | +1 值 | TRIBULATION_RESIST |
| `domain/monster/TribulationBoss.java` | 新增工厂 | `forPlayerBreakthrough()` + 技能生成；保留福地渡劫构造 |
| `service/cultivation/CultivationService.java` | 重构 | 分流：小境界 RNG vs 大境界/渡劫战斗 + 雷劫预报 |
| `service/combat/CombatService.java` | 扩展 | `calculateTeamStats()`、`buildPlayerTeam()` |
| `handle/command/CultivationCommandHandler.java` | 扩展 | 战斗突破结果格式化（雷劫详情 + 气血变化） |
| `domain/user/vo/BreakthroughResult.java` | 扩展 | 新增 battleResult + tribulationTypeName 字段 |
| `service/pill/PillConsumptionService.java` | 修正 | 允许 TRIBULATION_RESIST 负值（招雷散） |
| `service/cultivation/TribulationService.java` | 重构 | 福地渡劫改用 CombatService 共享方法 |
| `db/migration/V1.0.25.4__seed_item_template_potion.sql` | 修正 | 天劫丹属性修正 + 新增 5 种雷劫丹 |
| `db/migration/V1.0.21__create_player_buff.sql` | 新增 | DB CHECK 约束更新（含 tribulation_resist） |

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **境界层名数量**：实际 120 个（炼气/筑基/金丹/元婴各 10、化神/炼虚各 15、合体/大乘各 20、渡劫期 10 层独立命名 + 「第N劫」公式），文档「110 个」已作废。
- **雷劫抗性上限**：代码先把抗性本身 clamp 到 0.9 再参与 `max(0.1, 1 - resist)`，与文档下限公式效果等价，且与预报面板 90% 上限一致。
- **雷劫技能具体数值**：紫霄 EXECUTE 为 `attack*1.5`（阈值 0.3）、灭世含 DAMAGE `attack*1.5`、九色为 DAMAGE `attack*2.5` + DOT(0.1, 3 回合, 5 层) + ARMOR_BREAK(0.2, 3 回合) + FREEZE(1 回合)。
- **迁移文件版本**：实际为 `V1.0.25.4__seed_item_template_potion.sql` 与 `V1.0.21__create_player_buff.sql`。
- **突破丹药堆叠**：同类活跃层数上限 3（`MAX_ACTIVE_BUFFS_PER_TYPE`），实际数值受丹药成色与品级衰减（`GRADE_DECAY_COEFFICIENT = 0.2`）。
- **大境界奖励数值**：+20% 基于有效四维取整后累加，灵石 = `(rank+1) × 2000`。

### B. 保留代码设计

- **渡劫期防御/速度额外 ×0.7**：高劫数只让气血与攻击同步膨胀，防御/速度慢半拍，避免 40 回合内打不动或全程被先手压制，保证后期渡劫可完成。
- **雷劫预报面板**：只给候选天劫与削助手段、不泄露概率，把「备战」变成可见的正反馈，引导服丹与请护道，契合「天数难测，唯备战可恃」。
- **渡劫期层级奖励只在非跨大境界时 +5%**：进入渡劫期（111 级）属于跨大境界，走 +20% 与其它大境界一致，不会成为唯一一个奖励缩水的里程碑。
- **无出战单位不扣修为、不计失败**：灵兽可能阵亡或休养，玩家并无过错；拒绝渡劫但不施加惩罚，避免白扣修为与失败计数。

### C. 按设计修正（待修）

无。

### D. 未实现（待办）

- **招雷散「战胜额外 +50% 修为」**：设计意图——负抗性丹药以更高雷劫难度换取额外修为，构成风险/收益选择；现状——负抗性只增强 Boss、没有任何补偿，服丹是纯负面，建议实现该奖励或重做数值定位。
- **战后气血与灵兽写回**：设计意图——战斗气血回写玩家与灵兽，战败可能残血/濒死，与历练战斗一致（`applyCombatHpToBeasts`）；现状——`CultivationService` 战斗后只保存原状态，战斗内气血变化被丢弃（胜则回满、败则维持战前气血），灵兽不受伤、不休养；若要实现需先权衡失败三重代价（修为 + 失败计数 + 气血）是否过重。

### E. 缺陷修复

无。
