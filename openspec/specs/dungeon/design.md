# 秘境系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 设计理念

秘境不是「菜单指令驱动的副本列表」，而是**有意志的活的空间**。

每个秘境可以有秘境之灵（有灵模式）或纯粹由 LLM 叙事驱动（无灵模式）。玩家进入秘境后，通过自然语言与秘境之灵 / 叙事者对话来探索，不再需要 `秘境探索` / `秘境继续` 等固定指令。

核心原则：

- **场景、怪物、奖励系统预生成**，LLM 只负责叙事美化和驱动节奏
- **玩家行为影响走向**——谨慎度、态度、观察力影响隐藏发现和好感度
- **隐藏内容驱动探索欲**——每个区域有隐藏 POI 和隐藏区域，错过就是错过

## 2. 指令（以代码为准）

实际注册 4 个命令模板：

| 指令 | 条件 | 行为 |
|---|---|---|
| `秘境` | 空闲状态 | 展示当前开放的秘境列表（名称、五行、境界范围、队伍上限、奖励进度 / 首通、入口位置、不可进入原因） |
| `秘境` | 已在秘境中 | 展示进度概览（纯静态，不调 LLM） |
| `秘境 名` | 空闲状态 | 快捷进入已知秘境，成功后返回开场叙事 |
| `秘灵 内容` | 秘境中 | 与秘境之灵 / 叙事者对话，由 LLM 调用探索工具 |
| `秘灵`（无内容） | 任意 | 返回对话用法引导 |

**已删除**：`秘境探索`、`秘境继续`、`秘境撤退`（未注册；错误文案中仍残留「输入『秘境探索』继续探索」，见差异）。

## 3. 数据模型

### 3.1 `dungeon_template` — 秘境模板

