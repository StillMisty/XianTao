# 战斗系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 设计原则

- **懒结算**：所有战斗均为后台模拟，无实时交互、无后台任务
- **通用引擎**：一套引擎覆盖所有战斗场景（历练 / 秘境 / PVP / 悬赏 / 雷劫）
- **Team vs Team**：不关心具体类型，只操作 `Combatant` 接口

---

## 2. 服务层架构

### 2.1 职责划分

```
TrainingService（历练编排）
├── 修为计算（地图等级 / 悟性 / 身法效率 / 等级衰减 / 运势）
├── 调用 TrainingSettler 统一事件循环（COMBAT/NUMERIC/CHOICE）
├── 战后 HP 回写（PostCombatProcessor）
├── 灵兽休养/觉醒判定
├── 掉落分发到背包（RewardGrant）
└── LLM 历练叙事（事务提交后）

TrainingSettler（统一历练事件循环，TrainingService 与 UserStateService 共用）
├── EncounterCalculator.compute() → slots + perRollChance
├── 加权选择事件 → CombatEventHandler / CHOICE 事件 / NUMERIC 事件
└── CombatSummary 累加

CombatEventHandler（单次遇怪战斗）
├── 构建玩家队伍（含出战灵兽）与怪物队伍
├── CombatService.simulate() → BattleResultVO
├── 高光检测（HighlightBattleDetector）
├── 掉落（DropProcessor.processMonsterDrops）
├── 战后 HP 回写 + 灵兽休养/觉醒（PostCombatProcessor）
└── 顿悟判定（EnlightenmentProcessor）

CombatService（单场战斗，纯引擎）
├── simulate(CombatTeam, CombatTeam, int) → BattleResultVO
├── buildPlayerTeam(User, ...) → CombatTeam
└── calculateTeamStats(CombatTeam) → TeamStats
```

### 2.2 关键组件（代码现状）

| 组件 | 位置 | 职责 |
|------|------|------|
| `CombatService` | service/combat/ | 单场战斗模拟 + 队伍构建 + 队伍属性统计，无遇敌编排 |
| `TrainingService` | service/combat/ | 历练编排：起止、修为/物品收益、叙事 |
| `TrainingSettler` | service/combat/ | 统一事件循环（战斗/数值/选择事件），供历练与状态结算共享 |
| `CombatEventHandler` | service/combat/ | 单次遇怪战斗编排（队伍、掉落、战后、顿悟） |
| `DropProcessor` | service/ | 怪物掉落表解析（`DropTableEntry`） + 独立概率判定 |
| `PostCombatProcessor` | service/combat/ | 纯内存操作：玩家/灵兽 HP 回写、灵兽休养、觉醒判定 |
| `EncounterCalculator` | service/combat/ | 动态遇怪间隔/概率计算 |
| `HighlightBattleDetector` | service/combat/ | 高光战斗检测 |
| `DamageCalculator` | service/combat/ | 伤害计算（普攻/技能/法器克制/公式求值） |
| `EffectHandlerRegistry` | service/combat/ | `EffectType → EffectHandler` 注册表 |
| `ReactiveEffectProcessor` | service/combat/ | 受击反应层：冰冻易伤/闪避/反伤/反击 |
| `DefaultTeamBuilder` | service/combat/ | 玩家队伍构建（法器/法决/丹药 Buff/出战灵兽） |
| `DefaultTargetSelectionStrategy` | service/combat/ | 集火当前气血比例最低的存活目标 |
| `DefaultSkillSelectionStrategy` | service/combat/ | 从已冷却技能中随机选择 |
| `DropItem` / `DropItem.DropType` | domain/monster/vo/ | 强类型掉落记录 |
| `CombatLogEntry` | domain/monster/vo/ | 强类型战斗日志 Record |
| `CombatSummary` / `EncounterResult` | service/combat/ | 多场战斗统计累加 / 单次遇怪结果 |
| `TypeUtils` | infrastructure/util/ | 共享 `toLong()` 等解析工具 |
| `BeastQuality.recoveryMinutes` | domain/fudi/enums/ | 灵兽品质含恢复时间，消除 `switch` 字符串 |

---

## 3. 核心架构

### 3.0 战斗实体（Battle）

