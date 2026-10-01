# 地图旅行 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 地图节点（map_node 表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `name` | VARCHAR(128) | 地图名称（NOT NULL） |
| `description` | TEXT | 描述 |
| `map_type` | VARCHAR(32) | 类型，CHECK ∈ SAFE_TOWN/TRAINING_ZONE/HIDDEN_ZONE |
| `level_requirement` | INT | 推荐/准入等级（NOT NULL，默认 1） |
| `neighbors` | JSONB | 相邻地图 `[{"targetId": 1, "minutes": 5}]`，默认 `[]` |
| `specialties` | JSONB | 特产掉落池 `[{"templateId": 1, "weight": 30}]`，默认 `[]` |
| `encounter_richness` | INT | 事件密度 1-10，默认 5，CHECK BETWEEN 1 AND 10 |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

索引：`idx_map_node_type(map_type)`、`idx_map_node_level(level_requirement)`。

实体行为（`MapNode`）：`isAccessibleBy(playerLevel)`（等级 ≥ level_requirement）、`isAdjacentTo(mapId)`、`getTravelTimeTo(mapId)`（返回相邻条目的 `minutes`，无路径返回 null）、`getAdjacentMapIds()`。

### 1.1 相邻地图（neighbors）JSONB

```json
[{"targetId": 1, "minutes": 5}, {"targetId": 2, "minutes": 3}]
```

- `targetId` 为目标地图 ID
- `minutes` 为旅行耗时（分钟）

### 1.2 特产（specialties）JSONB

```json
[{"templateId": 1, "weight": 50}, {"templateId": 3, "weight": 10}]
```

- `templateId` 为物品模板 ID
- `weight` 为掉落权重

### 1.3 事件密度（encounter_richness）

控制历练时遇怪和事件的触发频率。值越大间隔越短。

```
encounter_richness: 1~10，默认 5
  1  = 几乎无事发生（安全城镇）
  5  = 普通野外
  10 = 凶险之地，怪事频发
```

遇怪配置与旅行事件均已迁移到 `activity_event` 表（无 `monster_encounters` / `travel_events` 字段）：

- 历练遇怪：`activity_type='TRAINING'`、`owner_id=map_id`、`event_type='COMBAT'`，`params` 含 `monster_template_id` / `min_count` / `max_count`
- 历练子事件：同表 `event_type='NUMERIC'`（`params.effects`）或 `event_type='CHOICE'`
- 旅行事件：`activity_type='TRAVEL'`、`owner_id=map_id`、`event_type='NUMERIC'`

