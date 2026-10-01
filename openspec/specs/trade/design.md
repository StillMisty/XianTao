# 交易系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 设计原则

- **文字挂机 MUD**：所有交互异步、无实时、无定时任务
- **事件驱动**：所有价格/库存变动均由玩家交互触发（懒检查），无定时任务
- **LLM 优先**：商铺掌柜为 LLM NPC，买卖均通过自然语言触发
- **LLM 不参与数值**：所有价格由程序 `PriceEngine` 计算，LLM 只负责读取和自然语言呈现，不能更改或编造价格
- **玩家↔系统，无玩家间交易**：所有交易都是玩家与商铺 NPC 之间的行为，玩家之间不流通物品，从根源杜绝 RMT 和小号喂养
- **真实感**：收购/售价受世界事件影响，库存受供需调节

### 1.1 为什么不做玩家间交易

| 风险 | 方案 B 的处理 |
|------|-------------|
| 小号喂养 | 不存在玩家间转移 |
| RMT | 不存在玩家间转移 |
| 经济通胀 | 系统控制所有价格和回收量 |
| 新号速成 | 无法通过交易获得高阶装备/丹药 |
| BOT 脚本 | 玩家只能卖给系统，无套利空间 |

---

## 2. 物品估值

### 2.1 `item_template.base_value`

```sql
ALTER TABLE item_template ADD COLUMN base_value BIGINT NOT NULL DEFAULT 0;
```

每个物品模板有基准价。玩家出售给商铺时，以此为基础计算。`V1.0.53__update_item_base_value.sql` 为无基准价的堆叠类物品按稀有度标签补齐了定价（mythic 2500 / legendary 1000 / epic 300 / rare 80 / uncommon 25 / 其他 8）。

### 2.2 `inventory_item.tradable` / `equipment.tradable`

```sql
ALTER TABLE inventory_item ADD COLUMN tradable BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE equipment ADD COLUMN tradable BOOLEAN NOT NULL DEFAULT TRUE;
-- 大部分物品默认可回收；装备一旦装备过即置 FALSE（绑定）
```

控制物品能否出售给商铺。不可回收的物品在卖给掌柜时会被拒绝：

> "客官，此物伴有神魂烙印，老朽收不得。"

实际表名为 `inventory_item` 与 `equipment`（无 `xt_` 前缀）；装备在首次装备时由 `EquipmentService` 置 `tradable=false`。

## 3. 商铺系统

### 3.1 `shop_npc` — 掌柜模板