```sql
CREATE TABLE dungeon_template(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE,
    description     TEXT,
    element_type    VARCHAR(16),          -- METAL/WOOD/WATER/FIRE/EARTH（CHECK）
    min_level       INT NOT NULL,
    max_level       INT NOT NULL,
    max_team_size   INT NOT NULL DEFAULT 1,   -- 仅展示，无组队机制
    timeout_hours   INT NOT NULL DEFAULT 4,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    access_rules    JSONB,                -- 入口条件
    spirit_config   JSONB,                -- 秘境之灵配置（null=叙事者模式）
    area_configs    JSONB NOT NULL,       -- 区域+POI完整配置
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 `access_rules` JSONB — 入口条件

```json
{
  "display_tag": null,
  "conditions": [
    {"type": "MAP_NODE", "node_ids": [31]},
    {"type": "LEVEL", "min": 30, "max": 75}
  ]
}
```

> 实际解析结构为 `[{type, node_ids?, min?, max?, sect_id?, template_id?, dungeon_id?, code?}]`（顶层数组，非 `conditions` 包装；`display_tag` 无对应字段）。

条件类型（条件存在即硬性要求，不存在即不限制）：

| type | 参数 | 语义 | 实现状态 |
|---|---|---|---|
| `MAP_NODE` | `node_ids: [long]` | 玩家位置在指定地图节点 | 已实现 |
| `LEVEL` | `min/max: int` | 境界范围 | 已实现 |
| `SECT` | `sect_id: long` | 指定宗门成员 | **未实现**：命中即报「宗门限制暂未实现」 |
| `ITEM` | `template_id: long` | 背包中有指定物品 | 已实现 |
| `DUNGEON_CLEARED` | `dungeon_id: long` | 已通关某秘境（按首通标记判定） | 已实现 |
| `HIDDEN_COMPLETION` | `code: string` | 已完成某隐藏事件 | 已实现 |

枚举 `DungeonAccessConditionType` 存在但未被使用（判定按字符串 switch）。

### 3.3 `spirit_config` JSONB — 秘境之灵（可选）

```json
{
  "spirit_name": "紫府剑灵",
  "spirit_appearance": "一柄插在古碑前的残剑轻颤，剑身映出模糊人影",
  "personality": "威严寡言，欣赏勇气与坚持，厌恶怯懦与谄媚",
  "tone_style": "文言为主，短促有力",
  "greeting": "又见求道者…能走到紫府深处，算你有几分本事。",
  "affection_system": true
}
```

`spirit_name` 为 null 时走叙事者模式——LLM 以旁白口吻描述环境，无好感系统。`affection_system` 控制好感工具是否可用；注意「是否有秘境之灵」只看 `spirit_name` 是否为空。

### 3.4 `area_configs` JSONB — 区域+POI 完整配置

```json
[
  {
    "key": "outer",
    "name": "外围",
    "description": "灵雾缭绕的山门区域",
    "type": "MAIN",
    "main_pois": [
      {
        "name": "青石牌坊",
        "type": "SEARCH",
        "description": "布满剑痕的古朴牌坊，似乎记载着什么",
        "loot_pool": [{"template_id": 1, "min_qty": 3, "max_qty": 6, "weight": 100}],
        "monster_pool": null,
        "clues": null
      },
      {
        "name": "试炼碑",
        "type": "COMBAT",
        "description": "三丈高的黑色石碑散发着压迫感",
        "loot_pool": [
          {"template_id": 1, "min_qty": 30, "max_qty": 80, "weight": 100},
          {"template_id": 456, "weight": 30}
        ],
        "monster_pool": [
          {"template_id": 101, "min_count": 2, "max_count": 4, "weight": 100}
        ]
      }
    ],
    "hidden_pois": [
      {
        "name": "坍塌丹室",
        "type": "SEARCH",
        "description": "藏在回廊下的秘密丹房",
        "loot_pool": [{"template_id": "spirit_pill", "weight": 50}],
        "clues": ["回廊地砖有空鼓声", "砖缝有丹香逸出"]
      }
    ],
    "hidden_areas": [
      {"key": "secret_valley", "trigger_after_resolve": ["坍塌丹室"]}
    ]
  },
  {
    "key": "secret_valley",
    "name": "剑意山谷",
    "type": "HIDDEN",
    "main_pois": [...],
    "hidden_pois": [...]
  }
]
```

POI 类型：

| type | 行为 |
|---|---|
| `COMBAT` | 战斗（从 `monster_pool` 按权重抽，战斗引擎模拟） |
| `GATHER` | 采集（从 `loot_pool` 按权重抽） |
| `SEARCH` | 搜索（20% 概率触发战斗，否则掉宝） |
| `PASSAGE` | 通往下一区域（主线全清后自动激活，不需 LLM 调用，也不能主动探索） |

#### POI 字段说明

| 字段 | 说明 |
|---|---|
| `name` | POI 名称 |
| `type` | COMBAT/GATHER/SEARCH/PASSAGE |
| `description` | 给 LLM 看的描述文本（提示它如何描述这个地点） |
| `loot_pool` | `[{template_id, min_qty?, max_qty?, weight}]`（数量缺省 1） |
| `monster_pool` | `[{template_id, min_count?, max_count?, weight}]`，仅 COMBAT 需要（数量缺省 1） |
| `clues` | 线索文本列表。有值时 LLM 在环境描述中自然暗示；为空时纯靠玩家自主发现 |

> `hidden_areas` / `trigger_after_resolve` 会被解析但**无任何消费逻辑**；`DungeonSpiritState.triggeredEvents` 字段有 setter 但无调用方。

### 3.5 `dungeon_instance` — 运行时实例

```sql
CREATE TABLE dungeon_instance(
    id                BIGSERIAL PRIMARY KEY,
    dungeon_id        BIGINT NOT NULL REFERENCES dungeon_template(id),
    leader_id         BIGINT NOT NULL REFERENCES player(id),
    current_area_key  VARCHAR(32) NOT NULL,   -- 对应 area_configs[].key
    passage_unlocked  BOOLEAN NOT NULL DEFAULT FALSE,
    explored_pois     JSONB NOT NULL DEFAULT '[]'::jsonb,
    status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'COMPLETED', 'FAILED', 'ABANDONED')),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at        TIMESTAMP NOT NULL,
    completed_at      TIMESTAMP
);
```

唯一约束实际为**部分唯一索引**：`UNIQUE (leader_id, dungeon_id) WHERE status = 'ACTIVE'`（文档写 `UNIQUE (leader_id, dungeon_id, status)`，不一致）。另有 leader / dungeon / expires_at(ACTIVE) 索引。

### 3.6 `dungeon_progress` — 奖励记录

```sql
CREATE TABLE dungeon_progress(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES player(id),
    dungeon_id        BIGINT NOT NULL REFERENCES dungeon_template(id),
    last_reward_date  DATE NOT NULL,
    reward_count      INT NOT NULL DEFAULT 0,
    daily_limit       INT NOT NULL,
    first_clear       BOOLEAN NOT NULL DEFAULT FALSE,
    best_area         VARCHAR(32),
    interaction_count INT NOT NULL DEFAULT 0,
    UNIQUE (user_id, dungeon_id)
);
```

### 3.7 `dungeon_spirit_state` — 秘境之灵好感度

```sql
CREATE TABLE dungeon_spirit_state(
    id                BIGSERIAL PRIMARY KEY,
    instance_id       BIGINT NOT NULL REFERENCES dungeon_instance(id),
    dungeon_id        BIGINT NOT NULL REFERENCES dungeon_template(id),
    user_id           BIGINT NOT NULL REFERENCES player(id),
    favor             INT NOT NULL DEFAULT 0,
    favor_log         JSONB,
    hidden_finds      JSONB NOT NULL DEFAULT '[]'::jsonb,
    triggered_events  JSONB NOT NULL DEFAULT '[]'::jsonb,
    UNIQUE (instance_id, user_id)
);
```

> `favor_log` 实际为 JSONB 字符串列表（文档写 `TEXT[]`）。

### 3.8 枚举

- `DungeonStatus`：`ACTIVE` 进行中 / `COMPLETED` 已通关 / `FAILED` 战败 / `ABANDONED` 已放弃
- `DungeonElementType`：`METAL` 金 / `WOOD` 木 / `WATER` 水 / `FIRE` 火 / `EARTH` 土
- `PoiType`：`GATHER` 采集 / `COMBAT` 战斗 / `SEARCH` 搜索 / `PASSAGE` 通道（配置按字符串解析，枚举未被引用）

## 4. 准入与实例生命周期

1. 「秘境 名」先按名查模板，未找到报「秘境不存在」，未开放报「尚未开放」。
2. 玩家状态 MUST 为空闲（IDLE），否则报「你当前处于 X 状态，无法进入秘境」。
3. 依次校验全部准入条件（见 3.2）；失败时列表页展示原因、进入时直接拒绝。
4. 同一玩家在同一秘境已有 `ACTIVE` 实例时：若未过期则拒绝（文案仍引用已删除的「秘境探索」）；若已过期则标记为 `FAILED` 后允许重新进入。
5. 创建实例：`current_area_key = area_configs[0].key`、`passage_unlocked = false`、`explored_pois = []`、`expires_at = now + timeout_hours`；玩家进入 `DUNGEON` 状态并记录活动目标为实例 ID。
6. 开场文本：首区域名与描述；有灵模式附形象与问候语，无灵模式列出可探索地点并提示「秘灵 内容」。
7. 超时清理：定时任务每 15 分钟扫描 `ACTIVE` 实例，过期实例标记 `FAILED` 并解除玩家 `DUNGEON` 状态（避免部分唯一索引把玩家锁死）。
8. 列表结果缓存 1 分钟，进入秘境时失效。

## 5. LLM 架构

### 5.1 对话隔离

`ConversationId(DUNGEON, userId, instanceId)`——实例结束对话即结束，旧历史不残留；好感度、隐藏发现等状态通过 `dungeon_spirit_state` 持久化。

### 5.2 prompt 工程

`DungeonStateBuilder` 构建系统 prompt，包含：

1. 秘境之灵个性 / 叙事者身份（有灵模式含形象、性格、语气风格；无灵模式为旁白口吻）
2. 当前区域名称与描述
3. 可探索地点列表（标注已探索；未探索且配置了 `clues` 的地点附线索）
4. 隐藏 POI 线索（仅提示尚未发现的隐藏地点，要求「只暗示、不点名」）
5. 通道是否已开启
6. 好感度 + 态度（有灵模式且已创建状态时）
7. 操作规则（先等玩家意图再调用工具；不替玩家做决定；战斗结果由工具返回，只做 3~5 句叙事美化；好感高时可更主动暗示隐藏内容）

### 5.3 工具集

| 工具 | 参数 | 行为 |
|---|---|---|
| `resolveEncounter` | `poiName, approach?` | 按名查当前区 POI → 执行 → 返回结构体（`approach` 当前未参与结算） |
| `advanceToNextArea` | — | 检查通道解锁 → 推进到下一区 / 通关 → 返回新区域信息 |
| `retreatFromDungeon` | — | 结算退出（标记 `ABANDONED`，保留已获奖励） |
| `checkPlayerStatus` | — | 昵称、境界、当前 HP、好感度与态度、灵石余额 |
| `checkCurrentArea` | — | 当前区已探索 / 剩余 POI、通道状态 |
| `adjustFavor` | `change, reason` | 好感度变更（仅好感系统开启时；变更写入 `favor_log`） |
| `giveHint` | — | 消耗 20~30 好感度获取一条未发现隐藏地点的线索 |

### 5.4 战斗与掉落流程

```
玩家描述战斗意图 → LLM 调用 resolveEncounter(poiName)
  → POI 类型 COMBAT
  → 从 monster_pool 按权重抽怪物模板，数量按 min~max 随机（缺省 1）
  → CombatEngine.simulate（DUNGEON 场景，最大 20 回合）— 零延迟
  → 结果结构体: {胜/负, HP损失, 击杀统计}
  → 胜 → 从 loot_pool 抽战利品 → 结构体带掉落
  → 结果返回 LLM → LLM 生成 3~5 句战斗叙事
  → 负 → 队伍被全歼时玩家进入濒死状态
