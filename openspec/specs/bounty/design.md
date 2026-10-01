# 悬赏系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

悬赏是玩家在地图节点接取的限时任务。接取后需等待指定时长，结算时发放接取时预确定的奖励（物品/灵石/兽卵/装备/法决玉简等），并可能触发悬赏支线事件与隐藏事件。

核心特性：
- **奖励预确定**：接取时即确定奖励内容并写入 DB，结算时直接发放，不重算
- **每日悬赏榜**：同一用户、同一地图、同一天看到的悬赏列表固定（种子随机选取 3~4 条）
- **唯一悬赏**：标记 `is_unique` 的悬赏完成后不再出现、不可再接
- **LLM 美化**：结算时调用 AI 生成叙事描述

---

## 2. 数据库设计

### 2.1 bounty — 悬赏配置表

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `map_id` | BIGINT | 所属地图 ID（FK → map_node） |
| `name` | VARCHAR(100) | 悬赏名称 |
| `description` | TEXT | 悬赏描述 |
| `duration_minutes` | INTEGER | 悬赏耗时（分钟，CHECK > 0） |
| `rewards` | JSONB | 奖励池 |
| `require_level` | INTEGER | 最低接取等级（默认 1，CHECK ≥ 1） |
| `event_weight` | INTEGER | 悬赏榜选取权重（CHECK ≥ 0） |
| `is_unique` | BOOLEAN | 是否为唯一悬赏（完成后不可再接，默认 FALSE） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

索引：`idx_bounty_map_id(map_id)`。

**rewards JSONB 格式**:

```json
[
  {"type": "rare_item", "min": 1, "max": 3, "template_id": 1},
  {"type": "spirit_stones", "min": 50, "max": 200},
  {"type": "beast_egg", "name": "兽卵"},
  {"type": "equipment", "template_id": 5}
]
```

- `type`: 奖励类型，支持 `rare_item` / `spirit_stones` / `beast_egg` / `equipment` / `skill_jade` / `potion` / `recipe_scroll` / `forging_blueprint`
- `min` / `max`: 产出数量范围（rare_item 为物品数，spirit_stones 为灵石数）
- `template_id`: 指定物品/装备/丹药/丹方/图纸模板 ID
- `name`: (beast_egg) 兽卵显示名称
- `weight`: 旧版遗留字段，当前解析时被忽略；**所有奖励池条目都会展开发放**（不再按权重只选一条）

### 2.2 user_bounty — 用户悬赏记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | 用户 ID（FK → player） |
| `bounty_id` | BIGINT | 悬赏 ID（FK → bounty） |
| `bounty_name` | VARCHAR(100) | 悬赏名称（快照） |
| `start_time` | TIMESTAMP | 接取时间 |
| `duration_minutes` | INTEGER | 悬赏耗时（分钟） |
| `rewards` | JSONB | 预确定的奖励物品 |
| `hidden_clues` | JSONB | 隐藏事件线索（预留字段，当前写入空对象） |
| `status` | VARCHAR(16) | `ACTIVE` / `COMPLETED` / `ABANDONED` |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

- `rewards` 在接取时预计算后写入，结算时直接读取发放
- `status` 状态机: `ACTIVE` → `COMPLETED` / `ABANDONED`
- 约束：`UNIQUE INDEX idx_one_active_bounty ON user_bounty(user_id) WHERE status='ACTIVE'`（每人同时最多一条进行中悬赏）

---

## 3. 奖励体系

### 3.1 奖励池类型（BountyRewardPool，8 种变体）

| 类型 | 来源 | 说明 |
|---|---|---|
| `rare_item` | 地图特产 | 按 `min~max` 数量从 `map_node.specialties` 加权随机选取 |
| `spirit_stones` | 固定数值 | 随机 `min~max` 灵石，结算时经财富倍率后入账 |
| `beast_egg` | ItemTemplate (BEAST_EGG) | 指定 `template_id` 或从全部 BEAST_EGG 中随机选 1 个 |
| `equipment` | 装备模板 | 按 `template_id` 生成 1 件装备 |
| `skill_jade` | 法决玉简模板 | 按 `template_id` 发放 1 个法决玉简 |
| `potion` | 丹药模板 | 按 `template_id` 发放 1 个丹药 |
| `recipe_scroll` | 丹方卷轴模板 | 按 `template_id` 发放 1 个丹方卷轴 |
| `forging_blueprint` | 锻造图纸模板 | 按 `template_id` 发放 1 个锻造图纸 |

通过 Jackson 多态反序列化，`type` 字段值即上表 code。

### 3.2 物品选取 (rare_item)

按条目 `min~max` 确定件数，每件从当前地图 `map_node.specialties` 中按 `weight` 加权随机选取 1 个模板，数量固定 1，同名不合并。

特产 JSONB 格式:

```json
[
  {"templateId": 1, "weight": 50},
  {"templateId": 3, "weight": 10}
]
```

- `templateId` 为 item_template 主键，`weight` 为权重

---

## 4. 奖励预确定机制

接取悬赏时用随机数生成器预计算奖励内容并写入 `user_bounty.rewards`。

```
seed = userId × 31 + LocalDate.now().toEpochDay() + ThreadLocalRandom.nextLong()
rng = new Random(seed)
```

**特性**:
- 奖励写入 DB 后不再变动，结算时直接读取
- 同一用户同日接取同一悬赏的奖励**不保证相同**（seed 含随机项；原「同日幂等」设计已失效）
- 跨日 seed 变化 → 不同日期奖励不同

### 4.1 预确定流程

```
1. 构建 rng(seed)
2. 遍历 bounty.rewards 全部条目（不再按 weight 只选一条）：
   - rare_item     → 从 mapNode.specialties 加权选取 min~max 件
   - spirit_stones → 随机产出 min~max 灵石
   - beast_egg     → template_id 存在则取该模板；否则从 ItemTemplate(BEAST_EGG) 随机选 1 个
   - equipment     → 按 template_id 查询 EquipmentTemplate，生成 1 件
   - skill_jade / potion / recipe_scroll / forging_blueprint
                   → 按 template_id 查询 ItemTemplate，生成 1 个
3. 写入 user_bounty.rewards (JSONB)
```

模板缺失时该条目静默跳过（不生成占位奖励）。

### 4.2 内部标记（BountyRewardItem，5 种变体）

预确定的 rewards 使用 `_rewardType` 标记字段区分奖励类型：

```json
[
  {"_rewardType": "spirit_stones", "amount": 100},
  {"_rewardType": "BEAST_EGG", "name": "兽卵", "templateId": 3, "quantity": 1},
  {"_rewardType": "equipment", "name": "装备", "templateId": 5, "quantity": 1},
  {"_rewardType": "skill_jade", "name": "法决玉简", "templateId": 7, "quantity": 1},
  {"_rewardType": "item", "templateId": 1, "name": "灵芝", "quantity": 1}
]
```

- `spirit_stones`: `_rewardType = "spirit_stones"`, 字段 `amount`
- `BEAST_EGG`: `_rewardType = "BEAST_EGG"`, 字段 `name` / `templateId` / `quantity`
- `equipment`: `_rewardType = "equipment"`, 字段 `name` / `templateId` / `quantity`
- `skill_jade`: `_rewardType = "skill_jade"`, 字段 `name` / `templateId` / `quantity`
- 普通物品 (ItemReward): `_rewardType = "item"`；兼容缺失标记的旧数据（按 ItemReward 解析）

`toMap()` 序列化为 JSONB 兼容 Map，`parse()` / `parseOne()` 反序列化。

---

## 5. 业务流程

### 5.1 操作流程

```
#悬赏（无进行中悬赏时显示列表）
  → 查询当前地图的悬赏（SQL 过滤 require_level ≤ 用户等级）
  → 排除已完成的唯一悬赏
  → 种子随机选取 3~4 条（按 event_weight 加权不放回）
  → 显示 ID / 名称 / 耗时 / 描述 / 推荐境界 / 奖励预览

#悬赏（有进行中悬赏时显示状态）
  → 显示悬赏名 / 描述 / 已过/总时长（剩余或已可结算）/ 预计奖励
  → 悬赏定义缺失时降级为列表展示

#悬赏接取 [ID]
  → 验证: 用户 IDLE / 编号合法 / 悬赏存在 / 地图匹配 / 等级满足 / 唯一悬赏未完成
  → 预确定奖励 → 写入 user_bounty (status=ACTIVE)
  → 用户 status → BOUNTY
  → 返回「已接取悬赏「X」，预计 N 分钟后完成。」

  ... 等待 duration_minutes 分钟 ...

#悬赏结算
  → 验证: 用户 BOUNTY / 存在 active 记录 / 时间 ≥ duration_minutes
  → 读取预存 rewards → 物品/兽卵/法决玉简入包、装备实例化、灵石入账户（×财富倍率）
  → 悬赏支线事件（必掷一次，按机缘概率触发，可放大灵石奖励）
  → 隐藏事件检查
  → 完成通知 + LLM 美化叙事
  → 标记 COMPLETED → 用户 status → IDLE

#悬赏放弃
  → 验证: 用户 BOUNTY / 存在 active 记录
  → 标记 ABANDONED（无任何产出）→ 用户 status → IDLE
```

### 5.2 状态约束

| 操作 | 要求用户状态 | 完成后状态 |
|---|---|---|
| 悬赏（列表/状态） | 无状态校验（有进行中悬赏时优先显示状态） | — |
| 悬赏接取 | IDLE | BOUNTY |
| 悬赏结算 | BOUNTY | IDLE |
| 悬赏放弃 | BOUNTY | IDLE |

