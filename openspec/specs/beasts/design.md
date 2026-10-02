# 灵兽系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 数据架构

### 1.1 单一数据源原则

灵兽属性 **仅存储在 `beast` 表**，兽栏地块 (`fudi_cell.config`) 只存兽栏专属数据，通过 `beast_id` 联表引用。

```
beast_template                ← 灵兽模板配置（孵化配置、技能池、产出物、tags）
  └── id (PK)

item_template (BEAST_EGG)     ← 兽卵物品模板
  └── properties.beast_template_id → beast_template.id

beast                         ← 灵兽实体（唯一数据源）
  └── template_id → beast_template.id

fudi_cell.config              ← 兽栏配置（PenConfig 记录）
  ├── beast_id → beast.id     (联表引用)
  ├── template_id             (兽卵模板ID，用于产出配置)
  ├── hatch_time              (孵化开始时间)
  ├── mature_time             (孵化完成时间)
  ├── production_stored       (累积产出列表 JSONB)
  └── last_production_time    (产出计时)
```

> 代码/迁移中的实际表名为 `beast_template`、`beast`、`fudi_cell`（无 `xt_` 前缀）。
> **实现偏差**：`BeastBreedingService.hatchBeastWithTemplate` 把兽卵的 `item_template.id` 写入 `beast.template_id`，而该列外键指向 `beast_template(id)`；技能池读取实际走「兽卵 properties → beast_template_id」路径，而变异特性标签与繁育配方匹配直接按 `beast.template_id` 查 `beast_template`（见文末差异）。

### 1.2 灵兽模板表 (beast_template)

| 字段                | 类型           | 说明                                           |
|-------------------|--------------|----------------------------------------------|
| `id`              | BIGSERIAL    | PK                                           |
| `name`            | VARCHAR(128) | 灵兽名称（唯一）                                    |
| `grow_time`       | INT          | 孵化时间（小时，CHECK >0；当前孵化流程未读取，见差异）                |
| `production_items`| JSONB        | 产出物品配置 `[{"weight":70,"template_id":1}]`     |
| `skill_pool`      | JSONB        | 技能池配置（见技能系统章节）                              |
| `tags`            | JSONB        | 灵兽标签 `["beast","flying","water"]`，用于繁育和变异筛选 |
| `description`     | TEXT         | 灵兽描述                                         |
| `create_time` / `update_time` | TIMESTAMP | 时间戳                                    |

### 1.3 Beast 表结构 (beast)

| 字段                            | 类型           | 说明                                       |
|-------------------------------|--------------|------------------------------------------|
| `id`                          | BIGSERIAL    | PK                                       |
| `user_id`                     | BIGINT       | 所属用户（FK → player）                         |
| `fudi_id`                     | BIGINT       | 所属福地（FK → fudi）                           |
| `template_id`                 | BIGINT       | FK → beast_template(id)，灵兽模板ID             |
| `beast_name`                  | VARCHAR(128) | 灵兽名称                                     |
| `gender`                      | VARCHAR(8)   | 性别（YIN 阴 / YANG 阳），孵化时随机分配             |
| `tier`                        | INT          | 等阶 (1通灵/2凝魄/3化形/4渡劫/5归真)                  |
| `quality`                     | VARCHAR(32)  | 品质 (MORTAL/SPIRIT/IMMORTAL/SAINT/DIVINE) |
| `mutation_traits`             | JSONB        | 变异特质ID列表（引用 mutation_trait_config.id）   |
| `level`                       | INT          | 等级                                       |
| `exp`                         | INT          | 修为                                       |
| `attack`                      | INT          | 攻击力                                      |
| `defense`                     | INT          | 防御力                                      |
| `max_hp`                      | INT          | 最大HP                                     |
| `hp_current`                  | INT          | 当前HP                                     |
| `skills`                      | JSONB        | 技能列表（技能ID 数组，`List<Long>`）              |
| `is_deployed`                 | BOOLEAN      | 是否出战                                     |
| `recovery_until`              | TIMESTAMP    | 恢复截止时间（休养中），可为 null；过期后自动清除           |
| `breeding_cooldown_until`     | TIMESTAMP    | 繁育冷却截止时间，冷却期内不可繁育                     |
| `penned_cell_id`              | INT          | 所在兽栏地块ID（null = 栏外休憩）                    |
| `birth_time`                  | TIMESTAMP    | 出生时间                                     |
| `create_time` / `update_time` | TIMESTAMP    | 时间戳                                      |

### 1.4 兽卵模板配置

兽卵 (`item_template`, type=BEAST_EGG) 的 `properties` 简化为引用灵兽模板：