详见 [异步事件系统.md](../../../docs/异步事件系统.md#51-事件池存储--统一事件系统)。

### 1.4 遭遇规模

历练事件循环从 `activity_event` 统一池按 `weight` 加权抽取事件；命中 COMBAT 事件时使用其 `params` 中的怪物模板，再按 `min_count` / `max_count` 使用**正态分布**随机生成怪物数量（中点为均值、标准差 `(max-min)/4`，极值约 5% 概率）组成一波，数量下限为 1。

---

## 2. 怪物系统

### 2.1 怪物模板（monster_template）— 静态配置

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `name` | VARCHAR(128) | 怪物名称 |
| `description` | TEXT | 描述 |
| `monster_type` | VARCHAR(32) | 怪物类型（BEAST/SPIRIT/ARMORED/…） |
| `base_level` | INT | 基准等级 |
| `base_hp` | INT | 基准生命值 |
| `base_attack` | INT | 基准攻击力 |
| `base_defense` | INT | 基准防御力 |
| `base_speed` | INT | 基准速度 |
| `exp_reward` | INT | 击杀修为 |
| `skills` | JSONB | 法决 ID 列表 `[1, 2, 3]` |
| `drop_table` | JSONB | 掉落表 |
| `tags` | JSONB | 标签（克制判断等） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

### 2.2 掉落表（drop_table）JSONB

```json
[
  {"category": "equipment", "templateId": 1, "weight": 50},
  {"category": "items", "templateId": 10, "weight": 80}
]
```

- `category` 为 `"equipment"` 或 `"items"`，分别对应装备或物品
- `templateId` 引用对应的模板表 ID
- `weight` 为独立掉率百分比（0-100），每条掉落表项各自掷骰判定，不做按权重排序截断

掉落分发细节：物品数量为 `1 + random(0,2)` 再乘财富倍率（至少 1）；单次掉落最多保留 5 件，超限时随机保留而非按权重保留。装备按模板实例化生成一件。

### 2.3 怪物实例 — 运行时

不持久化，遇怪时由服务层动态生成一组怪物：

```
① 从地图的 activity_event（TRAINING）统一池按 weight 加权抽取事件；命中 COMBAT 时取其 params 中的怪物模板
② 根据选中条目的 min_count/max_count 正态分布决定本波数量
③ 每只怪物等级 = base_level + random(-2, 3) + 运势偏移（luck 决定，范围 -3~3），下限 1
④ 属性按等级差缩放：
     hp/attack = base × (1 + (level - baseLevel) × 0.15)，hp 缩放系数下限 0.1
     speed/defense = base × (1 + (level - baseLevel) × 0.1)
⑤ 生成一组临时怪物对象投入战斗
⑥ 战斗结束后销毁
```

怪物实例的属性由模板基准属性按实例等级与模板基准等级的差值缩放计算，而非按玩家等级比例缩放。

### 2.4 怪物类型

| 枚举 | 中文 | 被克制法器 |
|------|------|----------|
| BEAST | 妖兽 | 刀 |
| SPIRIT | 灵体 | 剑 |
| ARMORED | 甲胄 | 斧 |
| WILD_BEAST | 猛兽 | 枪 |
| EVIL | 邪祟 | 棍 |
| FLYING | 飞行 | 弓 |
| HUMAN | 人形 | 无特定克制 |

克制倍率仅对玩家攻击怪物生效，灵兽不参与属性克制。详见 [战斗系统 design](../../combat/design.md)。

---

## 3. 地图类型

| 类型 | code | 展示名 |
|------|------|--------|
| SAFE_TOWN | 安全区 | 仙家坊市 |
| TRAINING_ZONE | 野外历练区（有怪物） | 试炼之地 |
| HIDDEN_ZONE | 秘境入口节点 | 秘境洞天 |

### 3.1 秘境入口节点

`HIDDEN_ZONE` 类型的地图节点作为秘境的物理入口。

```
秘境入口 (HIDDEN_ZONE)
  │ 玩家通过旅行系统到达此处
  │ 输入「秘境 紫府秘境」
  ▼
秘境系统 (DungeonService)
  ├── 创建 dungeon_instance
  ├── 进入外围区域
  ├── 探索建筑 POI（复用 CombatEngine，BattleContext.scene=DUNGEON）
  ├── 通过「秘灵」对话/探索推进至内围/核心
  └── 击败镇守 BOSS 或撤退 → 结算退出
```

- 玩家必须先旅行到此地图节点，才能进入秘境（否则提示「你当前不在【秘境名】所在的位置」）
- 入口节点本身不参与历练（无 COMBAT 事件配置）
- 秘境内部的外围/内围/核心三重天地由秘境系统独立编排，不经过地图系统
- 探索中的战斗复用 `CombatEngine`，`BattleContext.scene=DUNGEON`

详见 [秘境系统 design](../dungeon/design.md)（原 `docs/秘境系统设计.md`）。

---

## 4. 旅行系统

### 4.1 旅行开始

- `前往 [地图名]` → 进入 TRAVELING 状态，记录活动开始时间和目的地 ID（`activity_target_id`）
- 约束：必须处于空闲（IDLE）状态、目标地图相邻、等级满足 `level_requirement`
- `neighbors` 中的 `minutes` 即为旅行耗时（分钟）

失败分支（玩家可见文案）：

| 场景 | 结果 |
|------|------|
| 非空闲状态 | 抛出 `STATUS_BLOCKED`：「您当前处于 X 状态，无法进行此操作（需要 空闲 状态）」 |
| 当前地图不存在 | 「当前地图不存在」 |
| 目标地图不存在 | 「未找到地图: X」 |
| 不相邻 | 「A 与 B 不相邻，无法直接前往」 |
| 等级不足 | 「需要达到 N 级才能前往 B」 |
| 无可用路径 | 「A 到 B 无可用路径」 |

成功文案：「开始前往 B，预计 N 分钟后到达」，并附「预计到达时间: MM-dd HH:mm」。

### 4.2 旅行自动结算

旅行到期后，下一次用户交互时自动结算（懒加载模式，`UserStateService.loadUser` 获取行锁后依次执行 `StateHandler`）：

```
出发时间 + 当前地图到目的地的旅行耗时 ≤ 当前时间 → 到达目的地
更新所在地为目的地
状态切换为空闲（清除活动字段）
触发旅行到达流程（到达通知 + 子事件/隐藏事件/环境事件）
```

**触发点**：所有经 `loadUser` 加载用户状态的入口（状态查询、前往、历练、悬赏接取等）。只读加载（`loadUserReadOnly`）不结算。

**卡死保护**：若当前地图已无通往目的地的路径，清除活动状态并记录告警，不改变所在地。

### 4.3 状态展示优化

`状态` 命令在赶路时显示优化格式（而非仅显示"赶路"）：

```
所在地：青山镇
状态：赶路中 (青山镇 → 青云山外门)
旅途进度：12分钟/15分钟（剩余 3分钟）
```

进度基于出发时间和旅行耗时实时计算；剩余为 0 时显示「（即将到达）」。缺少旅行耗时数据时回退显示「预计到达：MM-dd HH:mm」。

### 4.4 旅行事件

旅行到达时由 `TravelCompleter` 触发（不再有固定的事件枚举表）：

1. **到达通知**：「你经过一路跋涉，终于抵达了{目的地}。」
2. **旅行子事件**：基础触发概率 0.30（乘机缘 fate 修正，0.21~0.39），从 `activity_event`（`activity_type='TRAVEL'`、`owner_id=地图ID`、`is_hidden=false`）按 weight 加权抽取一条 NUMERIC 事件，读取 `params.effects` 执行效果并生成通知。
3. **隐藏事件**：按触发条件（如属性阈值 `STAT_THRESHOLD`、持有物品 `HAS_ITEM`、持有法决 `HAS_SKILL`）检查，同一事件每人每图只触发一次（`hidden_completion` 记录）。
4. **环境世界事件**：应用当前地图区域与全局的 ENVIRONMENTAL 世界事件效果。

事件效果类型见 `SubEventEffectType`：加修为（固定/百分比）、按比例/固定扣血、治疗、加物品、随机物品、生成装备、掉落特产、加/扣灵石、悬赏奖励倍率、纯叙事。种子数据示例（青石镇 map_id=1）：

| code | 效果示例 |
|------|----------|
| travel_ambush_beast | 扣 8% 气血 + 修为 +30 |
| travel_broken_cart | 30% 概率随机获得灵芝/玄铁矿石 |
| travel_friendly_merchant | 50% 概率随机获得灵芝/灵木 |
| travel_wandering_elder | 悟性 ≥ 20 时修为 +3% |

原文档的 AMBUSH/FIND_TREASURE/WEATHER/SAFE_PASSAGE 固定事件枚举与 `travel_events` JSONB 已废弃；旅行不再进入战斗。

---

## 5. 地图指令

### 5.1 `地图` — 查看当前所在

显示当前所在地图完整信息：

```
【地图名】（地图类型展示名 · 境界显示）

地图描述

【妖兽出没】
  怪物名 [类型·境界]  数量:1~3

【四方可达】
  相邻地图名（御剑N分钟）

使用「前往 [地名]」启程修行。
```

- 怪物列表来自 `activity_event`（TRAINING/COMBAT），批量查询后按 weight 降序排列；只展示名称、类型、基准等级与数量范围，**不展示权重**
- 相邻地图展示名称与旅行耗时，并附带「前往 X」快捷操作建议
- 无怪物时不显示「妖兽出没」段；无相邻地图时不显示「四方可达」段

### 5.2 `地图列表` — 查看全部地图

**未实现为玩家指令**。`MapService.getAllMaps()` / `loadAllMaps()` 存在（带 `map_data` 缓存），但没有监听器暴露对应命令；原文档的「地图列表」输出仅为设计设想，当前不可用。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名**：`xt_map_node` → `map_node`、`xt_activity_event` → `activity_event`；无 `monster_encounters` / `travel_events`，遇怪与旅行事件统一在 `activity_event`。
- **地图类型展示名**：SAFE_TOWN「仙家坊市」、TRAINING_ZONE「试炼之地」、HIDDEN_ZONE「秘境洞天」（「安全区/野外历练区/秘境入口节点」仅作语义说明）。
- **`地图` 输出**：不展示遇怪权重；标题为「（类型名 · 境界显示）」；段标题「妖兽出没」「四方可达」；提示语「使用「前往 [地名]」启程修行。」。
- **旅行失败文案与状态约束**：非空闲抛 `STATUS_BLOCKED`；当前/目标地图不存在、不相邻、等级不足、无路径各有独立文案（见 §4.1）。

### B. 保留代码设计

- **怪物实例缩放**：按「实例等级 − 模板基准等级」差值缩放（hp/attack 系数 0.15、speed/defense 系数 0.1），实例等级 = 基准 ±2 + 运势偏移。文档「按玩家等级比例缩放」会让任何地图的怪物都与玩家同级，抹平地图难度梯度，地图等级、遇怪间隔与等级衰减机制将失去意义。
- **遭遇数量**：正态分布（中点为均值、极值约 5%）让常见规模集中在中点、极端大群罕见，避免均匀随机频繁刷出最大波导致挂机挫败。
- **掉落模型**：每条掉落表项独立按百分比掷骰（weight 即掉率），避免按权重排序截断让稀有掉落被高权重条目结构性挤出；财富只作用于数量（只增不减），单次 5 件上限随机保留防止掉落爆炸。
- **旅行事件**：统一 `activity_event` 数据驱动 + 隐藏/环境事件，旅行不再触发战斗——赶路被战斗打断的挫败消失，且与历练/悬赏共用一套事件引擎。
- **卡死保护**：目的地路径消失时清除旅行状态而非按原记录传送，避免玩家被传到不可达/不该去的地图并永久卡在旅行态。
- **`历练` 门槛引导**：非历练区返回 BFS 最近历练区指引（含「前往」指令与推荐境界），降低新手在安全区反复尝试的迷路成本。
- **秘境推进**：以「秘境」「秘境 [名]」「秘灵 [内容]」对话式推进替代固定「秘境继续」，与掌柜/地灵/宗灵等 AI 入口一致，探索叙事更自然。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

- **`地图列表`**：设计意图为世界地图总览（每张地图展示怪物概览与相邻关系）；现状 `MapService.getAllMaps()` / `loadAllMaps()` 存在且带 `map_data` 缓存，但没有监听器暴露命令，玩家不可用。

### E. 缺陷修复

无