### 5.3 结算时时间检查

计算从接取到当前时间的已过分钟数，若小于悬赏要求时长则抛出异常：「悬赏「X」还需 N 分钟（共需 M 分）」。

### 5.4 每日悬赏榜

列表选取种子：`userId × 31 + mapId × 17 + 当日 epochDay`，同一用户同一地图同一天结果固定；数量 `3~4`（不超过可用条数），按 `event_weight` 加权不放回抽取。

---

## 6. 悬赏支线事件与隐藏事件

悬赏结算时触发（与旅行系统共享 `activity_event` 机制，**不再使用 D20 判定**）：

1. **支线事件**：从 `activity_event`（`activity_type='BOUNTY_SIDE'`、`owner_id=bounty_id`、`is_hidden=false`）按 weight 加权抽取，触发概率为机缘（fate）修正值（`1.0 × getFateMultiplier`，clamp 至 1.0，约 0.7~1.0；事件池为空时除外）。效果可包含 `MULTIPLY_BOUNTY_REWARD`（按倍率放大灵石奖励）、加修为/扣血/加物品/加灵石/纯叙事等。
2. **隐藏事件**：按触发条件检查，每人每悬赏只触发一次（`hidden_completion` 记录），事件类型为 `BOUNTY_HIDDEN`。
3. **环境/世界事件**不参与悬赏结算。

灵石最终数值 = 预存奖励灵石 × 支线事件倍率 × 财富（wealth）倍率（`0.85 + wealth / 333`），经 `SpiritStoneService.deposit` 入账。

原文档 §6 的 D20 ≤ 10 判定与 AMBUSH（10-29 伤害）/FIND_TREASURE（5-19 灵石）/WEATHER/SAFE_PASSAGE 固定事件表已废弃。

---

## 7. 代码结构

### 7.1 目录树

```
domain/bounty/
├── entity/
│   ├── Bounty.java            # 悬赏配置实体 (表 bounty)
│   └── UserBounty.java        # 用户悬赏记录实体 (表 user_bounty)
├── vo/
│   ├── BountyVO.java               # 悬赏列表展示 VO
│   ├── BountyRewardVO.java         # 悬赏完成结果 VO
│   └── BountyStatusVO.java         # 悬赏状态 VO
├── BountyRewardPool.java           # 奖励池密封接口 (8 种变体)
├── BountyRewardItem.java           # 预存奖励密封接口 (5 种变体)
└── enums/
    └── BountyStatus.java           # ACTIVE / COMPLETED / ABANDONED

infrastructure/
├── mapper/ (BountyMapper / UserBountyMapper)
└── repository/ (BountyRepositoryImpl / UserBountyRepositoryImpl)

service/bounty/
├── BountyService.java              # 悬赏业务入口（列表/接取/状态/放弃/结算编排）
└── BountyCombatService.java        # 悬赏结算子流程（发放/事件/LLM 美化）

service/activity/
└── BountyCompleter.java            # 悬赏完成器（完成叙事/支线事件/隐藏事件）

handle/
├── command/MapCommandHandler.java  # 悬赏命令 → 文本格式化
└── listener/MapListener.java       # QQ 平台监听器
```

### 7.2 核心组件

**BountyRewardPool**: 奖励池密封接口，8 种变体（见 §3.1），Jackson 多态反序列化 `type` 字段。

**BountyRewardItem**: 预存奖励密封接口，5 种变体（见 §4.2），提供 `toMap()` / `parse()` / `parseOne()`。

**BountyRepository**: 按 ID 查询、按地图 ID 查询、按等级过滤（含排除唯一已完成）查询。

**UserBountyRepository**: 按用户查 active（含行锁版本）、查已完成唯一悬赏 ID、持久化记录。

**BountyService**: 悬赏业务入口。
- `listBounties(userId)` — 当前地图悬赏榜（等级过滤 + 唯一排除 + 每日种子选取 3~4 条）
- `getBountyStatus(userId)` — active 悬赏的已过/剩余时间与预计奖励
- `startBounty(userId, bountyId)` — 校验后预计算奖励写入 DB，更新用户状态
- `abandonBounty(userId)` — 标记放弃，恢复空闲
- `completeBounty(userId)` — 委托 `BountyCombatService`，事务提交后 LLM 美化
- `determineRewards(bounty, mapNode, rng)` — 展开全部奖励池条目

**BountyCombatService**: 结算子流程。
- `completeBounty(userId)` — 验证状态/时间，读取预存奖励，发放，支线事件，隐藏事件，更新记录与状态
- `addRewardsToInventory()` — 非灵石奖励统一经 `RewardGrant`（物品/兽卵/玉简入包、装备实例化）
- `beautify(completed)` — 事务提交后调用 `ExplorationDescriptionFunction` 生成叙事（失败内部兜底）

