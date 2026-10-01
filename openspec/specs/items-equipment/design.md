# 物品与装备系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 物品模板 (item_template)

### 1.1 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `name` | VARCHAR(128) | 物品名称（UNIQUE） |
| `type` | VARCHAR(32) | 物品类型（CHECK 与 ItemType.code 一致） |
| `properties` | JSONB | 类型特有属性（种子/丹药/材料等） |
| `tags` | JSONB | 物品标签（GIN 索引） |
| `base_value` | BIGINT | 物品基准价（灵石），用于计算收购/售价 |
| `description` | TEXT | 描述 |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

> 代码/迁移中的实际表名为 `item_template`（无 `xt_` 前缀）；模板上没有 `max_stack` 字段，堆叠不受上限约束。

### 1.2 properties 示例

```json
// 种子
{"grow_time":24,"max_harvest":1,"yield_min":2,"yield_max":4,"mutation":{"chance":0.05,"template_id":N},"production_items":[{"template_id":1}]}
// 兽卵
{"beast_template_id":1}
// 法决玉简
{"skill_id":1}
// 丹方卷轴
{"recipe":{"grade":3,"result_item_id":1,"result_quantity":1,"requirements":{"metal":{"min":1,"max":5}}}}
// 丹药
{"effects":[{"type":"exp","amount":100}]}
// 锻材
{"RIGIDITY":5,"TOUGHNESS":3,"SPIRIT":1}
// 药材
{"elements":{"WOOD":3,"FIRE":1,"WATER":2}}
```

---

## 2. 装备模板 (equipment_template)

### 2.1 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `name` | VARCHAR(128) | 装备名称（UNIQUE） |
| `description` | TEXT | 装备描述 |
| `tags` | JSONB | 装备标签（AI 检索用） |
| `slot` | VARCHAR(32) | 装备部位 (WEAPON/ARMOR/ACCESSORY) |
| `weapon_type` | VARCHAR(32) | 法器子类型，可空（护甲/饰品为 null） |
| `category` | VARCHAR(16) | 法器大类：MELEE/POLEARM/RANGED/EXOTIC（护甲/饰品为 null） |
| `equip_level` | INT | 装备等级（默认 1） |
| `base_attack` | INT | 基础攻击 |
| `base_defense` | INT | 基础防御 |
| `base_str` | INT | 基础力量加成 |
| `base_con` | INT | 基础体质加成 |
| `base_agi` | INT | 基础敏捷加成 |
| `base_wis` | INT | 基础智慧加成 |
| `attack_speed` | DECIMAL(3,1) | 攻速（法器专属，影响法决 CD 恢复速度） |
| `attack_range` | VARCHAR(16) | 近战/远程（法器专属） |
| `drop_weight` | JSONB | 稀有度掉落权重 |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

> 装备模板独立于 `item_template`，没有 `template_id` 关联列（装备不走物品模板表）。

### 2.2 drop_weight JSONB

```json
{"BROKEN": 60, "COMMON": 25, "RARE": 10, "EPIC": 4, "LEGENDARY": 1}
```

稀有度在装备实例化时加权随机决定，key 为 `Rarity.code`（`EnumValue`，与枚举名一致），value 为权重（整数）。通过 `Rarity.roll(dropWeight)` 解析；drop_weight 为空或权重总和 ≤0 时回退 COMMON。

### 2.3 装备部位 (EquipmentSlot)

| 枚举值 | 中文名 | 说明 |
|------|------|------|
| WEAPON | 法器 | 主要攻击源，有 weapon_type + category |
| ARMOR | 护甲 | 主要防御源，无 weapon_type |
| ACCESSORY | 饰品 | 属性辅助，无 weapon_type |

---

## 3. 法器类型 (WeaponType) 与克制矩阵

### 3.1 法器子类型 (WeaponType)

代码共 **14 种**法器子类型：