```json
{
  "beast_template_id": 1
}
```

所有孵化配置（产出物、技能池）均存储在 `beast_template` 中。

> 繁育产出的兽卵额外携带 `properties.inheritedTraits`（父代变异特性 ID 列表），孵化时注入后代。

### 1.5 变异特性配置表 (mutation_trait_config)

| 字段                | 类型           | 说明                                           |
|-------------------|--------------|----------------------------------------------|
| `id`              | BIGSERIAL    | PK                                           |
| `name`            | VARCHAR(64)  | 特性代码（唯一标识，如 SHARP_FANG）                      |
| `chinese_name`    | VARCHAR(32)  | 中文名称                                         |
| `description`     | VARCHAR(256) | 效果描述                                         |
| `category`        | VARCHAR(32)  | 分类（ATTACK/DEFENSE/SPEED/PRODUCTION/BREAKTHROUGH/EXP/BREEDING/COMBAT） |
| `effects`         | JSONB        | 效果数组，支持条件效果                                  |
| `required_tags`   | JSONB        | 所需tags（null=通用，需灵兽tags全部包含）                  |
| `required_quality`| VARCHAR(32)  | 最低品质要求（null=无限制）                             |
| `is_active`       | BOOLEAN      | 是否启用                                         |
| `sort_order`      | INT          | 排序顺序                                         |

**effects JSONB 结构**：

```json
// 简单数值增益
[{"type": "ATTACK_PERCENT", "value": 15}]

// 带触发条件
[{"type": "LOW_HP_ATTACK_BOOST", "value": 50, "condition": {"trigger": "HP_BELOW", "threshold": 30}}]
```

**特殊词条筛选逻辑**：

- `required_tags` 为 null → 通用词条，所有灵兽可刷出
- `required_tags` 非 null → 灵兽模板tags必须**全部包含**required_tags
- `required_quality` 非 null → 灵兽品质必须 >= required_quality

### 1.6 与兽栏的关系

- 兽卵在兽栏中孵化，孵化完成后生成 `beast` 记录
- 灵兽通过 `penned_cell_id` 关联到具体的兽栏地块
- `penned_cell_id = null` 表示灵兽在栏外休憩（兽栏被拆除时）
- 兽栏地块通过 `config.beast_id` 引用 Beast 实体

### 1.7 灵兽种族

灵兽共 **209 种**（种子数据），按元素/主题分为 11 组，每组 19 只（通用 9 + 稀有 6 + 史诗 3 + 传说 1）。

| 组 | 主题 | 传说灵兽 |
|---|---|---|
| 火行 | 火系攻击 | 朱雀 |
| 水行 | 水系治疗/防御 | 玄冥 |
| 木行 | 木系治疗/自然 | 青龙 |
| 金行 | 金系破甲/暴击 | 太白金星兽 |
| 土行 | 土系防御/力量 | 黄龙 |
| 冰行 | 冰系控制/冻结 | 玄冰螭龙 |
| 雷风 | 雷风速度/眩晕 | 应龙 |
| 飞禽 | 飞行/远程 | 大鹏金翅鸟 |
| 蛇蛟龙 | 蛇蛟龙族 | 烛龙 |
| 祥瑞 | 增益/治疗/智慧 | 麒麟 |
| 凶兽 | 高伤/吸血/混沌 | 混沌 |

**等阶与 Tag 数量**：通用灵兽 2 tags，稀有 2-3 tags，史诗 3-4 tags，传说 4-5 tags。高等灵兽天然融合多元素（如黑水玄蛇 = beast+ice+water，应龙 = dragon+thunder+wind+flying+myth）。

**梗兽**：包含 17 只严肃修仙风格的梗兽，如千年老龟（"活了不知几千年，壳上长满了青苔"）、摸鱼鲲（"北冥有鱼，其志不在化鹏"）、内卷蛟（"日夜不休修炼只为早一日化龙"）、躺平貘（"食梦而眠，无欲则刚"）等。

### 1.8 兽卵档次 vs 灵兽品质

| 概念 | 决定方式 | 影响 |
|------|----------|------|
| 兽卵档次 | 种子数据固定（common/uncommon/rare/epic/legendary 标签） | 技能池深度、产出物档次 |
| 灵兽品质 | 孵化时权重随机（凡品→神品，受好感/兽栏影响） | 攻防倍率、产出倍率、恢复时间 |

**两者独立**：传说档次的夔牛卵孵化品质仍可为凡品；普通档次的灵猫卵若好感够高也可能孵出神品。蛋的档次决定"学什么技能、产什么东西"，品质决定"技能效果多强、产出多少"。