```sql
CREATE TABLE shop_npc (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(64) NOT NULL,        -- 掌柜名
    map_node_id        BIGINT NOT NULL REFERENCES map_node(id),
    personality        VARCHAR(16),                 -- MBTI 性格（影响砍价难度）
    buy_price_modifier NUMERIC(3,2) NOT NULL DEFAULT 0.50,  -- 收购折扣（基准价的 %）
    category_multiplier JSONB,                      -- {"HERB": 0.85, "ORE": 1.15}
    system_prompt      TEXT,                        -- LLM system prompt
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

`buy_price_modifier` 是掌柜收购价相对基准价的折扣。种子数据（`V1.0.28.1__seed_shop_data.sql`）示例：

| 掌柜 | 地图 | 性格 | 收购折扣 | category_multiplier |
|------|------|------|---------|---------------------|
| 药老（药堂） | 1 青石镇 | ISFJ | 0.60 | POTION 0.85 / HERB 0.90 / MATERIAL 0.50 |
| 铁锤（铁匠铺） | 4 铁山堡 | ESTP | 0.70 | MATERIAL 1.10 / POTION 0.50 |
| 千机子（天机阁） | 11 天机阁 | INTJ | 0.55 | SKILL_JADE 1.20 / RECIPE_SCROLL 1.10 / MATERIAL 0.70 |
| 柳三娘（杂货铺） | 17 飘渺城 | ESFJ | 0.55 | SEED 0.80 / BEAST_EGG 0.75 / MATERIAL 0.60 |

`ShopNpc` 的行为方法：

- `getBuyPriceModifierDouble()`：空值兜底 0.50
- `getCategoryMultiplier(itemTypeCode)`：查 `category_multiplier[code]`，无配置返回 1.0
- `getHaggleDifficulty()`：按 MBTI 性格返回砍价难度——ESFJ 0.0（热情好客）、ISFJ 0.05（温和良善）、INTJ 0.1（疏离冷静）、ESTP 0.15（粗犷霸道）、未知/空 0.1

**输出长度**：使用 `shopChatClient` bean（`max-tokens: 800`，`SpringAiConfig.SHOP_MAX_TOKENS`）。`npcChatClient`（max-tokens: 1000）仅用于宗门命名等一次性生成。详见 [AI对话系统](./AI对话系统.md) 第 4.5 节。

收购价公式：

```
收购价 = base_value × buy_price_modifier × Σ(worldEventMultiplier) × categoryMultiplier
```

（实际为连乘世界事件倍率，取整后最低 1 灵石，见 §6。）

### 3.2 `shop_product` — 商铺出售的商品

实际迁移：`V1.0.27__create_shop_product.sql`。

```sql
CREATE TABLE shop_product (
    id              BIGSERIAL PRIMARY KEY,
    shop_npc_id     BIGINT NOT NULL REFERENCES shop_npc(id),
    product_type    VARCHAR(16) NOT NULL CHECK (product_type IN ('ITEM', 'EQUIPMENT')),
    template_id     BIGINT NOT NULL,            -- ITEM→item_template, EQUIPMENT→equipment_template
    base_price      BIGINT NOT NULL,
    min_price       BIGINT NOT NULL,
    max_price       BIGINT NOT NULL,
    min_stock       INT NOT NULL DEFAULT 0,
    max_stock       INT NOT NULL,
    current_price   BIGINT NOT NULL,
    current_stock   INT NOT NULL,
    last_sale_time  TIMESTAMP,
    version         INT NOT NULL DEFAULT 0,

    CONSTRAINT chk_price CHECK (
        min_price <= base_price AND base_price <= max_price AND
        min_price <= current_price AND current_price <= max_price
    ),
    CONSTRAINT chk_stock CHECK (
        current_stock >= 0 AND current_stock <= max_stock
    )
);

CREATE INDEX idx_shop_product_npc ON shop_product(shop_npc_id);
CREATE INDEX idx_shop_product_npc_template ON shop_product (shop_npc_id, template_id);
CREATE INDEX idx_shop_product_type ON shop_product (product_type);
```

与文档的差异：实际 `chk_stock` 只约束 `current_stock >= 0 AND <= max_stock`；`min_stock` 是补货下限目标（注释明确「购买可使库存低于该值但不低于 0」），不是硬下界。

`shop_product` 控制商铺**卖什么**。至于商铺**收什么**——所有 `tradable=true` 的物品都收，无需配置。

### 3.3 懒补货 / 懒调价

触发时机：玩家打开商铺（`listProducts`，仅内存展示）或尝试购买时（`purchaseItemInternal`，会落库）。

**缺货涨价补货**（实际常量）：

```
IF current_stock = 0 AND (now - last_sale_time) > 4 小时
  restockQty = max(1, max_stock / 2)
  current_stock = MIN(restockQty, max_stock)
  current_price = MIN(CEIL(current_price × 1.15), max_price)  -- +15%
  last_sale_time = now
```

**滞销降价减仓**（实际常量）：

```
IF current_stock > max_stock / 2 AND (now - last_sale_time) > 2 天
  decayQty = max(1, current_stock / 5)
  current_stock = MAX(current_stock - decayQty, min_stock)
  current_price = MAX(CEIL(current_price × 0.92), min_price)  -- -8%
  last_sale_time = now
```

**效果**：

| 状态 | 系统反应 | 玩家感知 |
|------|---------|---------|
| 热销品卖空 | 几小时后加量涨价 | "怎么贵了？" |
| 冷门品滞销 | 降价减仓，有人买即止跌 | "这玩意儿终于降价了" |
| 人多抢购 | 频繁触发补货涨价，见顶为止 | 先到先得 |
| 人少无人问 | 持续降价到 min_price | 低价淘货 |

文档中的 `restockInterval` / `decayInterval` / `threshold` / `restockQty` / `decayQty` 均为固定常量，无配置项。

---

## 4. 收购流程（玩家 → 掌柜）

### 4.1 对话流程

```
玩家：掌柜，这柄玄铁剑你收不收？
                  ↓
掌柜 LLM 调用 appraiseItem(itemName, itemId)
                  ↓