| 枚举 | 中文 | 所属大类 | 说明 |
|------|------|---------|------|
| BLADE | 刀 | 刀兵 | 刚猛斩击，克制妖兽 |
| SWORD | 剑 | 刀兵 | 灵动穿刺，克制灵体 |
| AXE | 斧 | 刀兵 | 沉重破甲，克制甲胄 |
| SPEAR | 枪 | 长兵 | 长驱直入，克制猛兽 |
| STAFF | 棍 | 长兵 | 降妖伏魔，克制邪祟 |
| BOW | 弓 | 远兵 | 远程精准，克制飞行 |
| WHIP | 鞭 | 奇兵 | 灵动缠绕 |
| HALBERD | 戟 | 奇兵 | 劈刺一体 |
| HAMMER | 锤 | 奇兵 | 重击碎魂 |
| DAGGER | 匕首 | 奇兵 | 短小精悍 |
| FAN | 扇 | 奇兵 | 风雷变化 |
| FLYWHISK | 拂尘 | 奇兵 | 万象归空 |
| RING | 圈 | 奇兵 | 乾天坤地 |
| BELL | 钟 | 奇兵 | 震慑心神 |

> `WeaponType.category` 在 Java 枚举中是中文（刀兵/长兵/远兵/奇兵）；`equipment_template.category` 与法决 `binding_value` 的种子数据使用英文 MELEE/POLEARM/RANGED/EXOTIC。两套取值目前不一致（见文末差异）。

### 3.2 怪物类型 (MonsterType)

| 枚举 | 中文 | 被克制法器 |
|------|------|----------|
| BEAST | 妖兽 | 刀 |
| SPIRIT | 灵体 | 剑 |
| ARMORED | 甲胄 | 斧 |
| WILD_BEAST | 猛兽 | 枪 |
| EVIL | 邪祟 | 棍 |
| FLYING | 飞行 | 弓 |
| HUMAN | 人形 | 无特定克制 |

### 3.3 克制规则（代码实现）

```
法器克制怪物（ADVANTAGE_MAP 命中）→ 玩家伤害 ×1.5
其余情况                        → 倍率 1.0
```

- 克制关系表：BLADE→BEAST、SWORD→SPIRIT、AXE→ARMORED、SPEAR→WILD_BEAST、STAFF→EVIL、BOW→FLYING。
- 倍率只作用于玩家对怪物的伤害（普通攻击与法决伤害均乘算）；**未实现**「被克制时玩家 ×0.8 / 怪物 ×1.5」的反向惩罚。
- 灵兽不使用法器，不参与属性克制。

---

## 4. 装备实例化 —— 稀有度加权随机

### 4.1 流程

装备获取时按来源走不同路径，逻辑等价：

- 通用路径：`EquipmentService.createEquipment(userId, templateId)` → `EquipmentFactory.createEquipment(...)`；
- 锻造路径：`ForgingService` 内联生成（按锻材品质评分决定稀有度）；
- 商铺购买路径：`ShopService` 内联生成（按模板 drop_weight roll 稀有度）。

```
① 获取 EquipmentTemplate
② 加权随机 roll 出 Rarity（BROKEN/COMMON/RARE/EPIC/LEGENDARY）
③ 在 Rarity 的 qualityMultiplier 范围内随机取一个值（均匀分布）
④ 在 Rarity 的 affixCount 范围内随机取词条数，打乱词条池后依次选取
   - 池 = 4 个属性词条（STRENGTH/CONSTITUTION/AGILITY/WISDOM）
   - LEGENDARY 额外加入 2 个特殊词条（LIFE_STEAL/TREASURE_HUNT）
   - 属性词条数值 = 1~4 随机；特殊词条数值固定 5
⑤ 装备名 = 模板名 + "-" + 稀有度中文名（如「青锋刀-稀有」）
⑥ Equipment.create(...) 并持久化
```

> 原文档所述「前缀 + 模板名」未实现：`Rarity.prefixes`（破烂的/生锈的…）列表存在但从未被调用。

### 4.2 稀有度体系 (Rarity)

| 稀有度 | 中文 | rank | 颜色 | 品质系数 | 词条数 | 前缀列表示例（未使用） |
|------|------|------|------|---------|--------|---------|
| BROKEN | 破旧 | 0 | ⚪ | 0.80–0.99 | 0 | 破烂的、生锈的、残破的、陈旧的 |
| COMMON | 普通 | 1 | 🟢 | 1.00–1.15 | 1 | 普通的、结实的、标准的、常规的 |
| RARE | 稀有 | 2 | 🔵 | 1.16–1.35 | 1–2 | 锋利的、精工的、锐利的、精制的 |
| EPIC | 史诗 | 3 | 🟣 | 1.36–1.60 | 2–3 | 卓越的、附魔的、辉煌的、不凡的 |
| LEGENDARY | 传说 | 4 | 🟡 | 1.61–2.00 | 3–4 | 传说的、完美的、神话的、神级的 |