**BountyCompleter**: `produceCompletionEvent()`（完成通知）、`produceReadyEvent()`（已可领取提示）、`rollBountySideEvent()`（支线事件）、`checkHiddenEvents()`（隐藏事件）。

---

## 8. 命令参考

| 命令 | 说明 | 要求 |
|---|---|---|
| `悬赏` | 有进行中悬赏显示状态，否则显示当前地图可用悬赏列表 | 状态 IDLE |
| `悬赏接取 [ID]` | 接取指定悬赏 | 状态 IDLE, 地图匹配, 等级满足, 唯一悬赏未完成 |
| `悬赏结算` | 结算已完成的悬赏 | 状态 BOUNTY, 时间达标 |
| `悬赏放弃` | 放弃当前悬赏 | 状态 BOUNTY（无产出） |

原文档的「悬赏列表」「悬赏提交」命令名不存在。

---

## 9. 数据流图

```
接取悬赏:
  Player → QQ 监听器捕获命令
         → 命令处理器解析参数、调用业务服务
         → 服务层验证用户状态/悬赏存在/地图匹配/等级/唯一性
         → 预计算: 遍历 bounty.rewards 全部条目展开具体奖励
         →    rare_item: 从 mapNode.specialties 加权随机选 N 件
         →    beast_egg: 指定模板或从 ItemTemplate 随机选 1 个
         →    equipment: 按 templateId 生成 1 件
         → 写入 user_bounty (含预确定奖励 JSONB)
         → 用户状态设为 BOUNTY
         → 返回确认文本

结算悬赏:
  Player → QQ 监听器捕获命令
         → 命令处理器调用业务服务
         → 服务层验证用户状态/时间已到
         → 读取 user_bounty.rewards
         → 发放物品/兽卵/法决玉简 (RewardGrant → StackableItemService)
         → 发放装备 (RewardGrant → EquipmentService)
         → 发放灵石 (×支线倍率 ×财富倍率 → SpiritStoneService)
         → 悬赏支线事件（activity_event BOUNTY_SIDE）
         → 隐藏事件检查
         → LLM 美化叙事 (ExplorationDescriptionFunction，事务提交后)
         → 标记悬赏为 COMPLETED + 用户状态恢复 IDLE
         → 返回 BountyRewardVO（含奖励描述 + 叙事）
```

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名**：`xt_bounty` → `bounty`、`xt_user_bounty` → `user_bounty`；新增 `is_unique` / `hidden_clues` 字段。
- **状态码**：`active/completed/abandoned` → `ACTIVE/COMPLETED/ABANDONED`（与 CHECK 一致）。
- **`_rewardType` 标记**：普通物品也有 `_rewardType: "item"`，新增 `skill_jade`，兼容缺失标记的旧数据。
- **命令名**：`悬赏列表` → `悬赏`（自动区分状态/列表）；`悬赏提交` → `悬赏结算`。
- **LLM 美化**：事务提交后调用；`BountyRewardVO.eventDescription` 恒为 null（事件描述由通知承载），`rewardDescription` 被美化文本替换。

### B. 保留代码设计

- **奖励池扩展至 8 种**：新增 skill_jade / potion / recipe_scroll / forging_blueprint，悬赏成为法决玉简、丹药、丹方、锻造图纸的稳定产出入口，支撑多系统养成。
- **全部条目展开发放**：种子数据按「完整报酬清单」配置（灵石 + 物品 + 指定稀有件数），全部发放使接取时的「预计奖励」与实际完全一致；按权重只选一条会让配置中的其它奖励永远无法获得。
- **支线事件**：与旅行/历练共用 `activity_event` 数据驱动池 + 机缘修正（0.7~1.0），替代固定 D20 判定与 AMBUSH/FIND_TREASURE 表，配置零 Java 改动。
- **灵石倍率**：结算叠加支线事件倍率与财富倍率，与运势系统接入点一致，财运有了实际价值。
- **每日悬赏榜**：当前地图 + 等级过滤 + 唯一排除 + 每日种子选取 3~4 条，减少选择负担且每天有新鲜感；`event_weight` 用于榜单抽取，语义清晰。
- **唯一悬赏**：`is_unique` 完成后不再出现、不可再接，提供一次性稀有奖励与长期目标。
- **列表状态门槛**：查询不做状态校验（有进行中显示状态、否则显示列表），比「需 IDLE」更少无意义限制；接取仍严格要求 IDLE。

### C. 按设计修正（已修）

- **奖励种子同日幂等**（已修）：种子去掉随机项，改为 `userId × 31 + bountyId × 17 + epochDay`，同一玩家同日同悬赏奖励恒定，「放弃重接」无法再重掷。

### D. 未实现（待办）

无

### E. 缺陷修复

无
