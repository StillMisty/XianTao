# 炼丹系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 核心概念

### 1.1 五行属性 (ElementType)

| 枚举值 | code | 中文 |
|-------|------|------|
| METAL | METAL | 金 |
| WOOD | WOOD | 木 |
| WATER | WATER | 水 |
| FIRE | FIRE | 火 |
| EARTH | EARTH | 土 |

- `fromCode` 大小写不敏感（`equalsIgnoreCase`），未知 code 抛 `IllegalArgumentException`；`displayName` 对未知编码原样返回。
- 药材的五行值存于物品模板/实例 `properties` JSONB 的 `elements` 映射中：

```json
{"elements":{"wood":3,"fire":1,"water":2}}
```

- 只存非零值，missing key 视为 0。
- 代码读取大小写不敏感（`StackableItem.getElementValue`），丹方/图纸 requirements 在解析时统一为大写键（`ItemProperties.normalizeRequirementKeys`），与枚举 code 对齐。

### 1.2 丹方数据

丹方数据直接存于物品模板（`item_template`）的 JSONB `properties` 中，由 `ItemProperties.Scroll` 强类型承载，不单独建表：

```json
{
  "recipe": {
    "grade": 3,
    "result_item_id": 123,
    "result_quantity": 1,
    "requirements": {
      "wood": {"min": 10, "max": 15},
      "water": {"min": 6, "max": 9},
      "fire": {"min": 5, "max": 7}
    }
  }
}
```

| 字段 | 说明 |
|------|------|
| `grade` | 丹药品阶（数据约定 1~9，代码不校验上限） |
| `result_item_id` | 成品丹药模板 ID |
| `result_quantity` | 成丹数量 |
| `requirements` | 五行要求，key 为元素名，value 为 `min`/`max`；`max=0` 视为无上限 |

丹方卷轴物品类型为 `RECIPE_SCROLL`（丹方卷轴），命名如「天元丹方」，使用后玩家学会该丹方。

### 1.3 玩家已学丹方 (player_pill_recipe)

表名 `player_pill_recipe`（无 `xt_` 前缀）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | FK → `player(id)`，ON DELETE CASCADE |
| `recipe_template_id` | BIGINT | FK → `item_template(id)`，丹方卷轴模板 |
| `result_item_id` | BIGINT | FK → `item_template(id)`，成品丹药模板 |
| `learn_time` | TIMESTAMP | 学习时间，默认 NOW() |

UNIQUE(user_id, recipe_template_id)：不可重复学习。另有 `user_id`、`recipe_template_id`、`result_item_id` 三个索引。

### 1.4 丹药品阶与成色

丹药实例存储于 `inventory_item`，通过 JSONB `properties` 记录品阶与成色：

```json
{"grade": 3, "quality": "SUPERIOR"}
```

| 概念 | 叫法 | 说明 |
|------|------|------|
| **品阶** | 一品~九品 | 丹方固有，决定药效基础值，影响等级衰减 |
| **成色** | 上成/中成/下成 | 炼制时由五行匹配度决定 |

`PillQuality` 枚举：

| 枚举值 | code | 中文 | 效果倍率 |
|--------|------|------|---------|
| SUPERIOR | SUPERIOR | 上成 | ×1.5 |
| NORMAL | NORMAL | 中成 | ×1.0 |
| INFERIOR | INFERIOR | 下成 | ×0.7 |

成色阈值：平均达标度 ≥ 0.8 → 上成；≥ 0.5 → 中成；否则下成。`fromCode` 对未知值抛异常；商店购买的丹药实例没有品质属性时按中成（NORMAL）处理。

### 1.5 效果计算中的品阶来源（实际实现）

服用丹药计算等级衰减时，品阶并不直接取丹方 `grade`，而是由 `PillConsumptionService.getPillGrade` 按**成品丹药模板的 tags** 映射：

| tag | 品阶取值 |
|-----|---------|
| `legendary` / `mythic` | 50 |
| `grand` | 35 |
| `epic` | 30 |
| `advanced` | 20 |
| `rare` / `intermediate` | 10 |
| `critical` | 8 |
| `basic` | 5 |
| `entry` | 3 |
| 无匹配 tag | 回退实例 `properties.grade`；再兜底 3 |

炼制时写入实例的 `properties.grade`（丹方品阶）仅在模板无 tags 时参与计算。

## 2. 药材体系

### 2.1 药材品阶梯度