### 4.3 属性计算公式（代码实现）

```
最终攻击 = floor(基础攻击 × qualityMultiplier) + forgeLevel × 5
最终防御 = floor(基础防御 × qualityMultiplier) + forgeLevel × 5
属性加成 = stat_bonus（模板基础属性） + affixes（词条加成）
```

- 属性加成不乘品质系数；攻击/防御乘品质系数后再向下取整。
- `qualityMultiplier` 为空（历史数据）时攻击/防御直接取基础值。
- 装备替换时的气血上限变化按 `根骨变化 × 20` 估算展示。

---

## 5. 装备实例 (equipment)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | 持有者（FK → player.id，级联删除） |
| `template_id` | BIGINT | 模板ID（FK → equipment_template.id） |
| `name` | VARCHAR(128) | 实际名称（模板名-稀有度名） |
| `slot` | VARCHAR(32) | 部位 |
| `weapon_type` | VARCHAR(32) | 法器子类型（可空） |
| `rarity` | VARCHAR(32) | 稀有度 |
| `quality_multiplier` | DOUBLE | 品质乘数（实际波动值） |
| `affixes` | JSONB | 随机词条 |
| `stat_bonus` | JSONB | 属性加成 |
| `attack_bonus` | INT | 攻击加成（基础值，不含波动与锻造） |
| `defense_bonus` | INT | 防御加成（基础值，不含波动与锻造） |
| `forge_level` | INT | 锻造强化等级（默认 0） |
| `equipped` | BOOLEAN | 已装备 |
| `tradable` | BOOLEAN | 是否可出售给掌柜（穿戴后置 FALSE） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

### 5.1 词条 (AffixType)

| 词条 | 中文名 | statField | 效果 |
|------|--------|-----------|------|
| STRENGTH | 蛮力 | STR | 力道 +X |
| CONSTITUTION | 坚韧 | CON | 根骨 +X |
| AGILITY | 轻灵 | AGI | 身法 +X |
| WISDOM | 启迪 | WIS | 悟性 +X |
| LIFE_STEAL | 吸血 | null | 吸血（伤害的 5% 转化为 HP，仅传说装） |
| TREASURE_HUNT | 寻宝 | null | 寻宝（历练极品掉率 +5%，仅传说装） |

- 特殊词条只在 `LEGENDARY` 词条池中出现，数值固定为 5。
- 存储键：属性词条用 statField（STR/CON/AGI/WIS），特殊词条用枚举名（LIFE_STEAL/TREASURE_HUNT）；读取兼容两种键。

### 5.2 affixes JSONB 示例

```json
{"STR": 3, "AGI": 2, "LIFE_STEAL": 5}
```

### 5.3 stat_bonus JSONB 示例

```json
{"STR": 5, "CON": 3, "AGI": 2, "WIS": 0}
```

键为属性缩写（UPPERCASE），值为基础加成。

---

## 6. 堆叠物品 (inventory_item)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | 持有者 |
| `template_id` | BIGINT | 模板ID |
| `item_type` | VARCHAR(32) | 物品类型 |
| `name` | VARCHAR(128) | 名称 |
| `quantity` | INT | 数量 |
| `tags` | JSONB | 标签 |
| `properties` | JSONB | 类型特有属性（丹药成色/药材五行等） |
| `properties_hash` | INT | properties 的确定性哈希，用于拆分堆叠行 |
| `tradable` | BOOLEAN | 是否可出售（默认 TRUE） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

`UNIQUE(user_id, template_id, properties_hash)`：同模板同属性合并存储，属性不同则分为不同行。增加数量使用 `upsertIncrementQuantity` 原子合并；无堆叠上限。

---

## 7. 物品类型 (ItemType)