```
Battle（领域实体）
├── teamA → 攻击方队伍（CombatTeam）
├── teamB → 防御方队伍（CombatTeam）
├── scene → 战斗场景（BattleScene）
├── maxRounds → 最大回合数
├── of(teamA, teamB, scene, maxRounds) → Battle   // 工厂方法
├── execute(CombatEngine) → BattleResultVO         // 内部构造 BattleContext 并委托引擎
├── isTeamAWin() → boolean                         // 攻击方是否胜
└── isTeamADead() → boolean                        // 攻击方是否全灭
```

泛型设计：只依赖 `Combatant` 接口和 `CombatTeam`，不关心具体类型（玩家/灵兽/怪物）。
战后结算（HP 回写、掉落、灵兽休养）由调用方（Service 层）处理。`execute()` 带记忆化（executed 标记）。

### 3.1 战斗引擎接口（CombatEngine）

```
CombatEngine（接口）
├── simulate(BattleContext) → BattleResultVO
```

**实现类：**
- `DefaultCombatEngine` — 默认 PVE 战斗引擎（所有场景共用）

### 3.2 战斗上下文（BattleContext）

```
BattleContext
├── teamA → 攻击方队伍
├── teamB → 防御方队伍
├── maxRounds → 最大回合数（默认 20）
└── scene → 战斗场景（TRAINING/DUNGEON/PVP/BOUNTY）
```

> 文档早期版本中的 `mapId/mapLevel/playerLevel/gearScore` 已不在 BattleContext 中（遇怪计算移至 `EncounterCalculator`，装备评分机制已移除）。

### 3.3 战斗单位（Combatant）

```
Combatant（接口）
├── getId()            → 唯一标识
├── getName()          → 名称
├── getSpeed()         → 行动速度（决定出手顺序）
├── getAttack()        → 攻击力（已包含所有加成）
├── getDefense()       → 防御力
├── getHp() / getMaxHp() → 气血
├── takeDamage(amount) → 受到伤害
├── heal(amount)       → 恢复气血
├── isAlive()          → 是否存活
├── getSkills()        → 已装载法决列表
├── getAttackSpeed()   → 攻速（影响技能冷却回合数）
│
├── PlayerCombatant    → 玩家 + 法器 + 法决槽位
│   ├── getWeaponType()       → 法器类型（克制判定用）
│   ├── getWis()/getStr()/getAgi() → 技能公式变量
│   ├── withBuffs(attackBuff, defenseBuff, speedBuff) → 加载丹药 Buff
│   ├── 攻击 = 有效力道 × 2 + 法器攻击 + 攻击 Buff
│   ├── 防御 = 有效根骨 + 防御 Buff
│   └── 速度 = 有效身法 × 2 + 10 + 速度 Buff
│
├── BeastCombatant     → 灵兽
│   ├── 攻防速由品质 + 等级自动推算，并乘突变特质倍率
│   ├── skills 来自 beast.skills（已启用）
│   └── 在战斗中与其他 Combatant 行为一致，无特殊 AI
│
├── Monster            → 怪物
│   ├── 由 MonsterTemplate + 等级缩放生成
│   ├── 缩放公式：实例属性 = 模板基准属性 × (1.0 + (level - baseLevel) × 缩放系数)
│   │       hp/attack: ×0.15, speed/defense: ×0.1（构造时 hp 有 0.1 缩放下限保护）
│   ├── 每个 Monster 实例拥有全局唯一 ID（AtomicLong 自增），消除同模板多怪 Buff 错位
│   └── skills 来自模板配置
│
└── TribulationBoss    → 雷劫/天劫 Boss（见 breakthrough 设计）
```

> 所有 Combatant 子类在战斗中行为一致：按速度排序出手，有技能则从可用技能中**随机**选取，无技能则普攻。
> 灵兽、怪物、玩家之间不做差异化处理。

### 3.4 战斗团队（CombatTeam）

```
CombatTeam
├── ownerId            → 所属者（用于掉落归属）
├── name               → 团队名称（胜负判定使用；玩家方默认 "Player"）
├── members[]          → Combatant 列表
├── aliveMembers()     → 存活成员
├── isAllDead()        → 是否全灭
├── aliveCount()       → 存活数量
├── selectTargetForPVE() → PVE目标选择（集火低气血比例）
└── selectTargetRandom() → 随机目标选择
```

---

## 4. 战斗流程

### 4.1 通用战斗流程（DefaultCombatEngine.simulate）