系统计算（PriceEngine.calculateBuybackPrice）：
  basePrice  = base_value × buy_price_modifier × worldMultiplier × categoryMultiplier
  minPrice   = ROUND(basePrice × 0.8)
  maxPrice   = ROUND(basePrice × 1.2)（最低 1）
返回: {basePrice, minPrice, maxPrice, tradable}
                  ↓
LLM 读取结果，自然回复
  → "玄铁剑，炼制手法中规中矩，老朽出 180 灵石"
                  ↓
玩家：行  → 成交（LLM 调用 buyItem）
  或
玩家：便宜点 / 太贵了（触发砍价）
                  ↓
LLM 调用 negotiatePrice(currentPrice, basePrice, isBuying)
                  ↓
程序（ShopTools + ShopService.haggleItem）：
  ├─ 检查本轮流是否已砍过（ShopChatContext.haggleUsed）→ 已砍过直接拒绝
  ├─ 校验 currentPrice 在 [minPrice, maxPrice] 内（以传入 basePrice 计算；非法则拒绝）
  ├─ 计算成功率：
  │    successRate = (0.3 + charmFactor × 0.3 - difficulty) × wealthMultiplier
  │    ├─ charmFactor: clamp((WIS - 10) / 20, 0, 0.3)
  │    ├─ difficulty: NPC MBTI 性格决定（ESFJ=0.0, ISFJ=0.05, INTJ=0.1, ESTP=0.15，默认 0.1）
  │    └─ wealthMultiplier: 玩家当日财运修正（0.85 ~ 1.15，= 0.85 + wealth / 333）
  │    successRate 钳制 [0.05, 0.85]
  ├─ Roll 并计算变价幅度：
  │    successMargin = successRate - roll
  │    amountRatio = (0.03 + successMargin × 0.12) × wealthMultiplier
  │    amountRatio 钳制 [0.01, 0.20]
  │    amount = max(1, ceil(currentPrice × amountRatio))
  │    newPrice = isBuying ? currentPrice - amount : currentPrice + amount
  ├─ 保护线：newPrice 被钳制在 [minPrice, maxPrice] 内；钳制后 amount ≤ 0 视为失败
  └─ 返回 HaggleResult(success, newPrice, priceChange, reason)
                   ↓
[成功] → "罢了罢了，看你诚心，让利 X 灵石，算你 Y 灵石吧"
[失败] → "客官，这价已经是最低了，再降老朽要亏本了"（卖东西时为抬价失败文案）
[已砍过] → "客官，方才已让过利了，这价不能再降了"
                   ↓
玩家：行
  → LLM 调用 buyItem(itemName, itemId, confirmedPrice)
  → 原子操作：扣物品 + 加灵石
```

**砍价规则**：

- 砍价由 LLM 自行判断调用时机（system prompt 中写明"当客人要求降价或抬价时，必须调用 negotiatePrice 工具"），**不用关键词匹配**
- 每轮对话只允许一次砍价，由 `ShopChatContext.haggleUsed` 程序标记控制
- `negotiatePrice` 必须传入 `basePrice`（从 `appraiseItem` 或 `showGoods` 获取），工具会校验 `currentPrice` 是否在合理范围内
- 砍价幅度可变（约 1%~20%，与掷骰余量和财运挂钩），不是固定 5%——掷骰运气越好折扣越大
- 价格始终被钳制在 `PriceEngine.minPrice` ~ `PriceEngine.maxPrice` 内
- **标记时机（代码）**：只要报价合法并真正执行了一次砍价（无论成功还是失败），`ShopChatContext.markHaggled()` 都会被调用；报价超范围被拒绝时不标记
- 砍价成功回复示例：「罢了罢了，看你诚心，让利 X 灵石，算你 Y 灵石吧」；卖东西抬价成功：「罢了罢了，看这品质确实不错，加 X 灵石，Y 灵石收了」

### 4.2 LLM 工具集

实际工具名为 `showGoods` / `checkPlayerItems` / `appraiseItem` / `negotiatePrice` / `buyItem` / `sellGoods` / `sellEquipment`。

```java
@Tool(description = "展示店铺所有可购商品清单，含商品编号、名称、价格(灵石)和库存")
public ProductListVO showGoods() { ... }