---

## 2. 灵兽品质 (BeastQuality)

### 2.1 品质表

| 品质            | 属性倍率 | 产出倍率 | 恢复分钟 | 孵化权重 |
|---------------|------|------|------|------|
| 凡品 (MORTAL)   | 0.8× | 1.0× | 30   | 600  |
| 灵品 (SPIRIT)   | 1.0× | 1.2× | 60   | 250  |
| 仙品 (IMMORTAL) | 1.3× | 1.5× | 120  | 100  |
| 圣品 (SAINT)    | 1.6× | 2.0× | 240  | 40   |
| 神品 (DIVINE)   | 2.0× | 3.0× | 480  | 10   |

恢复分钟：灵兽 HP 归零后自动进入休养状态，该时长后 `recoveryUntil` 过期，可重新出战。

> 枚举另含未使用的 `auraCostMultiplier`、`lifespanMultiplier` 字段（当前无调用方）。

### 2.2 品质孵化规则

孵化时品质由以下因素共同决定（权重总分 1000）：

- **基础权重**：按上表权重随机
- **地灵好感**：每 100 点好感提升一档品质下限（好感 100 则不会孵出凡品，好感 200 不会孵出灵品，以此类推；被淘汰品质的权重累加到最低保留品质）
- **兽栏等级**：每级将 10 点凡品权重转移给神品（Lv5 兽栏可让神品权重从 10 增至 60，转移量以凡品权重为上限）

品质升阶时不变，仅在升阶成功时有 10% 概率连带提升一级（灵悟特性 `QUALITY_UP_CHANCE +10` 可让此概率升至 20%）。

---

## 3. 等阶系统

### 3.1 等阶命名

| 数值 | 名称 | 含义 | 变异特性槽（实际实现） |
|------|------|------|------|
| 1 | 通灵 | 初醒通灵，灵智初开 | 3 |
| 2 | 凝魄 | 魂魄凝聚，形体渐固 | 3 |
| 3 | 化形 | 化形换骨，脱胎换貌 | 4 |
| 4 | 渡劫 | 历经雷劫，洗炼凡躯 | 4 |
| 5 | 归真 | 返璞归真，半步踏入仙道 | 5 |

> 原文档 §3.1 的「特性槽 1/1/2/2/3」与 §7.1 的「3/3/4/4/5」自相矛盾；代码实现为后者。

### 3.2 等阶确定

- **孵化**：所有兽卵孵出等阶统一为 **通灵(Tier 1)**，不受兽卵品种影响
- **升阶**通过 `manageBeast(EVOLVE)` 逐步提升，最高至归真(Tier 5)
- 等阶影响：HP上限、等级上限、产出量、特性槽数

---

## 4. 核心公式

### 4.1 修为与等级

```
孵化消耗 = 400 灵石（所有兽卵统一）
孵化时间 = 32h / 兽栏等级加速系数
产出间隔 = 4h / (兽栏等级加速系数 × 灵兽实际等阶)   ← 文档写法
实际实现：产出间隔 = 4h / 加速系数(兽栏等级, 灵兽等阶)
每轮产出 = round(tier × 品质产出倍率 × (1 + rand(0,tier)) / 2) 件
HP最大值 = tier × 200 + (level - 1) × tier × 20
升级所需修为 = 50 × level^1.5
等级上限 = tier × 10 + 10
攻击 = round((10 + (level - 1) × 3 × q) × q)     (q = 品质属性倍率)
防御 = round((8 + (level - 1) × 2 × q) × q)
速度 = level × 2 + 8
```

其中兽栏等级加速系数（`FudiHelper.getLevelSpeedMultiplier(cellLevel, minRequired)`）：

```
cellLevel < minRequired → 0.5
否则                   → 1.0 + (cellLevel - minRequired) × 0.15
```

- 孵化时 `minRequired = 1`（tier 固定为 1）；
- 产出时 `minRequired = 灵兽等阶`（兽栏等级低于等阶时速度为 0.5，即间隔翻倍）。

### 4.2 属性重算触发时机

| 时机 | 动作                               |
|----|----------------------------------|
| 升级 | 攻防按公式重算                          |
| 升阶 | tier 加 1, levelCap 更新, 属性重算, HP回满    |
| 品质连带提升 | quality 升级, 品质倍率 q 变化, 属性重算 |

---

## 5. 灵兽状态

### 5.1 状态机

```
孵化中 → 正常（在栏） → 出战 → 战斗受伤 → 回栏休养 → 正常（在栏）
                ↓
            栏外休憩（兽栏拆除）
```