```
DefaultCombatEngine.simulate(context):
  teamA = context.teamA
  teamB = context.teamB
  maxRounds = context.maxRounds
  buffManager = new BuffManager()
  winner = "DRAW"

  while round < maxRounds:
    round++
    ① 处理持续效果（DOT/HEAL）—— processOverTimeEffects()（同时 tick 并移除到期 Buff）
    ② 按 speed × speedModifier 降序排列所有存活 combatant → 行动序列
    ③ for each combatant in 行动序列:
         if 战斗单位被控制（眩晕/冰冻）→ 记录 CONTROLLED 日志并跳过行动
         if 战斗单位被沉默 → 不能使用技能（退化为普攻）
         a. 从已冷却的技能中**随机**选取一个
         b. 逐个效果按触发概率（默认 1.0）判定并处理：
            - DAMAGE/MULTI_HIT/AOE_DAMAGE → 造成伤害（AOE 对同队其他存活单位造成 60% 溅射）
            - ARMOR_BREAK → 施加破甲 Debuff
            - SLOW → 施加减速 Debuff
            - DOT → 施加持续伤害 Debuff（可叠加）
            - EXECUTE → 对低于阈值的目标伤害 ×2
            - LIFESTEAL → 造成伤害并恢复生命
            - HEAL → 恢复自身生命
            - ATTACK_BUFF/DEFENSE_BUFF/SPEED_BUFF → 施加增益 Buff
            - STUN/FREEZE/SILENCE → 施加控制 Debuff
            - DODGE/COUNTER/REFLECT → 施加受击反应 Buff
            - CLEANSE → 清除自身 Debuff
         c. 全部效果未命中时退化为普通攻击（不空耗技能 CD）
         d. 伤害落地统一走 ReactiveEffectProcessor（冰冻易伤 → 闪避 → 扣血 → 反伤 → 反击）
      ④ 回合结束时所有技能 CD -= 1
      ⑤ if teamB.isAllDead() → winner=teamA.name()
      ⑥ if teamA.isAllDead() → winner=teamB.name()
  → draw（超时平局）
```

### 4.2 返回结果（BattleResultVO，代码现状）

```
BattleResultVO
├── winner              → 胜方队伍名 / "DRAW"
├── rounds              → 实际回合数
├── playerHpChange      → teamA 成员气血变化 {name: {before, after}}
├── skillProcs[]        → 法决触发记录 [{key, count}]（key = 单位名:技能名）
└── combatLog[]         → 每次攻击的记录，按序排列
```

> 文档早期列出的 `beastHpChanges/monsterHpChanges/damageDealt/drops/expGained/summary` 字段已不在 VO 中；掉落与修为由 `CombatEventHandler` 的 `EncounterResult`/`CombatSummary` 聚合。

### 4.3 战斗日志条目（CombatLogEntry）

Java Record 定义（`domain/monster/vo/CombatLogEntry.java`）：

```
CombatLogEntry
├── round               → 回合数 (1-based)
├── sequence            → 本回合内出手序号
├── attackerName        → 攻击方名称
├── defenderName        → 防御方名称（施加增益时为攻击者自身）
├── attackType          → NORMAL / SKILL / CONTROLLED（枚举 AttackType）
├── skillName           → 法决名称（attackType=SKILL 时非空）
├── effects             → 效果类型列表（如["ARMOR_BREAK", "SLOW"]；闪避时追加 "DODGED"）
├── buffApplied         → 是否施加了Buff/Debuff/控制
├── buffTarget          → Buff/Debuff目标名称（增益为自身）
├── damageDealt         → 造成伤害值（闪避为 0）
├── defenderHpBefore    → 防御方攻击前HP
├── defenderHpAfter     → 防御方攻击后HP
└── isKill              → 本次攻击是否击杀
```

---

## 5. 目标选择策略

| 策略 | 适用场景 | 规则 | 状态 |
|------|---------|------|------|
| `DefaultTargetSelectionStrategy` | 全部场景（历练/秘境/PVP/悬赏/雷劫） | 选择存活目标中**气血比例最低**者 | ✅ 已实现 |
| `PVP_BALANCED` | PVP（预留） | 按 speed 比例分配攻击目标 | 🔜 未实现（当前 PVP 同样集火低血量） |

> 当前所有 Combatant 使用统一的目标选择逻辑，不区分玩家/灵兽/怪物；`CombatTeam` 上另有 `selectTargetForPVE()`（同规则）与 `selectTargetRandom()` 备用。

---

## 6. 伤害公式