@Tool(description = "查看客人背包中可出售/可回收的物品清单，含编号和是否可交易")
public PlayerItemsVO checkPlayerItems() { ... }

@Tool(description = "估价客人要出售的物品，返回收购价范围。首次报价必须等于 basePrice，不可自行编造")
public AppraisalResult appraiseItem(
    @ToolParam(description = "物品名称") String itemName,
    @ToolParam(description = "物品编号，首次留空，重名时由客人指定") String itemId
) {
    // 两阶段：itemId 非空则先试装备再试堆叠；为空则按名称模糊匹配
    // 匹配到多个 → 列出所有候选（含编号）让客人指定；无匹配 → 未找到
    // 返回: {tradable, basePrice, minPrice, maxPrice, itemName, description}
}

@Tool(description = "与客人讨价还价，每轮对话限一次。isBuying=true=砍价(降价)，false=抬价(提价)")
public HaggleResult negotiatePrice(
    @ToolParam(description = "当前报价（灵石）") long currentPrice,
    @ToolParam(description = "基准价格（灵石），从 showGoods 或 appraiseItem 结果中获取") long basePrice,
    @ToolParam(description = "客人是否在买东西(ture=买家砍价，false=卖家抬价)") boolean isBuying
) {
    // 已砍过 → 拒绝；currentPrice 超范围 → 拒绝
    // 计算 successRate（WIS + NPC性格 + 财运）
    // Roll 并计算变价幅度（1%~20%，浮动力度与掷骰余量挂钩）
    // 价格钳制在 minPrice ~ maxPrice 之间
    // 返回: {success, currentPrice, priceChange, reason}
    // 执行后标记 ShopChatContext.markHaggled()（成功与失败均标记）
}

@Tool(description = "从客人手里收购物品。itemId 可留空（按名称自动匹配），仅在重名时才需客人指定编号")
public SellResult buyItem(
    @ToolParam(description = "物品名称") String itemName,
    @ToolParam(description = "物品编号，可留空按名称匹配") String itemId,
    @ToolParam(description = "成交价格（灵石）") long confirmedPrice
) {
    // itemId 非空：先试 sellEquipment，仅 EQUIPMENT_NOT_FOUND 时回退 sellStackableItem
    // itemId 留空：按名称匹配；0 个 → ITEM_NOT_FOUND；多个 → ITEM_MULTIPLE_MATCH
    // 堆叠物品与装备均有概率性接受：acceptanceRate = 1 - (price - minPrice) / (maxPrice - minPrice)
    // 原子操作：扣物品 + 加灵石
}

@Tool(description = "卖给客人丹药/材料等堆叠物品。templateName 必须与 showGoods 商品名称一致")
public PurchaseResult sellGoods(
    @ToolParam(description = "商品名称") String templateName,
    @ToolParam(description = "购买数量") int quantity
) {
    // 按名称查模板 → purchaseItemInternal：原子扣灵石 + 加物品 + 减库存
}

@Tool(description = "卖给客人装备，品质随机。templateName 必须与 showGoods 商品名称一致")
public EquipmentPurchaseResult sellEquipment(
    @ToolParam(description = "装备名称") String templateName
) {
    // 随机生成 Equipment 属性（品质/词缀/品质系数）
    // 原子操作：扣灵石 + 发装备 + 减库存
}
```

工具结果中的玩家输入（物品名）会经 `sanitizeItemName` 剥离控制字符并限长 64，防止伪指令注入。

### 4.3 装备估价

装备价值和基础物品不同，需要根据其**稀有度、锻造等级、词缀**等属性计算。

**装备独有的验证**：

| 验证 | 通过条件 |
|------|---------|
| 归属 | `equipment.userId == currentUserId`（否则 EQUIPMENT_NOT_OWNED） |
| 状态 | 未装备（`equipped == false`；已装备提示「客官还穿着呢，脱下来再说吧」） |
| 绑定 | `equipment.tradable == true`（已装备过的会设 false，提示不可回收） |

文档中的 `status == 'OWNED'` 不存在；实际字段是 `equipped` 布尔值。

**估价公式**：

```
baseValue   = itemTemplate.base_value × rarityMultiplier × qualityMultiplier × forgeMultiplier × affixMultiplier