### 5.2 状态说明

| 状态     | 条件                                    | 产出 | 天劫防御 | HP恢复   |
|--------|---------------------------------------|----|------|--------|
| 孵化中    | `cell.matureTime` 在未来                  | ✗  | ✗    | —      |
| 正常（在栏） | HP满，非出战                               | ✓  | ✗    | 自动回血 |
| 出战     | `beast.is_deployed = true`            | ✗  | ✓    | —      |
| 休养中    | HP < max，或 `beast.recoveryUntil` 未过期 | ✗  | ✗    | 自动回血 |
| 栏外休憩   | `beast.penned_cell_id = null`         | ✗  | ✗    | 可出战（见差异） |

### 5.3 出战规则

- **出战上限**：固定 2 只（同一福地下的灵兽计数，行锁防并发超限）。
- **出战/召回**：通过地灵工具 `manageBeast(DEPLOY)` toggle 操作（需要兽栏地块编号定位灵兽）。
- **可出战略**：满足以下全部条件时方可出战：HP 大于 0、`recovery_until` 为 null 或已过期。
- **recovery_until**：可为 null（表示无休养状态），过期后自动视为可出战。
- **HP归零**：战斗结束后灵兽自动取消出战，`recoveryUntil` 设置为 now + 品质恢复分钟数。
- **自动回血**：灵兽在福地中且未出战时，每次「访问福地」（福地/福地地块/地灵交互等触发福地 touch 的操作）自动恢复 1% 最大 HP（至少 1 点）。
- **召回**：手动召回出战灵兽；出战中不结算产出，召回后按产出计时补算（出战期间经过的时间计入）。

### 5.4 栏外休憩

拆除兽栏时灵兽 **不会消失**，而是退为"栏外休憩"状态（`penned_cell_id = null`）：

- 不可生产
- 仍计入 `fudi_id` 下的出战数量统计（上限校验按 fudi_id 查询全部灵兽）
- 由于出战/召回入口需要兽栏地块编号，栏外休憩灵兽当前没有可用指令路径重新出战（见差异）
- 新建兽栏后可重新入栏（孵化新灵兽占用新兽栏）

---

## 6. 进化（升阶）

### 6.1 前置条件

进化前灵兽必须达到 **等级上限**（`level >= levelCap`），否则无法进化。

### 6.2 升阶

- 消耗：`(currentTier + 1) × 200` 灵石
- 成功率：85% + 好感加成（`min(15, 好感 / 7)`，最高 +15%）
- 10%概率连带品质提升一级（灵悟特性可使此概率 +10%，即升至 20%）；神品不再提升
- 失败：灵石不退
- 效果：tier 加 1, levelCap 更新, 属性重算, HP 回满, 解锁对应等阶先天技, 15%概率触发变异
- 等阶上限：最高归真(Tier 5)
- 错误码 `BEAST_TIER_REQUIRES_PEN` 已定义但无调用方（当前无兽栏等阶门槛校验）

---

## 7. 变异系统 (MutationTrait)

### 7.1 特性槽位

| 等阶 | 最大槽位 |
|------|------|
| 通灵/凝魄 | 3 |
| 化形/渡劫 | 4 |
| 归真 | 5 |

特性达到上限时不会再触发新的变异；已拥有的特性不会重复获得。

### 7.2 触发概率

| 触发   | 概率  |
|------|-----|
| 孵化   | 5%  |
| 升阶   | 15% |

### 7.3 特性配置

变异特性数据存储在 `mutation_trait_config` 表中，通过 `effects` JSONB 字段定义效果。特性按以下分类组织：

- **ATTACK**：攻击系（锐齿、煞气）
- **DEFENSE**：防御系（厚皮、玄甲）
- **SPEED**：速度系（疾走、追电）
- **PRODUCTION**：产出系（高产、勤勉、稀产）
- **BREAKTHROUGH**：突破系（灵悟）
- **EXP**：修为系（噬灵）
- **BREEDING**：繁育系（孕灵、多产、血脉觉醒）
- **COMBAT**：战斗系（自愈、反击、格挡、吸血、狂暴）

### 7.4 特殊词条筛选

部分词条需要特定灵兽类型或品质才能刷出：

| 词条 | 所需tags | 最低品质 |
|------|---------|---------|
| 煞气 | beast + dragon | IMMORTAL |
| 玄甲 | beast + dragon | SAINT |
| 追电 | flying | IMMORTAL |
| 狂暴 | beast + dragon | SAINT |

筛选逻辑：灵兽模板 (`beast_template.tags`) 必须**全部包含**所需tags，且品质 >= 最低品质。