### 6.1 基础公式

```
// 玩家攻击力（战斗内）
playerAttack = 有效力道 × 2 + 法器最终攻击 + 攻击Buff

// 玩家伤害
playerDamage = round(playerAttack × weaponTypeAdvantage(weaponType, monsterType) × attackModifier)

// 灵兽/怪物伤害（不受法器克制影响）
damage = round(attack × attackModifier)

// 统一减免（考虑破甲与防御增益）
reduction = round(targetDefense × 0.4 × defenseModifier(破甲) × defenseBonusModifier(防御Buff))
最终伤害 = max(1, rawDamage - reduction)
```

### 6.2 法决公式求值

`DamageCalculator.evaluateFormula(formula, playerCombatant)` 使用递归下降解析器（支持小数点与括号）求值公式。可用变量：

| 变量 | 含义 |
|------|------|
| `attack` / `atk` | 攻击力 |
| `str` | 力道 |
| `wis` | 悟性 |
| `agi` | 身法 |

支持运算：`+`、`-`、`*`、`/`、括号 `()`。例：`attack*1.5`、`wis*0.8+100`。

- 非玩家单位携带公式时按 `attack × 1.5` 兜底；无公式时按 `attack` 计算
- 公式解析失败（如使用未支持变量 `defense`/`con`）回退为 `max(1, attack)` 并记警告

### 6.3 Buff 修正计算

见 9.3 的 BuffManager 方法；伤害层只消费：攻击修正、防御修正（破甲）、防御增益修正。

---

## 7. 速度与攻速

### 7.1 行动顺序（速度）

每回合按 `speed × speedModifier（考虑减速/加速）` 降序排列，速度高的先行动：

```
// 玩家
speed = 有效身法 × 2 + 10 + 速度Buff

// 怪物
speed = baseSpeed × (1 + (level - baseLevel) × 0.1)

// 灵兽
speed = (level × 2 + 8) × 突变速度倍率
```

### 7.2 法决冷却系统

每个 Combatant 的每个技能独立冷却（key 为 `combatantId:skillId`），回合结束时统一 -1：

```
冷却回合数 = max(1, int(skill.cooldownSeconds / attackSpeed))
每回合结束时：每个技能 CD -= 1（归零移除）
```

- `attackSpeed` 来自法器模板（`EquipmentTemplate.attackSpeed`），无武器时为 1.0
- 技能触发后进入 CD；被沉默时不能使用技能
- 技能从已冷却的技能中随机选取一个使用
- 技能全部效果未命中时不进入 CD，退化为普攻

---

## 8. 克制矩阵

玩家装备法器类型克制怪物类型时，伤害 ×1.5；否则 ×1.0。

| 法器类型 | 克制怪物类型 |
|---------|------------|
| 刀 (BLADE) | 妖兽 (BEAST) |
| 剑 (SWORD) | 灵怪 (SPIRIT) |
| 斧 (AXE) | 甲怪 (ARMORED) |
| 枪 (SPEAR) | 蛮兽 (WILD_BEAST) |
| 棍 (STAFF) | 邪怪 (EVIL) |
| 弓 (BOW) | 飞行怪 (FLYING) |

克制仅玩家对怪物单向生效，灵兽不受法器克制影响。

---

## 9. 技能效果系统

### 9.1 效果类型枚举（EffectType）

| 效果 | 说明 | 实现状态 |
|------|------|----------|
| `DAMAGE` | 基础伤害 | ✅ 已实现 |
| `MULTI_HIT` | 连击（伤害 × 段数，默认 3） | ✅ 已实现 |
| `AOE_DAMAGE` | 群体伤害（同队其他存活单位受 60% 溅射） | ✅ 已实现 |
| `ARMOR_BREAK` | 破甲（降低防御） | ✅ 已实现 |
| `SLOW` | 减速（降低速度） | ✅ 已实现 |
| `DOT` | 持续伤害（可叠加） | ✅ 已实现 |
| `EXECUTE` | 斩杀（低气血额外伤害，×2） | ✅ 已实现 |
| `LIFESTEAL` | 吸血 | ✅ 已实现 |
| `STUN` | 眩晕（跳过行动） | ✅ 已实现 |
| `FREEZE` | 冰冻（跳过行动+受伤 ×1.3） | ✅ 已实现 |
| `SILENCE` | 沉默（禁止技能） | ✅ 已实现 |
| `HEAL` | 治疗 | ✅ 已实现 |
| `ATTACK_BUFF` | 攻击增益 | ✅ 已实现 |
| `DEFENSE_BUFF` | 防御增益 | ✅ 已实现 |
| `SPEED_BUFF` | 速度增益 | ✅ 已实现 |
| `DODGE` | 闪避（受击概率完全落空） | ✅ 已实现 |
| `COUNTER` | 反击（概率对攻击者追加普攻） | ✅ 已实现 |
| `REFLECT` | 反射（按比例返还伤害） | ✅ 已实现 |
| `CLEANSE` | 净化（清除自身 Debuff） | ✅ 已实现 |
| `RESIST_BUFF` | 抗性增益（被动，不进主动 handler） | ⚪ 被动专用 |
| `HP_BUFF` | 气血上限增益（被动，不进主动 handler） | ⚪ 被动专用 |
| `SURVIVE_LETHAL` | 濒死生存（被动，不进主动 handler） | ⚪ 被动专用 |