| 品阶 | 产地 | 单药材五行值 |
|------|------|------------|
| 凡阶 | 低阶地图、一级福地 | 1~2 |
| 灵阶 | 中阶地图、三级福地 | 2~4 |
| 玄阶 | 高阶地图、五级福地 | 4~7 |
| 地阶 | 特殊副本 | 6~10 |
| 天阶 | 世界 Boss、活动 | 9~15 |

### 2.2 药材获取

- 灵田种植（种子 → 药材）
- 地图探索掉落
- 悬赏奖励
- NPC 商店购买

## 3. 炼丹流程

### 3.1 药材槽位

炼丹炉最多支持 **5 个药材位**（`MAX_HERB_TYPES = 5`），每个位置放一种药材（可用数量 >1，合并计算五行）。自动与手动模式都受 5 种上限约束，超出报「最多只能使用5种药材」。

### 3.2 五行累加

```
累计木 = Σ(药材A 木 × 数量A + 药材B 木 × 数量B + ...)
累计火 = Σ(药材A 火 × 数量A + 药材B 火 × 数量B + ...)
```

代码按键累计到 `Map<String,Integer>`，`max=0` 的属性按无上限处理。

### 3.3 成丹判定

遍历玩家已学丹方，检查累计五行是否同时在丹方要求的范围内：

```
metal_min ≤ 累计金 ≤ metal_max
wood_min ≤ 累计木 ≤ wood_max
water_min ≤ 累计水 ≤ water_max
fire_min ≤ 累计火 ≤ fire_max
earth_min ≤ 累计土 ≤ earth_max
```

全部满足 → 成丹。无一匹配 → 失败，**药材不消耗**（手动模式只有在命中丹方时才扣减；自动模式在判定失败时直接报错）。

### 3.4 成色判定

每行属性计算达标度：

```
达标度 = 1 - |实际值 - 范围中心| / 范围半宽
范围中心 = (max + min) / 2
范围半宽 = (max - min) / 2（精确值时达标度为 1）
```

单项达标度下限截断为 0（超出范围记 0）。取所有涉及属性的达标度平均值：

| 平均达标度 | 成色 | 效果倍率 |
|-----------|------|---------|
| ≥ 0.8 | 上成 | ×1.5 |
| ≥ 0.5 | 中成 | ×1.0 |
| < 0.5 | 下成 | ×0.7 |

丹方不涉及的属性不参与成色计算。

## 4. 自动炼丹算法

自动炼丹（`炼方 [丹方名]`）流程：

1. 按名称模糊匹配玩家已学丹方（`template.getName().contains(输入)`，取第一个命中）；未命中时若背包中有同名丹方卷轴，提示先「使用」学习；否则报「未找到丹方」。
2. 筛选玩家背包中所有 `HERB` 类型物品；背包无药材报「背包中没有药材」。
3. 共享贪心算法 `CombinationStrategy` 在最多 5 种药材内搜索组合（每种药材可用多份，按实例 ID 记录用量）：
   - 每轮为未达 `min` 的属性挑选「对整体缺口贡献最大」的药材与份数，份数按 `ceil(缺口/单份值)` 计算且不超过剩余量；
   - 已在用的药材种类数达到 5 时不再引入新种类；多轮迭代直到无进展。
4. 五行累计值未覆盖全部 `min` → 报「缺少药材属性：<中文名列表>」，不消耗药材。
5. 任一属性超过 `max` → 报「药材属性超过上限：<中文名>」，不消耗药材。
6. 通过判定后计算成色，按实例 ID 精确扣减药材，生成丹药实例并入库。

## 5. 手动炼丹算法

手动炼丹（`炼 [药材名数量 ...]`）流程：

1. 输入按空白拆分，每段需满足「非数字名称 + 正整数数量」格式（如 `灵草3`）；输入条目数超过 5 报「最多只能使用5种药材」。
2. 逐段在背包 `HERB` 中按名称模糊匹配（`contains`），未解析或未命中的条目被静默跳过；全部无效报「药材输入格式错误，请使用 药材名数量 格式，如 灵草3」。
3. 按 `ElementType.getCode()` 累计五行到 `elementTotals`。
4. 遍历已学丹方，取第一个五行全部落在范围内的丹方，计算成色。
5. 命中后按输入逐条原子扣减药材（数量不足时扣减报错并整体回滚），生成丹药实例。
6. 无任何丹方匹配报「药材五行不匹配任何丹方」，不消耗药材。

## 6. 丹药效果系统

### 6.1 效果定义

丹药效果存储在物品模板的 JSONB 属性中，由 `ItemProperties.Potion(List<Effect>)` 承载：