### 7.5 效果计算

所有变异效果通过 `MutationEffectResolver` 统一查询和计算，支持：

- 无条件效果：直接叠加数值（`sumEffectValue`）
- 条件效果：如 HP_BELOW/ON_ATTACKED/ON_HIT 等触发条件（`getConditionalEffects` / `getEffectsByTrigger`）
- 同类效果可叠加（如同时拥有锐齿和煞气则攻击+40%）

`MutationEffectType` 共 43 种效果类型，覆盖数值增益、繁育、战斗触发等；`TriggerType` 支持 HP_BELOW、ON_KILL、ON_HIT、ON_ATTACKED、ON_DEATH、FIRST_N_TURNS。

战斗侧实际消费的效果（`BeastCombatant` / `PostCombatProcessor`）：攻击/防御/速度百分比、低气血攻击加成（HP_BELOW）、战斗结束回血等。

---

## 8. 灵兽繁育

### 8.1 基本规则

- 两只成年灵兽（≥ T2 化形）在兽栏中交配，产出兽卵放入背包
- 要求一阴一阳，同性不可繁育
- 繁育后双方进入冷却：24 小时 × (1 - 繁育冷却减少效果)，下限 1 小时
- 消耗灵石：200 + (双方 tier 之和) × 50
- 灵兽需 HP 满（恢复中不可繁育），且不在冷却中

### 8.2 性别

每只灵兽孵化时随机分配性别：`YIN`（阴 ♀）或 `YANG`（阳 ♂）。性别不影响战斗属性，仅用于繁育。

### 8.3 Tag 匹配系统

每种灵兽模板 (`beast_template`) 有 tags，如 `["flying", "fire", "wind"]`。

繁育时：

1. 提取父母双方灵兽模板的 tags（去掉稀有度标签 `beast_egg/common/uncommon/rare/epic/legendary`）
2. 合并去重
3. 查 `breeding_recipe` 表：`required_tags` 是合并集合的子集 → 命中
4. 多条命中 → 按 `weight` 加权随机
5. 无命中 → 随机继承一方的兽卵种类（按名称回推「X兽卵 / X卵」）

**Tag 梯度**：

- 通用灵兽：2 tags（如 `["beast", "fire"]` 火鼠）
- 稀有灵兽：2-3 tags（如 `["beast", "fire", "earth"]` 熔岩龟）
- 史诗灵兽：3-4 tags（如 `["beast", "fire", "wood"]` 毕方）
- 传说灵兽：4-5 tags（如 `["flying", "fire", "wood", "auspicious"]` 朱雀）

**配方示例**：

| 父母 tag 组合 | 可能产出 |
|-------------|---------|
| beast + fire | 火蟾、炎狼、火蝎 |
| flying + fire + wind | 烈焰雀 |
| dragon + fire + wood | 毕方 |
| phoenix + fire + wood + auspicious | 朱雀 |
| dragon + thunder + wind + flying | 应龙 |

### 8.4 品质遗传

```
avg = (父方品质序号 + 母方品质序号) / 2
roll = random(0.0, 1.0)                // 只升不降
roll += 繁育品质提升效果 / 100          // 孕灵 BREED_QUALITY_BOOST +15
结果 = clamp(round(avg + roll), 0, 4)
```

> 原文档 `random(-0.5, +1.0)` 与「FERTILE roll +0.3」均与代码不符；实际 roll 区间为 [0,1)，孕灵等词条按 +15% 概率值叠加。

### 8.5 词条继承

- 遍历父母双方的变异词条（所有词条都可遗传，合并去重）
- 每个词条基础 25% 概率遗传；`BLOOD_AWAKEN`（血脉觉醒，`INHERIT_RATE_BOOST +25`）可提升至上限 50%
- 最多继承 3 个（超出随机保留 3 个）
- 继承结果随兽卵 `properties.inheritedTraits` 持久化，孵化时注入（受后代特性槽位上限约束）

---

## 9. 兽栏产出机制

### 9.1 设计定位

兽栏产出是 **稳定、持续的低/中阶药材来源**，与炼丹系统形成闭环。高阶药材通过灵田种植、地图探索等途径获取，兽栏不越位。

### 9.2 产出物配置

产出物由灵兽模板 (`beast_template.production_items`) 决定（代码经兽卵 properties 解析灵兽模板后读取）。**灵兽不产出 epic/legendary 材料**（高阶材料通过灵田种植、地图探索获取）。