```

战利品与灵石：

- `loot_pool` 非空时，每次 POI 结算执行 **2~3 次**加权抽取；每次数量按 `min_qty ~ max_qty` 随机（缺省 1）。
- 每次 POI 结算额外获得 **10~50 灵石**。
- `SEARCH` 类型：20% 概率遭遇战（且配置了 `monster_pool`），胜利后照常掉宝；无遭遇战时给出搜索文案。
- `GATHER`：直接按 `loot_pool` 掉宝。
- 战斗失败：不掉宝；若队伍被全歼则玩家进入濒死。

## 6. 区域推进与隐藏内容

### 6.1 推进规则

1. 每个区域（MAIN 类型）有 `main_pois` 和 `hidden_pois` / `hidden_areas`。
2. `main_pois` 中**除 PASSAGE 外**全部 resolve 后 → `passage_unlocked = true`。
3. 隐藏内容不影响推进。
4. `advanceToNextArea()` 按 `area_configs` 数组顺序推进到下一区；推进后上一区关闭（不可回，`advanceArea` 会重置 `explored_pois`）。
5. `PASSAGE` 类型 POI 在主线全清后自动激活；主动探索会被拒绝（提示不需主动探索）。
6. 已在最后一个区域时调用推进 → 通关结算（见 7）。

> 隐藏区域当前只是数组中的后续区域；`trigger_after_resolve` 未被消费，因此不构成进入条件。

### 6.2 隐藏内容发现

- 有 `clues` 的隐藏 POI：LLM 在环境描述中自然暗示。
- 无 `clues` 的隐藏 POI：纯靠玩家自主探索发现。
- 隐藏 POI 可通过 `resolveEncounter` 按名探索；发现后记入 `hidden_finds`，之后 prompt 不再暗示该地点线索。
- 好感度高时秘境之灵会主动提示（有 `affection_system` 时）。
- 隐藏区域：文档设计为由 `trigger_after_resolve` 指定的 POI 触发后解锁入口——**未实现**。

### 6.3 好感度系统

- 受 `affection_system` 开关控制；好感初始 0，可正可负（无上下限截断）。
- 由 LLM 通过 `adjustFavor` 工具调整，变更记录 `favor_log`。
- 档位与态度：

| favor | 态度 | 文档设计效果（实现状态） |
|---|---|---|
| ≤ -50 | 敌视 | 触发陷阱概率↑、怪物增强、隐藏奖励不可达（**均未实现**） |
| -49~0 | 冷淡 | 公事公办 |
| 1~50 | 平和 | 偶尔给出暗示 |
| 51~100 | 欣赏 | 提示隐藏路径 |
| 101~200 | 亲近 | 额外掉落、特殊剧情（**未实现**） |

- 玩家可通过 `giveHint` 消耗好感度换取线索：需要好感 ≥20，消耗 20~30，随机揭示当前区域一个未发现的隐藏 POI 的一条线索（无 `clues` 时返回通用提示）；无可揭示内容时拒绝且不扣好感。
- 好感的实际生效点：`giveHint` 门槛、prompt 中「好感高可更主动暗示」、`checkPlayerStatus` 展示。

## 7. 奖励与进度

通关（在最后区域调用推进）时：

- 实例标记 `COMPLETED`，玩家解除 `DUNGEON` 状态。
- 进度记录（按用户 + 秘境唯一）：
  - `first_clear`：首次通关置 true。
  - `best_area`：写入通关时所在区域 key（撤退不更新）。
  - `daily_limit`：按玩家等级计算——≤9 → 1，≤19 → 2，≤29 → 3，≥30 → 4；记录不存在时用当前等级计算，存在时沿用存储值。
  - `interaction_count`：每次 `秘灵 内容` 对话原子累加。
- 奖励：当 `last_reward_date` 非今日或 `reward_count < daily_limit` 时，发放 **500~2000 灵石** 并 `reward_count +1`；达到上限时提示「今日通关奖励次数已达上限，未获得灵石」。奖励判定在每日限额内，避免限额形同虚设。

## 8. 指令交互流程（源文档示例）

```
=== 进入 ===
玩家: 秘境
系统: ── 可进入秘境 ──
      1. 紫府秘境  金  境界30-75  未通关
      2. 灵虚洞天  木  境界40-70  核心·1/3
      输入「秘境 秘境名」进入