```json
{
  "effects": [
    {"type": "exp", "amount": 500},
    {"type": "hp", "amount": 0, "percentage": 0.3},
    {"type": "stat", "statAttr": "STR", "amount": 2},
    {"type": "breakthrough", "rate": 0.15},
    {"type": "buff", "attribute": "attack", "amount": 30, "duration_seconds": 300},
    {"type": "cure", "status": "..."}
  ]
}
```

一张丹药可以有多个效果（如同时加修为和回血）。

### 6.2 效果类型

| 类型 | 说明 | 受等级衰减 | 受抗性衰减 | 抗性计数 |
|------|------|-----------|-----------|---------|
| `exp` | 增加修为 | ✅（有 0.1 保底） | ✅ | ✅ |
| `hp` | 回复 HP（固定值或百分比）。若玩家处于濒死状态，额外复活回满血并恢复空闲状态 | ❌ | ❌ | ❌ |
| `stat` | 永久增加指定四维属性（`statAttr`: STR/CON/AGI/WIS） | ✅（实际代码有 0.1 保底，见「迁移评估」B 段） | ✅ | ✅ |
| `breakthrough` | 一次性突破成功率加成（`rate`: 0.15 = +15%）→ 写入 1 小时 Buff | ✅（有 0.1 保底） | ❌ | ❌ |
| `buff` | 战斗增益 / 雷劫抗性等时效 Buff（`attribute`、`amount`、`duration_seconds`） | ✅（有 0.1 保底） | ❌ | ❌ |
| `cure` | 濒死时驱散异常并回满气血；否则提示无可驱散异常 | ❌ | ❌ | ❌ |

实际效果 = 效果基础值 × 成色倍率 × 等级衰减系数 × 抗性衰减系数（仅 exp/stat 乘抗性）。

各类型细节：

- **exp**：`actualExp = (long)(amount × 成色 × 等级衰减 × 抗性衰减)`；实际值 ≤ 0 时不生效、不加抗性、不产出文案；`addExp` 受修为存储上限截断（`100×等级² × 5`）；生效时抗性计数 +1。
- **hp**：濒死（`UserStatus.DYING`）时先复活回满血（成色倍率不参与），文案「复活并回满气血」；否则 `percentage>0` 按 `maxHp × percentage × 成色` 计算，`percentage=0` 按 `amount × 成色` 计算；治疗量按剩余气血截断，气血已满提示「气血已满，药力散入四肢百骸」。
- **stat**：`actualStat = (int)(amount × 成色 × 等级衰减 × 抗性衰减)`；≤ 0 不生效；`statAttr` 无法解析时不生效；生效后写回对应四维字段并累加抗性计数。
- **breakthrough**：`bonusValue = (int)(rate × 100 × 成色 × 等级衰减)`；≤ 0 不生效；写入 `player_buff` 的 `breakthrough` 类型，过期时间固定为当前 +1 小时；文案「突破成功率 +N%（1小时内有效）」。
- **buff**：`actualValue = (int)(amount × 成色 × 等级衰减)`；非雷劫抗性类实际值 ≤ 0 不创建；过期时间 = 当前 + `duration_seconds`；同类活跃层数上限 3（原子条件插入，超限返回提示）；详见 `pill-buffs` 能力。
- **cure**：濒死时复活回满血，否则提示「没有可驱散的异常状态」（不实际清除状态）。

### 6.3 濒死复活

服用 HP/驱散丹药时，若玩家状态为濒死：

- 血量设为满血
- 状态恢复为空闲
- 效果文本提示「复活」

## 7. 等级约束与抗性系统

### 7.1 等级衰减

统一公式（`GRADE_DECAY_COEFFICIENT = 0.2`）：

```
基础衰减 = min(1.0, pillGrade / (playerLevel × 0.2))
有保底衰减 = max(0.1, 基础衰减)
```

| 效果类型 | 是否保底（文档） | 实际代码 |
|---------|----------------|---------|
| exp | 有 0.1 保底 | 有保底 |
| breakthrough | 有 0.1 保底 | 有保底 |
| buff | 未写明 | 有保底 |
| stat | 无保底 | **有保底（传 `withFloor=true`）** |

无保底时的衰减参考表（文档数据，代码因保底会抬高最低值）：

| 丹药等级 | 5级玩家 | 20级玩家 | 50级玩家 | 100级玩家 |
|---------|--------|---------|---------|----------|
| 1品 | 1.00 | 0.25 | 0.10 | 0.05 |
| 3品 | 1.00 | 0.75 | 0.30 | 0.15 |
| 5品 | 1.00 | 1.00 | 0.50 | 0.25 |
| 7品 | 1.00 | 1.00 | 0.70 | 0.35 |
| 9品 | 1.00 | 1.00 | 0.90 | 0.45 |