| 灵兽等阶 | 产出物品阶 | 示例 |
|--------|---------|------|
| 通用 | common herb + common material | 灵芝、茯苓、兽骨、朱砂、玄铁矿石 |
| 稀有 | uncommon herb + uncommon material | 地火芝、蛇涎果、寒铁、灵蚕丝、月华露 |
| 史诗 | rare herb + rare material | 龙血草、冰魄花、玄晶、天雷竹、寒髓晶 |
| 传说 | rare herb + rare material | 赤炎花、万载玄冰、雷精矿石、天外陨铁 |

**灵兽模板配置示例**：

```json
{
  "grow_time": 24,
  "production_items": [
    {"template_id": 1, "weight": 70},
    {"template_id": 2, "weight": 30}
  ],
  "skill_pool": {
    "innate_skills": [
      {"skill_id": 101, "unlock": "BIRTH"},
      {"skill_id": 102, "unlock": "TIER_2"},
      {"skill_id": 103, "unlock": "TIER_3"},
      {"skill_id": 104, "unlock": "TIER_4"},
      {"skill_id": 105, "unlock": "TIER_5"}
    ],
    "awakening_skills": [
      {"skill_id": 110, "weight": 50},
      {"skill_id": 111, "weight": 30}
    ]
  }
}
```

每轮产出时，从 `production_items` 中按权重随机选取物品；`RARE_ITEM_CHANCE` 触发时额外产出一件低权重（视为更高档）物品。

### 9.3 产出流程

1. **累积阶段**：成熟后按产出间隔计算可结算轮数（轮数 = 经过秒数 / 间隔秒数），每轮按公式产出并写入 `fudi_cell.config.production_stored`；间隔可被 `OUTPUT_INTERVAL_REDUCE` 缩短
2. **收取阶段**：玩家通过地灵工具 `collectProduce` 收取（单个地块或 `all` 批量），将物品实际写入背包（`inventory_item`）
3. **背包满**：产出暂存在 `production_stored`，不消失，下次收取时一并发放
4. **上限**：`production_stored` 最多累积 `tier × 20` 件，超出不再产出（仍推进计时）
5. **出战期间不结算产出**：出战中的灵兽不更新 `production_stored`；产出计时（`last_production_time`）不清零，召回后按经过时间一次性补算（出战时段计入 elapsed）。

### 9.4 产出存储格式

`production_stored` 存储为物品列表：

```json
[
  {"template_id": 1, "name": "灵草", "quantity": 5},
  {"template_id": 2, "name": "黄精", "quantity": 2}
]
```

---

## 10. 等级成长

### 10.1 修为获取

| 来源       | 修为         | 说明            |
|----------|-------------|---------------|
| 灵兽精华喂养   | `精华 × 50`   | 放生灵兽获得精华，`使用 灵兽精华 [数量] [兽栏编号]` 喂养 |
| 出战战斗     | —（未接线）      | 设计为 `怪物等级 × 10`，当前无调用方 |
| 历练结算     | —（未接线）      | 设计为 `历练分钟 × 2`，当前无调用方 |

噬灵特性（`EXP_PERCENT +25`）在 `addBeastExp` / `addExpToDeployedBeasts` 中生效，但这两个方法当前无调用方；精华喂养路径直接加修为，不乘噬灵加成。

### 10.2 升级公式

升级所需修为 = 50 × level^1.5
等级上限 = tier × 10 + 10

| 等阶 | 等级上限 |
|------|------|
| 通灵 | 20 |
| 凝魄 | 30 |
| 化形 | 40 |
| 渡劫 | 50 |
| 归真 | 60 |

达到等级上限后，修为最多保留一级所需量，无法继续升级；喂养满级灵兽会被拒绝。

### 10.3 属性成长

攻击 = round((10 + (level - 1) × 3 × q) × q)
防御 = round((8 + (level - 1) × 2 × q) × q)
HP最大值 = tier × 200 + (level - 1) × tier × 20
速度 = level × 2 + 8

其中 `q = 品质属性倍率`（凡品 0.8, 灵品 1.0, 仙品 1.3, 圣品 1.6, 神品 2.0）。

每升一级：攻防按公式增长，HP +tier×20（线性）。
战斗时攻击/防御/速度类变异特性额外叠加（`BeastCombatant` 按百分比乘算）。

### 10.4 进化与等级

**进化保留等级**，进化本身提供额外的 tier 跃升加成：

- 升阶后等级不变，等级上限随 tier 提升
- 品质连带提升后等级不变，属性倍率 q 提升
- 属性按新 tier/q 重新计算
- 若当前等级超过新上限，等级不变但无法继续获取修为

---

## 11. 技能系统