玩家: 秘境 紫府秘境
秘灵: 你踏入紫府秘境的那一瞬间，天地仿佛颠倒了一下...

=== 探索 ===
玩家: 那边的石碑很显眼，我过去看看
秘灵: → resolveEncounter → 战斗叙事
      你离碑还有五步，脚下一沉！灵纹亮起...

=== 隐藏发现 ===
玩家: 挨个检查一下墙角有没有暗门
秘灵: → resolveEncounter("坍塌丹室") → 发现隐藏丹房
      你在回廊下发现一块地砖声音不对，撬开一看...

=== 推进 ===
玩家: 差不多了，继续深入
秘灵: → advanceToNextArea() → 进入内围
      你沿紫竹林中小径下行数百步，眼前豁然开朗...

=== 撤退 ===
玩家: 此行收获够了，带我出去吧
秘灵: → retreatFromDungeon()
      你且战且退，灵光一闪，已回到入口处。
      【灵石 +315】【黑铁令牌 +1】

=== 秘境中看状态 ===
玩家: 秘境
系统: 外围 | 探索 3/4 | 好感 15 | 用时 23min
      隐藏发现: 坍塌丹室
```

> 实际「秘境」进度输出为 `区域 | 探索 x/y | 用时 Nmin`，**不含好感与隐藏发现**（`DungeonStateBuilder.buildStatusOverview` 含这两项但无调用方）。且分母为主线 POI 总数（含 PASSAGE）、分子为已探索 POI 数，PASSAGE 不可探索，因此含通道的区域无法显示满进度（示例中的 `3/4` 在含 PASSAGE 区域实际会呈现为 `N-1/N`）。

## 9. 开发步骤（源文档，附实现现状）

1. `domain/dungeon/` — 重构 entity，删旧 VO，加 `DungeonAccessConditionType` 枚举 → 已完成（枚举存在但未被引用）。
2. `db/migration/` — Flyway 迁移（删旧表、建新表、种子数据）→ 已完成（V1.0.34/36/37/52 + 种子 V1.0.38.1 紫府秘境）。
3. `infrastructure/` — `DungeonSpiritStateMapper` + `DungeonSpiritStateRepository`，删旧 mapper/repository → 已完成。
4. `service/` — `DungeonAccessChecker`、`DungeonChatService`、`DungeonTools`、`DungeonStateBuilder`；改造 `DungeonQueryService`；精简 `DungeonService` → 已完成；旧组件 `DungeonCombatHelper` / `DungeonLootHelper` / `DungeonProgressHelper` 仍在使用（清理未完成）。
5. `config/` — `dungeonChatClient` bean → 已完成。
6. `handle/` — 重写 `DungeonListener` + `DungeonCommandHandler` → 已完成。
7. 清理：删 `DungeonCombatHelper` / `DungeonLootHelper` / `DungeonProgressHelper` 等旧组件 → **未完成**，三者仍是探索/推进链路的组成部分。

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **准入条件结构**：实际为顶层数组 `[{type, node_ids?, min?, max?, sect_id?, template_id?, dungeon_id?, code?}]`，无 `conditions` 包装与 `display_tag`；不可进入原因直接以文案展示在列表页。
- **实例唯一约束**：实际为部分唯一索引 `UNIQUE (leader_id, dungeon_id) WHERE status = 'ACTIVE'`，不是 `UNIQUE (leader_id, dungeon_id, status)`。
- **`favor_log` 类型**：实际为 JSONB 字符串列表（非 `TEXT[]`）。
- **`best_area` 语义**：仅通关时写入当前区域 key，撤退不更新；该字段目前无读取方，玩家不可见。
- **列表缓存**：秘境列表缓存 1 分钟（Caffeine `expireAfterWrite`），进入秘境时失效。
- **旧组件未清理**：`DungeonCombatHelper` / `DungeonLootHelper` / `DungeonProgressHelper` 仍是探索与推进链路的组成部分，清理保留为后续重构。
- **`approach` 参数**：`resolveEncounter` 的探索方式参数仅作 LLM 叙事输入，不参与结算。

### B. 保留代码设计

- **好感度保持轻量**：只做态度分档 + 提示主动性 + `giveHint` 线索货币，不实现「敌视触发陷阱 / 怪物增强 / 隐藏奖励不可达」等不可见负面修正，避免玩家难以归因的挫败；额外掉落 / 特殊剧情属后续内容扩展，不必以机制硬绑定。
- **战斗回合上限 20**：秘境遭遇战定位短促、即时结算，与天劫决战的 40 回合区分。
- **掉落与灵石细节**：每次 POI 结算执行 2~3 次加权抽取并额外给 10~50 灵石，`SEARCH` 20% 概率遭遇战（配置了怪物池时），保证每个 POI 都有正收益、搜索带变数。
- **通关奖励 500~2000 灵石 + 按等级每日上限**（≤9→1、≤19→2、≤29→3、≥30→4）：奖励判定在限额内发放，防止刷本通胀；等级越高上限越高，给成长以回报。
- **超时清理**：定时任务每 15 分钟关闭过期实例并解除玩家状态，进入时也顺手关闭过期实例，避免部分唯一索引把玩家锁死在秘境外。

### C. 按设计修正（已修）

- **「秘境」进度展示好感与隐藏发现**（已修）：`getStatusInternal` 接回 `DungeonStateBuilder.buildStatusOverview`，进度页输出好感与隐藏发现。

### D. 未实现（待办）

- **多人组队**：设计意图为按 `max_team_size` 组队进入；现状实例只有单人 `leader_id`，队伍 = 玩家 + 出战灵兽，`max_team_size` 仅展示（当前数据为 1），随数据启用。

> 已实现（本轮）：SECT 准入按有效宗门成员校验（指定宗门或任意宗门）；隐藏区域按 `trigger_after_resolve` 指定的 POI 探索记录解锁（跨区域保留探索记录，未满足时跳过）。

### E. 缺陷修复

- **探索计数错误**（已修）：分母改为非 `PASSAGE` 主线 POI 总数，分子只计已探索主线 POI，隐藏发现不再影响进度。
- **文案残留与不合语错误**（已修）：`DUNGEON_ALREADY_IN` 改为「你已在秘境【%s】中」；新增 `DUNGEON_SECT_RESTRICTED` 等结构化错误码，替换「你当前处于 %s 状态」模板复用。