| 类型 | 中文 | 说明 | 用途 |
|------|------|------|------|
| MATERIAL | 锻材 | 材料 | 锻造/炼药/送礼 |
| SEED | 种子 | 种子 | 福地种植 |
| BEAST_EGG | 兽卵 | 兽卵 | 福地孵化 |
| POTION | 丹药 | 丹药 | HP恢复/Buff |
| SKILL_JADE | 法决玉简 | 法决玉简 | 使用后习得法决 |
| RECIPE_SCROLL | 丹方卷轴 | 丹方卷轴 | 使用后习得丹方 |
| FORGING_BLUEPRINT | 锻造图纸 | 锻造图纸 | 使用后习得锻造配方 |
| HERB | 药材 | 药材 | 炼药原料 |
| BEAST_ESSENCE | 灵兽精华 | 灵兽精华 | 喂养灵兽获取修为 |

---

## 8. 背包管理

- 装备：每件独立实例，不堆叠（`equipment` 表）。
- 材料/消耗品：按 `user_id + template_id + properties_hash` 合并，`upsertIncrementQuantity` 原子累加；无 `max_stack` 限制。
- 历练结算掉落物**自动进背包**，无需手动拾取。
- 背包查询分类（`InventoryCategory`）：种子、装备、兽卵、锻材、丹药、药材、法决玉简、丹方卷轴、锻造图纸、灵兽精华；另有 `ALL`（全部）仅用于地灵 AI 的背包查询工具，玩家「背包 全部」会走未知分类分支。

---

## 9. 物品使用系统

### 9.1 统一使用指令

```
使用 [物品名]
使用 [物品名] [参数]
```

支持自动识别物品类型并执行对应效果：

| 物品类型 | 示例 | 效果 |
|---------|------|------|
| 丹药 (POTION) | `使用 天元丹` | 服用丹药，获得效果 |
| 法决玉简 (SKILL_JADE) | `使用 御剑术玉简` | 学习法决 |
| 丹方卷轴 (RECIPE_SCROLL) | `使用 天元丹方` | 学习丹方 |
| 锻造图纸 (FORGING_BLUEPRINT) | `使用 玄铁剑图纸` | 学习锻造配方 |
| 灵兽精华 (BEAST_ESSENCE) | `使用 灵兽精华 3 5` | 喂养 5 号兽栏灵兽 3 份精华 |

解析顺序：先按名称精确匹配；无精确匹配时按名称模糊匹配，若命中多个**不同名称**则要求玩家精确指定（报「找到多个」），不随机消耗。

### 9.2 策略模式架构

```
service/inventory/handler/
├── ItemUseHandler.java              # 接口
├── PillUseHandler.java              # 丹药处理器 → PillConsumptionService.takePill()
├── SkillJadeUseHandler.java         # 法决玉简处理器 → SkillService.learnFromJade()
├── RecipeScrollUseHandler.java      # 丹方卷轴处理器 → PillRecipeService.learnRecipeInternal()
├── ForgingBlueprintUseHandler.java  # 锻造图纸处理器 → ForgingService.learnRecipeInternal()
└── BeastEssenceUseHandler.java      # 灵兽精华处理器 → 直接喂养灵兽
```

### 9.3 ItemUseHandler 接口

接口定义：

- `getItemType()` → `ItemType`：此处理器负责的物品类型（以 `Map<ItemType, ItemUseHandler>` O(1) 分发，不再有 `supports()`）；
- `use(Long userId, StackableItem, @Nullable ItemTemplate, String args)` → `String`：执行使用逻辑；
- `consumesInternally()` → `boolean`（默认 false）：是否自行管理物品消耗（false 则由 `ItemUseService` 统一扣减 1 件）。

实际取值：

- `PillUseHandler`、`RecipeScrollUseHandler`、`ForgingBlueprintUseHandler`：`consumesInternally()` 返回 `false`，由 `ItemUseService` 自动扣减数量；
- `SkillJadeUseHandler`、`BeastEssenceUseHandler`：`consumesInternally()` 返回 `true`，自行管理消耗（玉简在境界校验通过后扣减；精华按玩家指定数量扣减）。

### 9.4 扩展方式

创建新的 `@Component` 类实现 `ItemUseHandler` 接口即可自动注册：

