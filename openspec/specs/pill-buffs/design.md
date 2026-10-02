# 丹药Buff系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

丹药 Buff 是服用丹药后获得的**有时效的增益效果**，分为战斗增益、突破加成和雷劫抗性三类。与战斗内技能产出的 Buff/Debuff 不同，丹药 Buff 持久化存储在数据库中，跨战斗生效。

### 与战斗 Buff 的区别

| 维度 | 丹药 Buff | 战斗内 Buff |
|------|-----------|-----------|
| 生命周期 | 持久化，跨战斗存在 | 仅单场战斗内有效 |
| 来源 | 服用丹药 | 技能施放 |
| 存储 | `player_buff` 表 | `BuffManager` 内存管理（`domain/monster`） |
| 枚举类型 | `PlayerBuffType`（`domain/pill/enums`） | `BuffType`（`domain/monster/enums`） |
| 作用范围 | 玩家全局战斗属性 | 单场战斗中单位属性 |

---

## 2. Buff 类型

`PlayerBuffType` 枚举定义五种类型：

| 枚举值 | code | 显示名称 | 说明 |
|-------|------|---------|------|
| ATTACK | attack | 攻击 | 战斗中直接叠加到攻击力，单位：属性点 |
| DEFENSE | defense | 防御 | 战斗中直接叠加到防御力，单位：属性点 |
| SPEED | speed | 速度 | 战斗中直接叠加到速度值，单位：属性点 |
| BREAKTHROUGH | breakthrough | 突破成功率 | 突破时加成成功率，单位：百分比 |
| TRIBULATION_RESIST | tribulation_resist | 雷劫抗性 | 渡劫/大境界突破时削弱雷劫 Boss，单位：百分比，**允许负值** |

- 战斗增益（ATTACK/DEFENSE/SPEED）在队伍构建时读取，叠加到玩家战斗单位的基础属性上。
- 突破加成（BREAKTHROUGH）仅在执行突破时读取，不参与战斗。
- 雷劫抗性（TRIBULATION_RESIST）仅在渡劫/大境界突破时读取，不参与普通战斗；负值表示增加雷劫难度。
- `Effect.Buff.attribute` 字段使用上表 code 字符串（如 `attack`、`tribulation_resist`）；`fromCode` 对未知 code 抛 `IllegalArgumentException`。

---

## 3. 数据存储

### 3.1 数据表 `player_buff`

表名 `player_buff`（无 `xt_` 前缀）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 玩家 ID，FK → `player(id)` |
| buff_type | VARCHAR(32) | Buff 类型，CHECK 约束限值 |
| value | INT | 增益值（攻击/防御/速度为属性点，breakthrough/雷劫抗性为百分比） |
| expires_at | TIMESTAMP | 过期时间 |
| created_at | TIMESTAMP | 创建时间，默认 NOW() |

```sql
CONSTRAINT chk_player_buff_type CHECK (
    buff_type IN ('attack', 'defense', 'speed', 'breakthrough', 'tribulation_resist')
),
CONSTRAINT chk_player_buff_value CHECK (value >= -100)
```

索引：

```sql
CREATE INDEX idx_player_buff_expires ON player_buff(expires_at);
CREATE INDEX idx_player_buff_user ON player_buff(user_id);
CREATE INDEX idx_player_buff_user_type_expires ON player_buff(user_id, buff_type, expires_at);
```

### 3.2 Java 层设计

- 实体：`PlayerBuff`（`domain/pill/entity`）— MyBatis-Flex 实体，`buffType` 字段类型为 `PlayerBuffType` 枚举，通过 `@EnumValue` 注解自动完成 code 与枚举的互转；提供 `isExpired()` / `isActive()` 与工厂方法 `create(userId, buffType, value, expiresAt)`。
- 仓储：`PlayerBuffRepository` — 按用户查询活跃 Buff、按类型查询/计数、原子条件插入、按类型删除、清理过期。
- `PlayerBuffMapper` 直接以 `expires_at > NOW()` 过滤，并提供：
  - `deleteExpired()` — 全局清理所有过期 Buff（已实现，当前无调用方）
  - `deleteExpiredByUserId(userId)` — 清理指定用户的过期 Buff
- `PlayerBuffType.fromCode()` 对未知 code 抛异常；枚举 code 与 DB CHECK 完全一致（含大小写）。

---

## 4. 创建流程

### 4.1 丹药服用产生（`Effect.Buff`）

丹药效果中的 `buff` 类型效果触发 Buff 创建：