> `EffectHandlerRegistry` 覆盖全部类型：被动专用类型注册为空结果；`SkillEffect` 支持 `formula/value/duration/maxStacks/chance/element/target` 字段。

### 9.2 Buff/Debuff 系统

```
Buff
├── type → BuffType枚举
├── value → 效果数值（百分比 0.2 = 20%）
├── remainingTurns → 剩余回合数
├── source → 来源（技能名称）
├── stackable → 是否可叠加
├── stackCount → 当前叠加层数
├── maxStacks → 最大叠加层数
├── isDebuff() → 是否为负面效果
├── isControl() → 是否为控制效果
├── isOverTime() → 是否为持续效果
├── tick() → 回合结束时减少持续时间
├── isExpired() → 是否已过期
├── tryStack() → 尝试叠加
└── refreshDuration(newDuration) → 刷新持续时间
```

**BuffType 枚举：**

| 类型 | 说明 | 类别 |
|------|------|------|
| `ARMOR_BREAK` | 破甲（降低防御力） | Debuff |
| `SLOW` | 减速（降低速度） | Debuff |
| `DOT` | 持续伤害 | Debuff |
| `STUN` | 眩晕（跳过行动） | Debuff |
| `FREEZE` | 冰冻（跳过行动+受伤增加） | Debuff |
| `SILENCE` | 沉默（禁止技能） | Debuff |
| `HEAL` | 治疗 | Buff |
| `ATTACK_BUFF` | 攻击增益 | Buff |
| `DEFENSE_BUFF` | 防御增益 | Buff |
| `SPEED_BUFF` | 速度增益 | Buff |
| `RESIST_BUFF` | 抗性增益 | Buff |
| `HP_BUFF` | 气血上限增益 | Buff |
| `DODGE` | 闪避 | Buff |
| `COUNTER` | 反击 | Buff |
| `REFLECT` | 反射 | Buff |

### 9.3 Buff管理器（BuffManager）

```
BuffManager
├── addBuff(combatantId, buff) → 添加Buff（按 type+source 叠加/刷新）
├── removeExpiredBuffs(combatantId) → 移除过期Buff
├── removeDebuffs(combatantId) → 清除所有Debuff
├── getBuffs(combatantId) → 获取所有Buff
├── getBuffsByType(combatantId, type) → 获取特定类型Buff
├── hasControl(combatantId) → 检查是否有控制效果
├── hasSilence(combatantId) → 检查是否有沉默效果
├── getDefenseModifier(combatantId) → 防御修正（破甲，下限 0.1）
├── getSpeedModifier(combatantId) → 速度修正（减速/加速，clamp [0.1, 3.0]）
├── getAttackModifier(combatantId) → 攻击修正（上限 4.0，即 +300%）
├── getDefenseBonusModifier(combatantId) → 防御增益（上限 3.0）
├── getResistModifier(combatantId) → 抗性修正（上限 3.0）
├── getHpBonusModifier(combatantId) → 气血上限修正（上限 3.0）
├── getDodgeChance(combatantId) → 闪避概率（各层之和，上限 1.0）
├── getCounterChance(combatantId) → 反击概率（上限 1.0）
├── getReflectPercent(combatantId) → 反射比例（上限 1.0）
├── processOverTimeEffects(combatantId) → 处理DOT/HEAL并tick（每回合开始调用）
├── clearAllBuffs(combatantId) → 清除所有Buff
└── clear() → 清除所有数据
```