### 7.2 抗性系统

永久收益类丹药（exp、stat）会产生抗性。抗性按「**丹药模板 + 成色**」独立计数：每次生效服用对应记录 `count + 1`。

```
抗性衰减 = max(0.1, 1 / (1 + count × 0.3))
```

| 服用次数 | 抗性衰减 |
|---------|---------|
| 第1次 | 1.00 |
| 第2次 | 0.77 |
| 第3次 | 0.63 |
| 第4次 | 0.53 |
| 第5次 | 0.45 |
| 第10次 | 0.27 |
| 第31次 | 0.10 (保底) |

抗性存储于独立表 `pill_resistance`，不随时间自动衰减。更新为原子 upsert（`RETURNING` 最新次数）。

### 7.3 数据存储

```sql
-- 丹药抗性表（实际表名无 xt_ 前缀）
CREATE TABLE pill_resistance (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES player(id),
    template_id BIGINT NOT NULL REFERENCES item_template(id),
    quality     VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    count       INT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, template_id, quality),
    CONSTRAINT chk_pill_resistance_count CHECK (count >= 0),
    CONSTRAINT chk_pill_resistance_quality CHECK (quality IN ('SUPERIOR', 'NORMAL', 'INFERIOR'))
);
```

玩家 Buff 表 `player_buff`（战斗增益 + 突破加成 + 雷劫抗性）见 `pill-buffs` 详细设计。

### 7.4 突破丹药

突破丹药服用后写入 `player_buff`，设置过期时间（固定 1 小时）。下次执行突破时从表中读取未过期的 `breakthrough` Buff 累加到此轮成功率，突破完成后（无论成败）清除该玩家所有 `breakthrough` Buff。突破成功率：

```
小境界：总成功率 = clamp(基础概率 + 失败次数 × 单次补偿 + 护道加成 + 突破丹药加成, 0, 100)
大境界/渡劫：丹药与护道加成转为雷劫 Boss 削弱 clamp((丹药 + 护道)/100, 0, 0.5)
```

### 7.5 战斗增益丹药

服用后写入 `player_buff`，过期时间由 `duration_seconds` 决定（如 300 秒）。战斗队伍构建时读取未过期的 `attack/defense/speed` Buff 叠加到战斗属性；同类活跃 Buff 最多 3 层。过期后查询层面不可见，并在用户状态结算时按用户物理清理。

## 8. 架构设计

### 8.1 服务拆分

```
PillRecipeService      → 丹方学习、已学列表、详情查询
PillRefiningService    → 自动/手动炼丹入口、输入解析、丹方匹配
PillCombinationFinder  → 自动配药组合、成色计算、丹药实例生成（内部使用 CombinationStrategy）
PillConsumptionService → 服用丹药、抗性查询更新、等级衰减计算、效果应用
```

### 8.2 物品消耗

物品使用服务（`ItemUseService`）的分发器在找到匹配的处理策略后，**统一调用扣减逻辑**：

1. 匹配处理策略（按 `ItemType` 查找 handler）
2. 统一扣减物品（handler `consumesInternally()=false` 时先扣 1 件）
3. 执行策略的具体使用逻辑

各处理策略不再自行处理消耗。丹药实例在扣减后才交给 `PillConsumptionService.takePill`，因此 `takePill` 不再按名重查背包。

### 8.3 服用丹药流程

1. 使用「使用 [丹药名]」定位背包丹药物品并扣减 1 件
2. 读取模板 `effects` 列表与实例成色（缺省中成）、品阶
3. 查询抗性表获取（模板 + 成色）计数 → 计算抗性衰减
4. 逐条应用效果：
   a. exp：等级衰减（有保底）× 抗性衰减 → 加修为 → 更新抗性
   b. hp：直接加血，若濒死则复活 → 不更新抗性
   c. stat：等级衰减（代码有保底）× 抗性衰减 → 加属性字段 → 更新抗性
   d. breakthrough：等级衰减（有保底）→ 写入 Buff 表 → 不更新抗性
   e. buff：等级衰减（有保底）→ 写入 Buff 表（同类 ≤3 层）→ 不更新抗性
   f. cure：濒死复活 → 不更新抗性
5. 汇总各效果文案返回「服用丹药成功：...」；全部效果无文案时报「丹药效果未知」

## 9. 丹方获取途径

- 灵石购买（NPC 商店）
- 地图探索掉落丹方卷轴
- 地图奇遇领悟

## 10. 命令接口