1. 玩家执行「使用 [丹药名]」命令，物品使用服务统一扣减 1 件后调用 `PillConsumptionService.takePill()`
2. 解析丹药模板的效果列表，读取成色（缺省中成）与品阶
3. 对 `Effect.Buff` 类型效果计算实际值：
   ```
   实际值 = (int)(amount × 成色倍率 × 等级衰减)
   等级衰减 = max(0.1, min(1.0, pillGrade / (玩家等级 × 0.2)))
   ```
4. 实际值 ≤ 0 且类型不是 `tribulation_resist` → 不创建 Buff（避免无意义 0 值记录）
5. 调用原子条件插入 `insertIfBelowStackLimit(userId, buffType, value, expiresAt, 3)`：
   - 仅当该玩家该类型活跃 Buff（`expires_at > NOW()`）少于 3 条时写入
   - 返回 0 表示已达堆叠上限，回复「[类型] buff 已达堆叠上限（3层）」
6. 过期时间 = 服用时刻 + 效果的 `duration_seconds` 字段（秒）
7. 成功回复「获得 [类型显示名] +N（持续N秒）」

### 4.2 突破丹药（`Effect.Breakthrough`）

突破类型丹药的 `breakthrough` 效果也通过 Buff 系统实现：

1. 计算 `bonusValue = (int)(rate × 100 × 成色倍率 × 等级衰减)`（等级衰减有 0.1 保底），≤ 0 不生效
2. 创建 `PlayerBuffType.BREAKTHROUGH` 类型的 Buff，直接插入（**不经过 3 层上限**，多颗丹药的加成会累加）
3. 过期时间固定为当前 +1 小时
4. 下次执行突破时，读取未过期的 BREAKTHROUGH Buff 累加成功率/削弱雷劫
5. 突破完成后（无论成败），清除该玩家所有 BREAKTHROUGH Buff

---

## 5. 战斗中的使用

### 5.1 队伍构建时加载

战斗队伍构建（`CombatService.buildPlayerTeam` → `TeamBuilder`/`DefaultTeamBuilder`）时：

1. 查询玩家所有未过期的 Buff（`expires_at > NOW()`）
2. 遍历 Buff，按类型累加：
   - ATTACK → attackBuff（攻击点）
   - DEFENSE → defenseBuff（防御点）
   - SPEED → speedBuff（速度点）
   - BREAKTHROUGH / TRIBULATION_RESIST → 跳过（不参与战斗）
3. 通过 `PlayerCombatant.withBuffs(attackBuff, defenseBuff, speedBuff)` 应用到战斗单位

### 5.2 属性计算公式

```
玩家攻击 = 有效力道 × 2 + 装备攻击 + attackBuff
玩家防御 = 有效根骨 + defenseBuff
玩家速度 = 有效身法 × 2 + 10 + speedBuff
```

有效属性为玩家实体计算后的四维（含装备/词条等加成）；同类型多条 Buff 的值全部累加。

### 5.3 预加载优化

历练结算的事件循环在循环开始前预查本批事件涉及的怪物模板与怪物技能（`skillMap`）、灵兽缓存，后续每次遭遇复用这些查找表，不再逐次查询数据库。Buff 查询保持每次队伍构建时执行（因为 Buff 可能在两次遭遇间变化）。

---

## 6. 突破中的使用

### 6.1 小境界突破

```
总成功率 = clamp(基础概率 + 失败次数 × 单次补偿 + 护道加成 + 突破丹药加成, 0, 100)
```

其中突破丹药加成 = 所有未过期 BREAKTHROUGH Buff 的 value 之和（百分比）。

### 6.2 大境界 / 渡劫突破（战斗制）

- 丹药 + 护道加成转为雷劫 Boss 削弱：`bossReduction = clamp((丹药加成 + 护道加成)/100, 0, 0.5)`，下限 0（负加成不反向增强雷劫）
- 雷劫抗性 Buff：`tribulationResist = min(0.9, Σ(tribulation_resist value)/100)`，即最多削弱 90%；负值会增大 `1 - tribulationResist` 的难度系数
- 雷劫预报面板展示 `min(90, Σvalue)` 百分比与突破丹药加成

### 6.3 突破后清除

小境界突破的成功与失败处理、战斗突破的胜败处理，均调用 `deleteByUserIdAndType(userId, BREAKTHROUGH)` 清除突破类 Buff，无论成败。雷劫抗性 Buff 不在突破后清除，按自身 `expires_at` 过期。

---

## 7. 过期与清理

### 7.1 查询过滤