### 11.1 设计理念

每个灵兽品种有自己的 **技能池**，技能与品种主题一致（火系灵兽配火系技能）。技能通过 **进化解锁** 和 **战斗觉醒** 获取，不使用道具学习。

灵兽实体中的技能字段 `skills` 存储为技能 ID 列表（`List<Long>`），组建战斗队伍时通过技能仓库按 ID 批量加载技能完整数据。

### 11.2 技能池配置

每个灵兽模板 (`beast_template`) 配置两组技能池：

**先天技池** — 进化解锁，稳定获取：

```json
{
  "innate_skills": [
    {"skill_id": 101, "unlock": "BIRTH"},
    {"skill_id": 102, "unlock": "TIER_2"},
    {"skill_id": 103, "unlock": "TIER_3"},
    {"skill_id": 104, "unlock": "TIER_4"},
    {"skill_id": 105, "unlock": "TIER_5"}
  ]
}
```

**后天悟池** — 战斗觉醒，随机获取：

```json
{
  "awakening_skills": [
    {"skill_id": 110, "weight": 50},
    {"skill_id": 111, "weight": 30},
    {"skill_id": 112, "weight": 20}
  ]
}
```

### 11.3 先天技 — 进化解锁

灵兽进化时自动解锁对应技能，无需概率判定：

| unlock 条件       | 枚举值    | 含义            |
|-----------------|--------|---------------|
| `BIRTH`         | BIRTH  | 孵化即拥有         |
| `TIER_2`        | TIER_2 | 升阶到凝魄解锁 |
| `TIER_3`        | TIER_3 | 升阶到化形解锁 |
| `TIER_4`        | TIER_4 | 升阶到渡劫解锁 |
| `TIER_5`        | TIER_5 | 升阶到归真解锁 |

unlock 字段使用 `SkillUnlock` 枚举，存储为 `UPPER_SNAKE_CASE` 格式。

### 11.4 后天悟 — 战斗觉醒

在战斗高光时刻，灵兽有概率从后天悟池中随机领悟一个技能。

**触发条件**（满足其一）：

- 灵兽在战斗中击杀最后一击的怪物
- 灵兽 HP 降至 0 但战斗最终胜利
- 战斗被标记为高光战斗（势均力敌、回合数多）

**觉醒概率**：15%（每场战斗最多判定一次）

**觉醒流程**：按权重从后天悟池中随机选取一个**未拥有**的技能（已习得的从池中排除），技能上限满 4 个时不再觉醒。

> 实际实现：战斗胜利后对每只参战灵兽调用一次觉醒判定（不区分高光/击杀者，高光参数未参与判断）。

### 11.5 技能上限与替换

- 灵兽技能上限：**4 个**
- 满 4 个时无法再解锁新技能（觉醒路径检查；先天技解锁路径不去重上限）
- 放生灵兽可获得灵兽精华，用于喂养其他灵兽

### 11.6 技能成长路线

```
孵化 → 先天技 ×1 (birth)
  │
  ├─ 升阶凝魄 → 先天技 ×1
  ├─ 升阶化形 → 先天技 ×1
  ├─ 升阶渡劫 → 先天技 ×1
  ├─ 升阶归真 → 先天技 ×1
  │
  ├─ 战斗觉醒 → 后天悟 ×0~1 (概率)
  │
  └─ 品质连带提升 → 先天技 ×1
```

一只完全体灵兽最多可通过进化获得 **6 次先天技解锁机会**，但技能上限为 4 个，实际承载技能数受上限约束。后天悟最多 1 个。

> 代码中「品质连带提升」调用 `unlockInnateSkills(beast, "quality_break")`，但 `SkillUnlock` 无此枚举值，因此该路径实际不会解锁任何技能（见差异）。

---

## 12. 灵石消耗一览

| 操作      | 灵石消耗               |
|---------|--------------------|
| 孵化灵兽    | 400（所有兽卵统一） |
| 灵兽升阶    | `(currentTier + 1) × 200` |
| 灵兽繁育    | `200 + (双方 tier 之和) × 50` |

---

## 13. 玩家交互

### 13.1 地灵操作（通过 Spirit 工具）

灵兽操作统一通过地灵对话完成，**没有独立指令入口**（无「灵兽」「灵兽列表」指令）。

| 操作 | 工具 | 说明 |
|------|------|------|
| 出战/召回 | `manageBeast(DEPLOY)` | toggle：已出战则召回，未出战则出战；position=兽栏地块编号 |
| 进化 | `manageBeast(EVOLVE)` | 消耗灵石升阶 |
| 放生 | `manageBeast(RELEASE)` | 放生灵兽，获得灵兽精华（孵化中不可放生；出战中需先召回） |
| 孵化 | `manageBeast(HATCH)` | value=兽卵名称 |
| 繁育 | `breedBeasts(pos1, pos2)` | 两只灵兽交配产出兽卵 |