- `getItemType()`：返回其负责的 `ItemType`；
- `use()`：执行使用逻辑；
- 可选覆盖 `consumesInternally()`。

---

## 10. 玩家指令入口（按代码补充）

| 指令 | 说明 |
|------|------|
| `背包` | 汇总展示：未穿戴装备、按类型物品、灵石 |
| `背包 [分类]` | 按分类编号列表（装备类显示稀有度；材料显示三性；药材显示五行） |
| `装备 [名称/编号]` | 穿戴未装备的装备；等级不足/多件同名给出提示 |
| `卸下 [部位/名称/编号]` | 卸下；部位名支持 法器/护甲/饰品；也支持装备名或列表编号 |
| `丢弃 [数量] [名称]` | 丢弃装备或堆叠物品；已装备装备拒绝 |
| `使用 [物品名] [参数]` | 统一使用入口 |
| `查看 [装备名/物品名]` | 装备详情（品质、系数、攻防、词条、属性加成）或物品详情；装备解析范围为未穿戴装备 |

错误码（节选）：`ITEM_NOT_FOUND`、`ITEM_CANNOT_USE`、`ITEM_EQUIPPED`、`ITEM_MULTIPLE_MATCH`、`ITEM_QUANTITY_INSUFFICIENT`、`ITEM_NOT_EXISTS`、`ITEM_OWNERSHIP_MISMATCH`。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- 表名无 `xt_` 前缀：实际为 `item_template`、`equipment_template`、`equipment`、`inventory_item`；`equipment_template` 无 `template_id` 列，装备模板独立于物品模板。
- `item_template` 无 `max_stack`（堆叠无上限），新增 `base_value`（收购/售价基准）；`inventory_item` 含 `properties` / `properties_hash` / `tradable`，唯一键 `(user_id, template_id, properties_hash)`，属性不同分行存储。
- `equipment_template` 新增 `description` / `tags`；`equipment` 新增 `tradable`（穿戴后置 FALSE，影响商铺出售）。
- `WeaponType` 实际 14 种（补 DAGGER 匕首 / RING 圈 / BELL 钟，三者无克制关系）；`ItemType` 实际 9 种（含 FORGING_BLUEPRINT、BEAST_ESSENCE）。
- 属性/词条公式：最终攻防 = `floor(基础值 × qualityMultiplier) + forgeLevel × 5`，属性加成不乘品质系数；属性词条 1~4 随机、特殊词条固定 5。
- `ItemUseHandler` 以 `getItemType()` 取代 `supports()`，实现位于 `service/inventory/handler/`；`Pill` / `RecipeScroll` / `ForgingBlueprint` 的 `consumesInternally()` 为 false（由 `ItemUseService` 统一扣 1），`SkillJade` / `BeastEssence` 为 true（自行扣减）。
- 使用解析：精确优先，模糊命中多个不同名称时要求精确指定；模板不存在的物品记录被跳过。
- 装备实例化非单一路径：`EquipmentService.createEquipment`、锻造、商铺购买三处各自生成（逻辑等价）；`查看 [名称/编号]` 可查装备/物品详情；卸下部位名为 法器/护甲/饰品。

### B. 保留代码设计

- **克制只做正向加成**：法器克制时玩家伤害 ×1.5，其余 1.0，不实现反向惩罚（被克制不降伤、怪物不加伤）。与 spec「MUST NOT 实现反向惩罚」一致：避免同一选择既给收益又给惩罚，缺少对口法器时挫败更小；灵兽不参与克制，规则简单可预期。
- **装备命名为「模板名-稀有度名」**：稀有度直接进入名称（如「青锋刀-稀有」），背包/掉落/商店列表一眼可辨；`Rarity.prefixes` 与 `randomPrefix()` 无调用方，属可清理的死配置，不影响行为。

### C. 按设计修正（待修）

- 无。

### D. 未实现（待办）

- 无。

### E. 缺陷修复

- **`equipment_template.category` 与 `WeaponType.category` 取值不一致**（已修）：`WeaponType` 新增 `categoryCode()`（刀兵→MELEE、长兵→POLEARM、远兵→RANGED、奇兵→EXOTIC），法决 `WEAPON_CATEGORY` 过滤改按 code 比较，绑定恢复命中。