rarityMultiplier:   BROKEN=0.3, COMMON=1.0, RARE=2.0, EPIC=5.0, LEGENDARY=15.0
qualityMultiplier:   Equipment.qualityMultiplier（品质系数区间，按稀有度 0.8~2.0）
forgeMultiplier:     1.0 + forgeLevel × 0.15（每锻造一级 +15%）
affixMultiplier:     1.0 + Σ(affixPower) / 100（词缀强度总和）

finalOffer = baseValue × buy_price_modifier × worldMultiplier × categoryMultiplier
minPrice   = ROUND(finalOffer × 0.8)
maxPrice   = ROUND(finalOffer × 1.2)
```

**装备出售的 LLM 对话流程**：

```
玩家：掌柜，这柄玄铁剑你收不收？
                  ↓
LLM 比对该玩家拥有的未装备玄铁剑数量：
  ├── 唯一一件 → 直接 appraiseItem(equipmentId)
  └── 多件同名 → LLM 调用 checkPlayerItems()
                  → "客官，你包里有三柄玄铁剑，
                     一柄普通锻造、一柄+3、一柄带火属性词缀，
                     你是指哪一柄？"
                  ↓
玩家：那个+3的
                  ↓
LLM 通过 forgeLevel 匹配 → appraiseItem(对应 equipmentId)
                  ↓
"这柄锻造到+3了，做工不错，老朽出 360 灵石"
                  ↓
后续同标准砍价/成交流程
```

**估价叙事示例**：

| 装备状态 | LLM 回复 |
|---------|---------|
| 白板装备 | "玄铁剑，炼制手法中规中矩，老朽出 180 灵石" |
| +3 锻造 | "这柄锻到了+3，费了不少材料吧，老朽出 360 灵石" |
| 史诗品质 | "好剑！这锻造水准至少是大师手笔，老朽出 2000 灵石" |
| 传说 + 极品词缀 | "此剑有龙魂加持，世间罕有，老朽出 8000 灵石，客官当真要卖？" |
| 已装备 | "客官还穿着呢，脱下来再说吧" |
| 已绑定 | "此物伴有神魂烙印，收不得" |

装备估价的程序描述文案由 `buildEquipmentAppraisalDesc` 生成：`稀有度品质 +（锻造+N）+（X条词缀）+ 基准估价 X 灵石`。

---

## 5. 出售流程（掌柜 → 玩家）

保持原有设计不变，参见 `shop_product` 懒补货/懒调价 + LLM 对话。

```
玩家：掌柜，有什么好货？
  → LLM 调用 showGoods()
  → 返回当前区域商品
  → "客官来得巧，刚到了一批聚气丹，80 灵石一瓶"

玩家：来两瓶
  → LLM 调用 sellGoods(templateName, quantity)
  → 原子扣灵石 + 加物品 + 减库存
   → "好嘞，两瓶聚气丹，承惠 160 灵石"
```

### 5.1 装备购买

装备与堆叠物品不同——每件有独立属性（稀有度、品质浮动、词缀）。商铺的装备商品按模板出售，购买时随机生成属性。

```
shop_product
  ├── template_id 指向 equipment_template（如"玄铁剑"模板）
  ├── current_stock 表示库存数量（非装备实例数）
  └── current_price 为基准价

购买时（ShopService.purchaseEquipmentInternal）：
  → 校验 shop_product 存在、类型为 EQUIPMENT、库存 > 0
  → 扣灵石（原子条件更新）→ 减库存 1（原子条件更新）
  → 随机生成 Equipment 实例：
      rarity = Rarity.roll(template.dropWeight)     // 按模板掉落权重
      qualityMultiplier = rarity 区间内随机           // BROKEN 0.8~0.99 / COMMON 1.0~1.15 / RARE 1.16~1.35 / EPIC 1.36~1.60 / LEGENDARY 1.61~2.00
      affixCount = rarity 区间随机                    // BROKEN 0 / COMMON 1 / RARE 1~2 / EPIC 2~3 / LEGENDARY 3~4
      affixes = 从属性词缀池随机（LEGENDARY 额外含特殊词缀；特殊词缀值 5，普通词缀 1~4）
      forgeLevel = 0，tradable = true，equipped = false
  → 分配给玩家
```

**LLM 对话流程**：

```
玩家：掌柜，有什么好剑？
  → LLM 调用 showGoods()，过滤 equipment 类别
  → "小店刚到一批玄铁剑，品质上乘，200 灵石一柄"