### 13.2 查询入口

| 入口 | 说明 |
|------|------|
| `福地地块` | 兽栏显示：灵兽名、品质、等阶、变异特质、累计产出、是否孵化中 |
| 地灵对话 | AI 上下文包含灵兽名、性别、等阶、品质、修为、繁育冷却等，可自然语言查询 |

### 13.3 物品使用

| 指令 | 说明 |
|------|------|
| `使用 灵兽精华 [数量] [兽栏编号]` | 喂养指定兽栏的灵兽，按数量消耗精华转化为修为（50/份）；数量与编号均必填 |

### 13.4 放生与精华

放生灵兽获得灵兽精华数量 = `tier × 5 + 品质加成`，品质加成为 MORTAL 0 / SPIRIT 2 / IMMORTAL 5 / SAINT 10 / DIVINE 20。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- 表名无 `xt_` 前缀：实际为 `beast_template`、`beast`、`mutation_trait_config`、`breeding_recipe`、`fudi_cell`。
- 产出机制：间隔 = `4h / 加速系数`；每轮 = `round(tier × 品质产出倍率 × (1 + rand(0,tier)) / 2)`，叠加 `OUTPUT_PERCENT` 后至少 1 件；暂存上限 `tier × 20`。
- 特性槽位实际 3/3/4/4/5（通灵/凝魄 3，化形/渡劫 4，归真 5）；§3.1 的 1/1/2/2/3 为旧稿。
- 繁育数值：孕灵 `BREED_QUALITY_BOOST +15`；词条继承基础 25%、血脉觉醒上限 50%、最多 3 个；放生精华 = `tier × 5 + 品质加成`。
- 「灵兽」「灵兽列表」指令不存在，操作与查看走地灵工具与「福地地块」；`BeastQuality` 的 `auraCostMultiplier` / `lifespanMultiplier` 无调用方（预留字段）。

### B. 保留代码设计

- **产出间隔由「兽栏等级相对等阶」决定**：`4h / levelSpeed`，兽栏低于等阶减半、高于等阶每级 +15%。旧稿「×等阶」会让高等阶无条件更快、梯度失真；现设计让升级兽栏持续有收益，与灵田一致。
- **繁育品质只升不降**：`roll ∈ [0,1)` 只加不减（旧稿 -0.5~+1.0 会掉品质），失败代价更小、更公平。
- **繁育冷却可被特性缩短**：`24h × (1 - 减免)`，下限 1h，繁育系特性有实际收益，繁育 build 有深度。
- **觉醒条件简化为「战斗胜利 15%」**：不区分击杀者/高光；「势均力敌、回合数多」对玩家不可感知，扁平概率更可预期（spec 已按此描述）。
- **精华喂养必填数量与兽栏编号**：显式指定数量与目标，避免旧稿「编号可选、消耗全部」的误操作与歧义。

### C. 按设计修正（待修）

- **孵化时长应按模板 `grow_time` 计算**（小）：种子 `grow_time` 按档次递进（12~168h），代码硬编码 32h 使配置失效；按模板读取可与灵田/种子系统一致，并让兽卵档次体现在等待成本上。

### D. 未实现（待办）

无。

> 已实现（本轮）：战斗（怪物等级 × 10）与历练（分钟 × 2）均向出战灵兽发放修为，噬灵 `EXP_PERCENT` 在两条路径生效；中途结算与最终结算逐段累计不重复。

### E. 缺陷修复

- **`beast.template_id` 写入错误**（已修）：孵化改用兽卵 `properties.beast_template_id` 解析 `BeastTemplate` 并写入其 ID，技能池/变异/繁育解析路径统一。
- **先天技解锁从未生效**（已修）：`unlockInnateSkills` 改为按 `SkillUnlock` 枚举 code 比较，调用方传 `BIRTH`/`TIER_N`；技能列表复制为可变列表后再解锁。
- **栏外休憩灵兽无法再出战且占用出战名额**（已修）：拆除兽栏时同时解除出战（释放 2 只上限）；新增地灵工具动作 `PEN`（栏外灵兽入栏），栏外休憩可回归兽栏。
- **自动回血无时间门槛**（已修）：`tryAutoHeal` 以上次保存时间为基准，间隔 ≥5 分钟才回 1%，反复访问福地无法快速刷满。