效果默认值：增益/减益 `value = 0.2`、`duration = 3`；破甲默认 0.3/3 回合；减速默认 0.3/2 回合；DOT 默认 0.15/3 回合/3 层；EXECUTE 默认阈值 0.3；吸血默认 0.33；治疗默认 0.5；控制默认 1 回合（内部 +1 以覆盖完整回合）。

### 9.4 受击反应层（ReactiveEffectProcessor）

伤害落地时按顺序结算：

1. **冰冻易伤**：目标有未过期 FREEZE 时伤害 ×1.3
2. **闪避判定**：`random < 闪避概率` → 本次攻击完全落空（伤害 0，日志追加 DODGED）
3. **扣血**
4. **反伤**：按反射比例将所受伤害返还攻击者（至少 1 点，攻击者存活时）
5. **反击**：按概率对攻击者追加一次普攻伤害

---

## 10. 高光战斗系统

### 10.1 设计目标

在文字 MUD 挂机游戏中，每次历练可能有多场战斗，但只有 1 场高光战斗值得用 LLM 美化渲染，提升沉浸感。

### 10.2 高光战斗触发条件（代码现状）

| 条件 | 说明 | 权重 |
|------|------|------|
| 长回合战斗 | 战斗回合数 ≥ 10 | 高 |
| 势均力敌 | 玩家方任一成员战后气血 ≤ 战前气血的 30% | 高 |
| 技能纷呈 | 本场触发的不同技能种类 ≥ 3 | 中 |

三者满足其一即为高光；`HighlightInfo` 记录 `battleIndex`、`reason`、`rounds`。

### 10.3 LLM 渲染策略

**输入：** 高光战斗数据 + 玩家角色信息 + 地图环境（历练叙事由 `ExplorationDescriptionFunction` 统一美化）

**输出：** 修仙小说风格的战斗叙述

**风格要求：**

- 简洁有力，符合文字 MUD 风格
- 突出关键时刻和转折
- 包含技能名称和效果描述
- 体现灵兽配合和战斗策略

**示例：**

```
第5场战斗，你遭遇了一只剧毒蟾蜍王。它喷出的毒雾几乎遮蔽了你的视线，
但火羽鸡及时喷出烈焰，将毒雾驱散。你抓住机会，御剑术触发，
一剑刺穿了它的要害。这场战斗持续了5个回合，最终你以微弱优势胜出。
```

---

## 11. 动态遇怪系统

### 11.1 遇怪间隔计算（EncounterCalculator）

```
baseInterval  = 12 - map.encounterRichness        // 地图富裕度越高，基础间隔越短
mapDanger     = 1 + (mapLevel - 1) × 0.015        // 地图凶险系数
levelMismatch = 1 + |playerLevel - mapLevel| / max(mapLevel, 1) × 2.5  // 等级偏差惩罚
interval      = clamp(baseInterval × levelMismatch / mapDanger, 3, 20) // 分钟
```

- 最小间隔 3 分钟、最大间隔 20 分钟
- 无装备评分（gearScore）因子；装备评分机制已移除

### 11.2 遇怪概率与结算

```
slots          = int(durationMinutes / interval)
perRollChance  = min(1.0, 0.4 × 10 / interval)     // 间隔越短，单次概率越高
实际概率       = min(1.0, perRollChance × 运势命运倍率)
```

- 对一段历练时间执行 `slots` 次掷骰，命中后按事件权重随机选择事件（COMBAT / CHOICE / NUMERIC）
- 事件池来自 `activity_event` 中该地图的 TRAINING 子事件；无事件池时返回空结算

### 11.3 装备评分（已移除）

文档原 `gearScore = Σ(finalAttack + finalDefense × 2) + 稀有度加成` 的遇怪抑制机制不再存在，稀有度加成表（普通 +10 / 稀有 +40 / 史诗 +80 / 传说 +160）仅保留为历史设计。

---

## 12. 出击 / 参战规则

### 12.1 玩家

- 自动参战，不可离队
- 气血归零 → 玩家进入濒死（`setDying`），全队判负

### 12.2 灵兽

来源见 `福地` 系统：