所有 Buff 生效判定与查询条件均为 `expires_at > NOW()`，过期 Buff 在逻辑层面已「不可见」。

### 7.2 物理清理

- `BuffExpiryHandler`（`service/player/state`，`@Order(4)`）在玩家状态结算时按用户调用 `deleteExpiredByUserId(userId)`；状态稳定（`isStableState()`）时状态结算被快速路径跳过，因此清理是懒执行的。
- `PlayerBuffMapper.deleteExpired()` 提供全局清理方法，但当前没有定时任务调用方。
- 无独立定时清理任务；过期 Buff 主要靠查询过滤保证不生效。

### 7.3 突破后清除

见 6.3；此外，战斗突破流程在扣除修为后、结算胜败前也会清除 BREAKTHROUGH Buff。

---

## 8. 值域约束

- DB CHECK：`value >= -100`（雷劫抗性允许负值，如「招雷散」的 `-40`）；文档所述「≥ 0」不成立。
- 实际值由 `PillConsumptionService` 计算：`效果基础值 × 成色倍率 × 等级衰减`。
- 若计算后实际值 ≤ 0 且类型非雷劫抗性，不创建 Buff。
- 战斗增益 / 雷劫抗性类 Buff 受同类活跃层数上限 3 约束（原子条件插入）；突破加成不设层数上限，多条记录数值累加。

---

## 9. 有效期设计

| Buff 来源 | 典型持续时间 |
|---------|------------|
| 战斗增益丹药 | 丹方配置的 `duration_seconds`（如 300 秒=5 分钟、1800 秒=30 分钟） |
| 雷劫抗性丹药 | 丹方配置的 `duration_seconds`（如 3600 秒=1 小时） |
| 突破加成丹药 | 固定 1 小时（代码硬编码） |

有效期从丹药服用时刻开始计算，不同丹药可有不同持续时间，由物品模板 JSON 中的 `duration_seconds` 字段定义。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **枚举实际 5 种**：新增 `TRIBULATION_RESIST`（`tribulation_resist`，雷劫抗性），DB CHECK 同步 5 值；`fromCode` 对未知值抛异常。
- **值域下限 -100**：`chk_player_buff_value` 为 `value >= -100`（雷劫抗性允许负值，如「招雷散」-40），非原文档的「≥ 0」。
- **属性公式使用有效四维**：攻击/防御/速度分别基于 `getEffectiveStatStr/Con/Agi`（含装备/词条加成），公式形式与文档一致。
- **战斗读取实现在 `DefaultTeamBuilder`**：`CombatService.buildPlayerTeam` 委托 `TeamBuilder`，`loadActiveBuffs` 额外跳过 `BREAKTHROUGH`/`TRIBULATION_RESIST`；原文档所述 `CombatService.buildPlayerTeam()` 内联逻辑已拆分。
- **预加载描述过时**：`TrainingCombatLogic` 类不存在；实际为 `TrainingSettler` 在事件循环开始前预查怪物模板/技能（`skillMap`）与灵兽缓存（`beastCache`），Buff 仍每次队伍构建查询（两次遭遇间可能变化）。
- **突破 Buff 过期时间固定 1 小时**（`plusHours(1)`），与文档一致；雷劫抗性时长来自 `duration_seconds`。

### B. 保留代码设计

- **战斗增益/雷劫抗性类 Buff 同类活跃上限 3 层**：原子条件插入（`insertIfBelowStackLimit`）防止并发超层，避免开战前堆叠大量丹药形成爆发；超限回复明确提示。满层时丹药仍已在统一扣减阶段消耗，属现有使用流程的副作用，后续可考虑消费前校验。
- **实际值 ≤ 0 的雷劫抗性 Buff 仍创建**：负值是「招雷散」这类高风险丹药的刻意设计（增大雷劫难度），丢弃负值会让该类丹药失效；仅非雷劫抗性类型丢弃无效值。
- **突破加成/护道加成覆盖战斗突破**：小境界 `clamp(基础 + 补偿 + 护道 + 丹药, 0, 100)`；大境界/渡劫转为雷劫 Boss 削弱 `clamp((丹药 + 护道)/100, 0, 0.5)`，雷劫抗性削弱上限 0.9（预报展示上限 90%），下限 0 保证负加成不反向增强雷劫。丹药在两类突破中都有意义，避免渡劫期丹药失效的断层。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

无。

> 已实现（本轮）：`PlayerBuffMapper.deleteExpired()` 挂每小时定时清理（存储卫生；过期 Buff 在玩法上早已不可见）。

### E. 缺陷修复

无