玩家：来一把
  → LLM 调用 sellEquipment(templateName)
  → 系统按上述流程生成装备
  → "好嘞，客官运气不错，这柄玄铁剑隐隐有灵光，品质上佳！承惠 200 灵石"
```

**装备定价**：

```
售价 = shop_product.current_price  （标价，与品质无关）
收购价 = 见 §4.3 装备估价公式

※ 售价与装备实际品质解耦——玩家买的是"开盲盒"
※ 高品质装备让玩家觉得赚了，低品质也不会亏（售价固定）
※ 收购价则精确按装备实际属性计算
```

---

## 6. 价格引擎

### 6.1 收购价公式（实际）

```
收购价 = ROUND(base_value × buy_price_modifier × worldEventMultiplier × categoryMultiplier)，最低 1 灵石

worldEventMultiplier = Π(所有生效 ECONOMIC 世界事件的 globalMultiplier)
                      （仅当事件无 affected_tags 或与物品 tags 有交集时计入）
categoryMultiplier  = npc.categoryMultiplier[inferCategoryFromTags(item.tags)] ?? 1.0
```

标签→类别推断（`PriceEngine.inferCategoryFromTags`，大写后包含匹配）：HERB/MEDICINE → `HERB`；ORE/METAL/FORGE → `ORE`；POTION/PILL → `POTION`；SEED → `SEED`；BEAST/EGG → `BEAST_EGG`；其余 → `MATERIAL`。

**售价与文档的差异**：玩家从商铺购买时价格就是 `shop_product.current_price`（由懒补货/懒调价维护），购买路径不实时重算 `base_price × worldEventMultiplier × categoryMultiplier`；世界事件当前只影响**收购价**。

### 6.2 世界事件

世界事件系统已重构，ECONOMIC 类事件通过 `PriceEngine` 影响物品的**收购价**。详设见 [世界事件系统](./世界事件系统.md)。

---

## 7. 调货机制（设计预留，未实现）

> **代码现状**：`shop_special_order` 表（`V1.0.28`）、`ShopSpecialOrder` 实体、`ShopSpecialOrderRepository` 与相关错误码均已存在，但没有任何 Service、Tool 或命令调用——玩家侧调货流程尚未实现，本节仅为设计参考。

```sql
CREATE TABLE shop_special_order (
    id              BIGSERIAL PRIMARY KEY,
    player_id       BIGINT NOT NULL REFERENCES player(id),
    shop_npc_id     BIGINT NOT NULL REFERENCES shop_npc(id),
    template_id     BIGINT NOT NULL REFERENCES item_template(id),
    unit_price      BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    deposit         BIGINT NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'READY', 'COLLECTED', 'CANCELLED')),
    sourcing_hours  INT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

流程设计：收 10% 定金 → 固定调货时长 → 补尾款取货 → LLM 告知进度。

---

## 8. 对话历史存储

掌柜对话历史使用统一的对话基础设施（`chat_history` 表，`ChatType.SHOP`）。每条消息按 `(SHOP, shopNpcId, userId)` 存储，窗口上限 20 条，由 `PerTypeChatMemory` + `ChatMemoryRepositoryAdapter` 自动管理并修剪 DB 超限条目。

> 详见 [AI对话系统](./AI对话系统.md)

---

## 9. 旅行商人（扩展预留）

- 事件触发（复用 `travel_events`），非固定 NPC
- 价格浮动更大（0.5x - 3.0x）
- 同样走 LLM 对话
- 无 `shop_npc` 数据，事件中临时构建

代码现状：`NpcType.TRAVELER` 枚举已存在；旅行商人作为 CHOICE 子事件 `travel_curious_merchant` 存在于活动事件配置中，但无独立商铺交易实现。

---

## 10. 玩家指令

| 指令 | 说明 |
|------|------|
| `掌柜 {{内容}}` | 与当前所在地图商铺掌柜自然语言交互（买/卖/闲聊） |
| `回收 {{物品名}}` | 快速回收物品（当前实现为把「我想卖掉这个：X，按估价直接成交」交给掌柜 LLM 处理，并非绕过 LLM） |

**掌柜对话示例**：
- `掌柜 有什么好货？`
- `掌柜 聚气丹怎么卖？`
- `掌柜 来两瓶聚气丹`
- `掌柜 这柄剑你收不收？`
- `掌柜 有玄天丹吗？`
- `掌柜 最近生意如何？`