| 命令 | 功能 | 权限 |
|------|------|------|
| `丹方` | 查看已学丹方（编号 + 名称 + 品阶） | 已认证 |
| `丹方 [名称]` | 查看丹方详情（五行要求 + 成品） | 已认证 |
| `使用 [丹方卷轴名称]` | 使用丹方卷轴（RECIPE_SCROLL）学习 | 背包有卷轴 |
| `炼方 [丹方名]` | 自动配药炼丹 | 已学 |
| `炼 [药材名数量 ...]` | 手动配药炼丹（最多 5 种） | 已认证 |
| `使用 [丹药名称]` | 服用丹药 | 背包有 |

## 11. 品质计算示例

```
丹方 天元丹: 木[10~15] 水[6~9] 火[5~7]（金、土不参与）

方案A: 木=13 水=8 火=6
  木达标度 = 1 - |13-12.5|/2.5 = 0.8
  水达标度 = 1 - |8-7.5|/1.5  = 0.67
  火达标度 = 1 - |6-6|/1      = 1.0
  平均 = 0.82 → 上成

方案B: 木=10 水=6 火=5
  木达标度 = 0, 水达标度 = 0, 火达标度 = 0
  平均 = 0 → 下成
```

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名无 `xt_` 前缀**：`player_pill_recipe`、`pill_resistance`、`player_buff`；`player_pill_recipe.user_id` 的 FK 指向 `player(id)`，`pill_resistance` 另有 `id` 主键与 `quality` 列。纯实现细节，文档已按代码修正。
- **新增 `cure`/`buff` 效果与 `hp` 百分比乘成色**：`ItemProperties.Effect` 现为 6 种；`cure` 在濒死时复活回满血、否则提示无可驱散异常；`buff` 为时效增益（详见 `pill-buffs`）；`hp` 百分比回复同样乘成色倍率。
- **手动炼丹解析容错**：格式错误或背包未命中的输入段被静默跳过，全部无效才报格式错误；指定数量超过持有量由原子扣减报「物品数量不足」。
- **失败不消耗药材**：自动模式在缺属性/超上限时直接报错，手动模式仅在命中丹方后才扣减（文档 §3.3 已明说）。

### B. 保留代码设计

- **抗性按「模板 + 成色」独立计数**：唯一键 `(user_id, template_id, quality)`。上成丹药不再被此前服用中成/下成的次数拖累，高品质炼制成果能完整体现，比按模板共用一个计数更公平。
- **抗性衰减 `max(0.1, 1/(1 + count × 0.3))`**：比文档 `1/(1 + count)` 衰减更慢且有 0.1 保底，永久收益类丹药长期服用仍有价值，避免过早沦为「废丹」。
- **stat 等级衰减有 0.1 保底**：与 exp/breakthrough/buff 统一走 `withFloor=true`，避免高等级玩家服用低品阶 stat 丹时被取整为 0、白耗一颗（`calcGradeDecay` 的 Javadoc 仍写「stat 无保底」，属注释过时，行为本身合理）。
- **效果品阶按成品模板 tags 映射**：legendary/mythic=50、grand=35、epic=30、advanced=20、rare/intermediate=10、critical=8、basic=5、entry=3，无匹配 tags 才回退实例 `properties.grade`、再兜底 3。品阶与物品稀有度阶梯同源，高品丹药在高等级仍保有药效，数据也随模板统一维护。
- **命令拆分为 `炼方`（自动）/`炼`（手动），列表命令为 `丹方`**：`炼 [丹方名]` 与手动炼制存在歧义，拆分后语义清晰；文档的 `丹方列表` 在模板语义下会命中 `丹方\s*{{recipeName}}` 被当作详情查询（recipeName=列表），改用 `丹方` 后列表与详情边界明确。
- **抗性仅在效果实际生效时计数**：exp/stat 实际值 ≤0 时不生效也不加抗性，避免无效服用仍被抗性惩罚的双重损失。
- **炼丹产物按「同模板 + 同属性」合并堆叠、命名「成品名-成色」**：不同成色分堆、名称直示成色，减少背包碎行，便于辨认与出售。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

无

### E. 缺陷修复

- **五行大小写不匹配**（已修）：原实现以大写枚举 code 读取小写 `elements` 键、以大写 key 匹配小写 `requirements` 键，导致五行累计恒为 0、自动炼丹必报缺属性、手动炼丹必不匹配。现 `StackableItem.getElementValue` 大小写不敏感匹配，`ItemProperties.normalizeRequirementKeys` 把丹方/图纸 requirements 键统一为大写，种子数据无需迁移。