- 在福地中选择"出战"灵兽（`beast.is_deployed = true`）
- 仅气血 > 0 且未在休养中的灵兽可出战（`Beast.canFight()`）
- 出战上限：固定 2 只（`MAX_BEAST_DEPLOY_COUNT = 2`，超出提示「出战灵兽已达上限 (2只)」）
- 战斗获取修为，升级后属性自动成长
- **技能已启用**：灵兽可以使用 `beast.skills` 中的技能
- **无独立 AI**：灵兽在战斗中与其他 Combatant 行为一致，无特殊策略
- 突变特质影响战斗：攻击/防御/速度百分比加成、低气血攻击提升、战后自愈（`ON_BATTLE_END_HEAL`）等

阵亡：

| 品质 | 休养时长 |
|------|---------|
| 凡品 | 30min |
| 灵品 | 1h |
| 仙品 | 2h |
| 圣品 | 4h |
| 神品 | 8h |

休养期间不可出战；阵亡时取消出战并写入 `recovery_until`；胜利后灵兽有概率觉醒技能（`tryAwakeningSkill`）。

### 12.3 怪物

- 由 `MonsterTemplate` → 等级缩放生成；实例等级 = `baseLevel + random(-2, 3) + 运势幸运偏移`（最低 1 级）
- 战斗中死亡即销毁
- 掉落归属由击杀者的 `CombatTeam.ownerId` 决定

**掉落表：** JSONB 数组，每个元素为 `DropTableEntry` record `(category, templateId, weight)`：

```json
drop_table = [
  {"category": "equipment", "templateId": 1, "weight": 50},
  {"category": "items", "templateId": 10, "weight": 80}
]
```

- `weight` 为独立百分比掉率（0-100），`DropProcessor` 逐条 `ThreadLocalRandom.nextDouble(100) < weight` 判定
- 物品数量 = `1 + random(0,3)`（1-3 个）× 财富加成倍率（不低于 1）
- 安全上限 5 件，超出时随机保留而非按权重截断
- `processMonsterDrops()` 返回 `List<DropItem>`，每个 `DropItem(DropType.EQUIPMENT/ITEM, templateId, name, quantity)`
- 掉落分发：装备走 `equipmentService.createEquipment()`，物品走 `stackableItemService.addStackableItem()`（`RewardGrant`）

### 12.4 战斗修为

```
expGained = expReward × 击杀数量 × clamp(1 + (怪物等级 - 玩家等级) × 0.05, 0.1, 3.0)
```

- 单次遇怪战斗开始前，玩家恢复 `max(1, 最大气血 / 20)`（5%）气血
- 击杀修为受修为存储上限截断

---

## 13. 历练编排与战后结算

- 单次结算封顶 720 分钟（12 小时），超出部分不参与物品掉落判定
- 每 60 分钟自动中途结算一段（`TrainingSettlementHandler`），`last_settlement_minute` 记录进度
- 基础修为/分钟 = `max(地图等级 × 5, √有效悟性 × 12)`
- 总修为 = `基础修为 × 分钟数 × 身法效率 × 等级衰减 × 运势幸运倍率`
  - 身法效率 = `1 + min(有效身法 × 0.01, 2.0)`
  - 等级衰减 = `max(0.1, 1 - (玩家等级 - 地图等级 - 5) × 0.04)`
- 地图特产掉落：判定次数 = `max(1, (有效分钟 / 10) × 身法效率)`，加权选取特产、数量 1-3
- 战后：`PostCombatProcessor` 回写玩家/灵兽气血，全灭进入濒死；`RewardGrant` 分发掉落；产出历练事件与叙事
- 历练中断（濒死）时：重伤前所获修为与物品照常发放，活动清空并产出中断事件
- 秘境（DungeonCombatHelper）：20 回合、场景 DUNGEON，战后回写玩家气血
- PVP（PvpService）：切磋双方战前回满气血，50 回合模拟，不持久化战斗气血

---

## 14. 未来扩展

### 14.1 LLM 叙事

`BattleResultVO.combatLog[]` 提供完整的战斗原子记录，可直接喂给 LLM 渲染为修仙小说风格叙事（历练高光、雷劫战斗均已接入）。

### 14.2 多方战斗编队

```
TeamA = [PlayerCombatant, PlayerCombatant, BeastCombatant, BeastCombatant...]
TeamB = [BossCombatant, MonsterCombatant, MonsterCombatant...]

simulate(TeamA, TeamB, 50)
```

### 14.3 PVP