**快速回收**：`回收 玄铁剑` → 以直接成交意图向掌柜发起对话。

---

## 11. 经济循环总览

```
物品产出 ──────────────────────────┐
（秘境/悬赏/历练/事件）              │
          │                        ▼
          │              玩家背包
          │                │
          ▼                ▼
    ┌─────────────┐   ┌──────────┐
    │ 掌柜 购买 ←──│ ←─ 出售给掌柜│  ← 灵石流入玩家
    │ (shop_product) │  (回收)    │
    │ 灵石流出玩家 →│──→ 灵石扣除 │
    └──────┬──────┘   └──────────┘
           │
           ▼
     灵石被系统回收
     （炼丹、灵兽、福地、交易税等继续消耗）

※ 灵石只在玩家↔系统之间流动
※ 系统控制所有物价和回收量
※ 无玩家间交易 → 经济循环可控
```

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名无 `xt_` 前缀**：实际为 `item_template`、`inventory_item`、`equipment`、`shop_npc`、`shop_product`、`shop_special_order`、`chat_history`、`player`、`map_node`。
- **玩家间交易边界**：与设计一致——系统只提供玩家↔商铺交易，无任何玩家间转移物品/灵石的功能。
- **懒补货/懒调价参数为固定常量**：缺货阈值 4 小时、补货量 `max(1, max_stock/2)`；滞销条件 `current_stock > max_stock/2` 且 2 天、减仓量 `max(1, current_stock/5)`、涨价 +15% / 降价 -8%；无 `restockInterval` 等配置项。
- **砍价幅度为 1%~20%**：`amountRatio = (0.03 + successMargin × 0.12) × 财运` 后钳制 `[0.01, 0.20]`，`amount` 至少 1 灵石。
- **世界事件倍率为连乘**：收购价按 `Π(ECONOMIC 事件 globalMultiplier)` 计算，并按物品 tags 过滤（事件无 `affected_tags` 时对所有物品生效），非文档的 `Σ`。
- **装备状态字段为 `equipped` 布尔**：不存在 `status == 'OWNED'`。
- **工具命名与签名**：实际为 `showGoods` / `sellGoods` / `sellEquipment`；`purchaseEquipment` 是 Service 方法名而非工具入口；`appraiseItem(itemName, itemId)` 而非 `(templateId, itemId)`。

### B. 保留代码设计

- **`chk_stock` 允许买空至 0（`min_stock` 仅补货目标）**：玩家总能买走最后一件，避免「显示有货却买不到」的墙；卖空 4 小时后补货涨价，形成稀缺→补货的自然循环。
- **砍价无论成败都消耗本轮唯一机会**：若失败不消耗，玩家可反复重试直到成功，NPC 性格难度与悟性/财运差异将失去意义；spec 已契约化。
- **购买价 = `shop_product.current_price`，不实时叠加世界事件**：玩家看到的标价即成交价，避免显示价与结算价不一致；经济事件通过收购价与懒调价间接影响物价。
- **`回收` 走掌柜对话链路（不绕过 LLM）**：全部交易统一走掌柜，保证世界观反馈一致，也避免出现绕过估价/概率接受机制的第二套结算路径。
- **装备购买按 `equipment_template.drop_weight` 生成稀有度**：与战斗掉落共用同一品质表，同模板品质分布一致；低阶商品定价本就低廉（破旧对应便宜货），并保留开出传说的惊喜。

### C. 按设计修正（待修）

无。

### D. 未实现（待办）

- **调货机制**：设计意图 = 收 10% 定金 → 固定调货时长 → 补尾款取货 → LLM 告知进度；现状 = `shop_special_order` 表/实体/仓库/错误码齐全，但无任何 Service/Tool/命令调用。
- **旅行商人临时商铺**：设计意图 = 事件触发、价格 0.5x~3.0x、无 `shop_npc` 数据的临时商铺；现状 = 仅 `NpcType.TRAVELER`、`ChatType.TRAVELER` 与 `travel_curious_merchant` CHOICE 事件，无交易实现。

### E. 缺陷修复

- **装备商品售罄后永不补货**（已修）：`purchaseEquipmentInternal` 购买前执行 `applyLazyRestock` 并落库，购买与浏览库存一致。
