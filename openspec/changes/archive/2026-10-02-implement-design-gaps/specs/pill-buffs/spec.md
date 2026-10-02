# Spec Delta

## Purpose

定义丹药 Buff 对玩家可观察的行为契约：服用丹药获得时效增益、战斗增益叠加、突破加成与雷劫抗性、同类堆叠上限、过期与清理、值域约束与提示文案。数据模型、公式与实现细节见同目录 design.md。

## ADDED Requirements

### Requirement: 过期 Buff 定时清理

系统 MUST 以每小时一次的定时清理删除全库已过期 Buff 记录，覆盖长期未触发状态结算的玩家；清理 MUST 删除全部「过期时间 ≤ 当前时间」的记录，且 MUST NOT 影响未过期记录。

#### Scenario: 定时清理覆盖离线玩家

- **WHEN** 长期未上线的玩家存在已过期的 Buff 记录
- **THEN** 下一轮定时清理删除该记录，同表未过期记录保留

## MODIFIED Requirements

### Requirement: 战斗增益生效

有效期内，攻击、防御、速度类 Buff MUST 在构建玩家战斗队伍时按类型分别累加，并 MUST 应用到战斗单位的攻击/防御/速度计算中：

```
玩家攻击 = (有效力道 + 装备力道) × 2 + 装备总攻击 + 攻击 Buff 总和
玩家防御 = (有效根骨 + 装备根骨) + 装备总防御 + 防御 Buff 总和
玩家速度 = (有效身法 + 装备身法) × 2 + 10 + 速度 Buff 总和
```

同类型多条 Buff 的数值 MUST 全部累加；已过期的 Buff MUST NOT 参与计算。

#### Scenario: 战斗加成

- **WHEN** 玩家持有未过期的攻击 Buff 参与战斗
- **THEN** 战斗中的玩家攻击力包含该 Buff 的加成总和

#### Scenario: 过期不生效

- **WHEN** 玩家持有的 Buff 已过有效期后进入战斗
- **THEN** 该 Buff 不参与战斗属性计算
