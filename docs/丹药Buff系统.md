# 丹药Buff系统

## 1. 概述

丹药Buff是服用丹药后获得的**有时效的增益效果**，分为战斗增益和突破加成两类。与战斗内技能产出的Buff/Debuff不同，丹药Buff持久化存储在数据库中，跨战斗生效。

### 与战斗Buff的区别

| 维度 | 丹药Buff | 战斗内Buff |
|------|---------|-----------|
| 生命周期 | 持久化，跨战斗存在 | 仅单场战斗内有效 |
| 来源 | 服用丹药 | 技能施放 |
| 存储 | `xt_player_buff` 表 | `BuffManager` 内存管理 |
| 枚举类型 | `PlayerBuffType` | `BuffType` |
| 作用范围 | 玩家全局战斗属性 | 单场战斗中单位属性 |

---

## 2. Buff类型

`PlayerBuffType` 枚举定义四种类型：

| 枚举值 | code | 显示名称 | 说明 |
|-------|------|---------|------|
| ATTACK | attack | 攻击 | 战斗中直接叠加到攻击力，单位：属性点 |
| DEFENSE | defense | 防御 | 战斗中直接叠加到防御力，单位：属性点 |
| SPEED | speed | 速度 | 战斗中直接叠加到速度值，单位：属性点 |
| BREAKTHROUGH | breakthrough | 突破成功率 | 突破时加成成功率，单位：百分比 |

- 战斗增益（ATTACK/DEFENSE/SPEED）在 `CombatService.buildPlayerTeam()` 中读取，叠加到 `PlayerCombatant` 的基础属性上
- 突破加成（BREAKTHROUGH）仅在执行突破时读取，不参与战斗

---

## 3. 数据存储

### 3.1 数据表 `xt_player_buff`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 玩家ID，FK → xt_user |
| buff_type | VARCHAR(32) | Buff类型，CHECK约束限值 |
| value | INT | 增益值（攻击/防御/速度为属性点，breakthrough为百分比） |
| expires_at | TIMESTAMP | 过期时间 |
| created_at | TIMESTAMP | 创建时间 |

CHECK约束：`buff_type IN ('attack', 'defense', 'speed', 'breakthrough')`

### 3.2 Java层设计

- 实体：`PlayerBuff` — MyBatis-Flex实体，`buffType` 字段类型为 `PlayerBuffType` 枚举，通过 `@EnumValue` 注解自动完成code与枚举的互转
- 仓储接口：`PlayerBuffRepository` — 提供按用户ID、按类型查找、按类型删除的方法
- `PlayerBuffType.fromCode()` — 将JSON属性字符串安全转换为枚举，未知code抛异常（编译期安全）

---

## 4. 创建流程

### 4.1 丹药服用产生

丹药效果中的 `buff` 类型效果触发Buff创建：

1. 玩家执行"使用 丹药名"命令
2. `PillConsumptionService.takePill()` 解析丹药模板的效果列表
3. 对 `Effect.Buff` 类型效果，计算实际值（品质倍率 × 等级衰减）
4. 调用 `PlayerBuff.create(userId, buffType, value, expiresAt)` 创建Buff
5. 过期时间由效果的 `duration_seconds` 字段决定

### 4.2 突破丹药

突破类型丹药的 `breakthrough` 效果也通过Buff系统实现：

1. 服用突破丹药，创建 `PlayerBuffType.BREAKTHROUGH` 类型的Buff
2. 过期时间固定为1小时
3. 下次执行突破时，读取未过期的BREAKTHROUGH Buff累加成功率
4. 突破完成后（无论成败），清除该玩家所有BREAKTHROUGH Buff

---

## 5. 战斗中的使用

### 5.1 队伍构建时加载

`CombatService.buildPlayerTeam()` 在构建玩家队伍时：

1. 查询玩家所有未过期的Buff（`expires_at > NOW()`）
2. 遍历Buff，按类型累加：
   - ATTACK → attackBuff（攻击点）
   - DEFENSE → defenseBuff（防御点）
   - SPEED → speedBuff（速度点）
   - BREAKTHROUGH → 跳过（不参与战斗）
3. 通过 `PlayerCombatant.withBuffs(attackBuff, defenseBuff, speedBuff)` 应用到战斗单位

### 5.2 属性计算公式

```
玩家攻击 = 力道 × 2 + 装备攻击 + attackBuff
玩家防御 = 根骨 + defenseBuff
玩家速度 = 身法 × 2 + 10 + speedBuff
```

### 5.3 预加载优化

训练循环（历练）中，`TrainingCombatLogic` 在循环开始前预先加载所有技能（怪物+玩家+灵兽），后续每次遭遇复用同一个查找表，不再逐次查询数据库。Buff查询保持每次队伍构建时执行（因为Buff可能在两次遭遇间变化，但当前训练中不变）。

---

## 6. 过期与清理

### 6.1 查询过滤

所有Buff查询条件为 `expires_at > NOW()`，过期Buff在逻辑层面已"不可见"。

### 6.2 物理清理

`PlayerBuffMapper` 提供两个删除方法：

- `deleteExpired()` — 全局清理所有过期Buff
- `deleteExpiredByUserId(userId)` — 清理指定用户的过期Buff

### 6.3 突破后清除

`CultivationService` 的突破成功和失败处理中，均调用 `deleteByUserIdAndType(userId, BREAKTHROUGH)` 清除突破类Buff，无论成败。

---

## 7. 值域约束

- Buff值必须 ≥ 0（DB CHECK `chk_player_buff_value`）
- 实际值由 `PillConsumptionService` 计算：`效果基础值 × 品质倍率 × 等级衰减`
- 若计算后实际值 ≤ 0，不创建Buff（避免无意义的0值记录）
- 战斗增益为无条件累加（无上限），多个同类型Buff效果叠加

---

## 8. 有效期设计

| Buff来源 | 典型持续时间 |
|---------|------------|
| 战斗增益丹药 | 丹方配置的 `duration_seconds`（如300秒=5分钟） |
| 突破加成丹药 | 固定1小时（代码硬编码） |

有效期从丹药服用时刻开始计算，不同丹药可有不同持续时间，由物品模板JSON中的 `duration_seconds` 字段定义。