```
TeamA = [PlayerCombatant + BeastCombatants]
TeamB = [PlayerCombatant + BeastCombatants]

TargetStrategy → PVP_BALANCED（未实现，当前集火低血量）
```

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **类型/位置更名**：`Team` → `CombatTeam`；`BeastQuality` 位于 `domain/fudi/enums/`；`DropProcessor` 位于 `service/` 而非 `service/combat/`。
- **BattleResultVO 字段**：仅 `winner/rounds/playerHpChange/skillProcs/combatLog`；掉落与修为由 `EncounterResult`/`CombatSummary` 聚合。
- **BattleContext 字段**：无 `mapId/mapLevel/playerLevel/gearScore`；场景为 `TRAINING/DUNGEON/PVP/BOUNTY`。
- **法决公式变量**：仅支持 `attack/atk/wis/str/agi`，`defense`/`con` 解析失败回退 `max(1, attack)`；现有法决公式全部只使用 `attack`，无实际差异。
- **伤害公式细节**：非玩家单位带公式按 `attack × 1.5` 兜底、无公式按 `attack`；伤害取整并 `max(1, raw - reduction)`，减免含破甲与防御 Buff 修正。
- **速度公式**：玩家 `有效身法 × 2 + 10 + 速度Buff`；灵兽 `(level × 2 + 8) × 突变倍率`；怪物 `base × (1 + Δ × 0.1)`。
- **怪物缩放**：hp/attack 系数 0.15、speed/defense 0.1；构造 hp 有 `max(0.1, …)` 下限而 `getMaxHp()` 无（实例等级偏差被限制在 ±5 级内，该下限实际不会触发，无观测影响）。

### B. 保留代码设计

- **目标选择统一集火低血量（含 PVP）**：双方同一规则、无阵营特权；文档「teamB 随机」会让玩家/灵兽承受不可预测的集火，`PVP_BALANCED`「按速度分配目标」语义含糊且无对称性收益，保留现状更清晰公平。
- **攻速决定技能冷却**（`max(1, cooldownSeconds / attackSpeed)`）：法器攻速成为实感差异，强化法器选择与锻造取舍，而非所有武器同速。
- **技能触发概率与未命中退化**：全部效果未命中时退化为普攻且不空耗 CD，减少「技能放了没用还进冷却」的挫败。
- **EffectType/BuffType 扩展**：AOE/闪避/反击/反射/净化/濒死生存等由引擎与受击反应层统一结算，给法决与流派更多空间。
- **高光判定改为「任一成员残血 / 回合 ≥ 10 / 不同技能 ≥ 3」**：标记玩家亲历的苦战与多变战；文档「同一技能触发 ≥ 3 次」会让常用技能几乎每场触发高光，稀释叙事价值。
- **遇怪公式重写**（地图富裕度 + 等级偏差 + 凶险系数，clamp [3, 20] 分钟）：频率由地图与等级匹配决定，语义直观；移除装备评分这一隐性「穿得好打得少」机制，避免强化装备反而降低历练收益。
- **掉落模型**（逐条独立概率、weight 即掉率、数量 1-3 × 财富倍率、上限 5 随机保留）：weight 语义统一，稀有掉落不会被高权重条目结构性挤出，也不会因必然掉落造成通胀。
- **实例等级随机 ±2 + 运势偏移**：同模板怪有等级波动，每日运势对战斗难度有实感，遇怪不再完全同质。
- **每场遇怪前恢复 5% 最大气血**：连续多场战斗不会越打越残，降低小怪连击导致的濒死挫败。
- **新增组件与机制**（TrainingSettler/CombatEventHandler/ReactiveEffectProcessor/EnlightenmentProcessor/CombatSummary/灵兽觉醒与变异战斗加成）：事件循环统一、受击反应独立成层，顿悟与觉醒提供惊喜反馈。
- **历练结算规则**（每 60 分钟中途结算、单次封顶 720 分钟、击杀修为受存储上限截断）：挂机期间定期产出收益与事件，避免数小时一次性结算的等待与溢出。

### C. 按设计修正（待修）

无。

### D. 未实现（待办）

无。

> 已实现（本轮）：护甲/饰品数值参与战斗。`PlayerCombatant` 通过 `EquipmentStats` 聚合装备四维与总攻/总防（含品质波动、词条与锻造）：攻击 = (力道 + 装备力) × 2 + 装备总攻击 + Buff；防御 = (根骨 + 装备根) + 装备总防御 + Buff；速度同理。状态页、档案与战斗共用同一聚合。

### E. 缺陷修复

无。
