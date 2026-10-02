# 用户系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 角色实体（player 表）

### 1.1 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | 角色 ID（PK） |
| `nickname` | VARCHAR(64) | 道号（UNIQUE） |
| `level` | INT | 等级（默认 1，CHECK ≥ 1） |
| `exp` | BIGINT | 修为（默认 0，CHECK ≥ 0） |
| `spirit_stones` | BIGINT | 灵石（默认 0，CHECK ≥ 0；UPDATE 时不回写，由 `SpiritStoneService` 原子 SQL 控制） |
| `stat_str` | INT | 力道（默认 5，CHECK ≥ 0） |
| `stat_con` | INT | 根骨（默认 5，CHECK ≥ 0） |
| `stat_agi` | INT | 身法（默认 5，CHECK ≥ 0） |
| `stat_wis` | INT | 悟性（默认 5，CHECK ≥ 0） |
| `hp_current` | INT | 当前气血（默认 200，CHECK ≥ 0） |
| `status` | VARCHAR(32) | 当前状态（默认 IDLE，CHECK ∈ IDLE/TRAINING/TRAVELING/BOUNTY/DYING/DUNGEON） |
| `location_id` | BIGINT | 所在地图 ID（默认 1，FK → map_node） |
| `activity_type` | VARCHAR(16) | 通用活动类型（NULL 或 TRAVEL/TRAINING/BOUNTY/DUNGEON/BOUNTY_SIDE） |
| `activity_start_time` | TIMESTAMP | 活动开始时间 |
| `activity_target_id` | BIGINT | 活动目标 ID（TRAVEL/TRAINING → map_id；BOUNTY → bounty_record_id） |
| `breakthrough_fail_count` | INT | 突破失败次数（默认 0，CHECK ≥ 0） |
| `last_hp_recovery_time` | TIMESTAMP | 上次气血自然恢复时间（见 7.2） |
| `dying_start_time` | TIMESTAMP | 濒死开始时间（见 7.3） |
| `last_settlement_minute` | BIGINT | 历练中途结算的已处理分钟数（默认 0） |
| `last_fortune_date` | DATE | 上次运势生成日期，用于每日自动刷新 |
| `gm` | BOOLEAN | GM 标记（默认 FALSE） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳（默认 CURRENT_TIMESTAMP） |

### 1.2 索引与约束

- `uq_nickname` UNIQUE(nickname) — 道号唯一
- `idx_user_status` (status)
- `idx_user_location` (location_id)
- `idx_user_activity_type` (activity_type)
- `idx_user_level_exp` (level DESC, exp DESC) — 等级榜查询索引
- 外键 `fk_user_location` → map_node(id)
- 状态 CHECK 与 `UserStatus` 枚举 code 完全一致（含 DUNGEON）；活动类型 CHECK 与 `ActivityType` 枚举 code 一致（含 BOUNTY_SIDE）

---

## 2. 跨平台授权（user_auth）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | FK → player(id) ON DELETE CASCADE |
| `platform` | VARCHAR(32) | 平台类型，CHECK 目前仅 `'QQ'` |
| `platform_open_id` | VARCHAR(128) | 平台唯一 ID |
| `create_time` | TIMESTAMP | 绑定发生时间（默认 CURRENT_TIMESTAMP） |

**约束**：`uq_platform_id` UNIQUE(platform, platform_open_id)，同一平台账号只能绑定一个角色；`user_id` 无唯一约束，一个角色可绑定多个平台。

实体行为：`UserAuth.matches(platform, openId)` 判定绑定归属；`UserAuth.init(platform, openId, userId)` 创建绑定。

---

## 3. 等级与修为

### 3.1 升级公式

```
升级所需修为 = 100 × level²
修为存储上限 = 升级所需修为 × 5
```

### 3.2 修为存储（`Player.addExp`）

- 当前档位已存修为 = `exp − 100 × (level−1)²`（level ≤ 1 时为 0）
- 可存入空间 = `存储上限 − 当前档位已存修为`
- 实际入账 = `min(expToAdd, 可存入空间)`，超出部分静默丢弃
- 击杀修为入账同样受存储上限截断

### 3.3 突破效果（按代码现状）

**小境界 RNG 突破成功**：

- `level += 1`
- 扣除升级所需修为（`exp -= expNeeded`）
- 失败计数归零
- 气血回满（`hpCurrent = calculateMaxHp()`）
- 清除所有被护道关系与全部 `BREAKTHROUGH` 类 Buff
- 跨大境界成功时额外应用大境界奖励（见第 5 节）

**小境界 RNG 突破失败**：

- 扣除全部所需突破修为（`exp = max(0, exp − expNeeded)`）
- 失败计数 += 1
- 清除所有被护道关系与全部 `BREAKTHROUGH` 类 Buff
- 气血保持原值

**大境界 / 渡劫期突破**：改为雷劫战斗，详见 `openspec/specs/breakthrough/design.md`。

---

## 4. 四维属性

### 4.1 基础值与实际值

```
实际属性（effective）= 属性字段 + 4 + level
属性字段初始值为 5（DB 默认），丹药（Stat 类）直接累加到对应字段
等级 1 无任何加成时：实际属性 = 5 + 4 + 1 = 10（文档示例为 5）
等级 50 无任何加成时：实际属性 = 5 + 4 + 50 = 59（文档示例为 54）
每升一级：实际属性 +1
```

装备的 `str/con/agi/wis` 加成在「状态」展示中叠加显示（`有效四维 + 装备四维`），但不回写属性字段。

### 4.2 属性公式

| 属性 | 影响 |
|------|------|
| 力道（STR） | 法器普攻伤害 `attack = 有效力道 × 2 + 法器攻击`（战斗内另加攻击 Buff；状态展示为 `(有效力道+装备力道) × 2 + 全装备攻击`） |
| 根骨（CON） | 气血上限 `100 + 有效根骨 × 20`；防御 `defense = 有效根骨 + 防御 Buff`（状态展示为 `有效根骨 + 全装备防御`） |
| 身法（AGI） | 速度 `speed = 有效身法 × 2 + 10 + 速度 Buff`；历练效率 `1 + min(agi × 0.01, 2.0)` |
| 悟性（WIS） | 法决伤害（公式变量 `wis`）；历练基础修为 `max(地图等级 × 5, √有效悟性 × 12)/分钟` |

### 4.3 差异化来源

玩家间的属性差异**不来自加点**，而来自：

- **装备**：每件装备有不同的 `base_str/con/agi/wis` 加成 + 随机词条（当前战斗中仅法器（武器）的攻击与攻速参与计算，护甲/饰品仅影响状态展示）
- **法器类型**：不同法器对怪物有不同克制倍率
- **法决**：悟性高 → 法决伤害高

---

## 5. 突破系统

### 5.1 成功率公式（`Player.calculateBreakthroughSuccessRate`）

```
基础概率 rawBase   = 100 / (1 + (level / 65)^4)
单次失败补偿 rawPityGain = 5 + 20 / (1 + (level / 50)^2)
基础+补偿 = clamp(rawBase + 失败次数 × rawPityGain, 0, 100)，保留两位小数
最终成功率 = clamp(基础+补偿 + 护道加成 + 突破丹药加成, 0, 100)
```

- 突破丹药加成：来自 `player_buff` 表中未过期的 `buff_type='breakthrough'` 记录，取总和
- 突破后（无论成败）清除该玩家所有 `breakthrough` 类 Buff
- `attemptBreakthrough()` **不检查玩家当前状态**，任何状态下均可发起突破

### 5.2 突破分流

```
修为不足 → 直接拒绝（提示所需修为与当前修为，无任何扣减）
小境界（同一大境界内）→ RNG 骰子：Math.random() × 100 < 最终成功率
大境界（跨大境界）或目标境界为渡劫期 → 扣除修为 → 随机雷劫类型 → 生成雷劫 Boss → 战斗 40 回合 → 胜=突破 / 败=失败
```

### 5.3 护道加成

```
单人加成 = max(0, 5% + (护道者等级 − 被护道者等级) × 1%)
总加成上限 = 20%（仅同地点护道者计入）
```

---

## 6. 护道系统（dao_protection）

| 字段 | 说明 |
|------|------|
| `id` | 护道关系 ID |
| `protector_id` | 护道者（FK → player，CASCADE） |
| `protege_id` | 被护道者（FK → player，CASCADE） |
| `create_time` / `update_time` | 时间戳 |
| UNIQUE(protector_id, protege_id) | 防重复 |
| CHECK(protector_id != protege_id) | 不可自我护道 |

### 6.1 约束

- 护道者等级 ≥ 被护道者等级（低于时拒绝并提示双方境界）
- 护道者最多同时为 3 人护道（`MAX_PROTECTOR_COUNT = 3`）
- 被护道者突破后自动清除所有护道关系（`clearProtegeRelations`）
- 查询缓存 `dao_protection`，建立/解除/清除时失效

### 6.2 指令

- `护道 「道号」` — 建立
- `护道解除 「道号」` — 解除
- `护道查询` — 查看为谁护道（x/3）与谁在为自己护道（含是否同地点与总加成）

---

## 7. 用户状态（UserStatus）

| 状态 | 说明 |
|------|------|
| IDLE | 空闲 |
| TRAINING | 历练中 |
| TRAVELING | 旅途中 |
| BOUNTY | 悬赏任务中 |
| DUNGEON | 秘境中 |
| DYING | 濒死（气血=1，无法行动） |

### 7.1 濒死（DYING）

触发条件：战斗中气血归零后调用 `setDying()`（气血固定设为 1，记录 `dying_start_time`）。

效果：

- 无法进行任何行动（历练/旅行/悬赏/秘境）
- 可使用丹药恢复气血，`reviveFromDying()` 后状态回到 IDLE
- 或等待 30 分钟自动恢复（见 7.3）

### 7.2 气血自然恢复（HpRecoveryHandler，@Order(3)）

```
IDLE 状态：每 5 分钟恢复 max(1, 最大气血 / 100) 点，直到满血
其他状态：不恢复
```

**实现机制**（懒加载）：

- 通过 `last_hp_recovery_time` 列记录上次恢复时间戳
- 计算经过的 5 分钟刻度数，逐刻度执行 `Player.naturalHpRecovery()`
- 首次加载时 `last_hp_recovery_time` 为空 → 写入当前时间为初始值，不触发恢复
- 时间戳按刻度对齐推进（`lastRecovery + ticks × 5`），避免累计误差
- 恢复满血时产出 `HP_RECOVERED` 事件到 `game_event`，由通知追加器随回复送达

```
HpRecoveryHandler.tryResolve(user)
  ├── status != IDLE → skip
  ├── hpCurrent >= maxHp → skip
  ├── lastHpRecoveryTime == null → 写入当前时间，return true
  ├── ticks = elapsedMinutes / 5
  ├── ticks <= 0 → skip
  ├── for tick in ticks: user.naturalHpRecovery()
  ├── 更新 lastHpRecoveryTime = lastRecovery + ticks × 5
  └── HP 满 → 产出 HP_RECOVERED 事件
```

### 7.3 濒死自动恢复（DyingRecoveryHandler，@Order(2)）

玩家处于 DYING 状态超过 30 分钟后自动恢复：

- 气血恢复到 `max(1, 最大气血 / 5)`（20%）
- 状态回到 IDLE
- 清空活动字段（`activity_type/start_time/target_id`）
- 清除 `dying_start_time`
- 产出 `DYING_RECOVERED` 事件到 `game_event`（叙事含恢复后气血值）

```
DyingRecoveryHandler.tryResolve(user)
  ├── status != DYING → skip
  ├── dyingStartTime == null → 写入当前时间作为起始点，return true
  ├── elapsedMinutes < 30 → skip
  ├── reviveFromDying(max(1, maxHp / 5))
  └── 产出 DYING_RECOVERED 事件
```

### 7.4 旅行自动结算（TravelCompletionHandler，@Order(1)）

旅行到期后在下一次用户交互时自动结算（懒加载），详见 `docs/地图旅行历练.md`：

```
TravelCompletionHandler.tryResolve(user)
  ├── status != TRAVELING 或 activityType != TRAVEL 或 targetId == null → skip
  ├── startTime == null → 清空活动
  ├── 当前/目标地图不存在 → skip；无路径 → 清空活动（卡死保护）
  ├── 到达时间 = startTime + travelTime(current → destination)
  ├── now < 到达时间 → skip
  ├── locationId = 目标地图
  ├── clearActivity()
  └── travelCompleter.completeTravel(...)
```

### 7.5 其他懒结算处理器

| 处理器 | @Order | 行为 |
|--------|--------|------|
| `BuffExpiryHandler` | 4 | 单条 DELETE 清理该玩家所有过期 `player_buff`（返回值 false，不触发整行保存） |
| `TrainingSettlementHandler` | 5 | 历练中每满 60 分钟自动中途结算一段（击杀修为入账、产出 `TRAINING_EVENT` 事件、更新 `last_settlement_minute`） |
| `DailyFortuneHandler` | 6 | 跨天首次加载时生成当日运势并写入 `FORTUNE` 事件 |

---

## 8. 用户状态服务（UserStateService）

### 8.1 职责

所有服务加载用户时统一走 `UserStateService`，确保状态一致（旅行是否到期、气血是否恢复、濒死是否超时、历练是否需中途结算、Buff 是否过期、运势是否需刷新）。

**公开方法（现状）：**

- `loadUser(Long userId)` → Player：行锁加载（`SELECT ... FOR UPDATE`）并执行 `resolveState()` 结算
- `loadUserReadOnly(Long userId)` → Player：只读加载，不结算（仅查询场景）
- `loadUsersByIds(List<Long>)` → Map：批量加载并逐个结算
- `loadUsersByIdsReadOnly(List<Long>)` → Map：批量只读加载
- `loadUserByNickname(String)` → Player 或 null：不结算
- `save(Player)` → Player：全字段保存
- `clearActivity(Long)` / `saveActivity(Player)` / `saveHpStatus(Player)` / `saveTrainingEndState(Player)`：定向字段保存，避免覆盖灵石等并发数据

**内部结算：**

- `resolveState(user)`：稳定状态快速路径（IDLE + 气血满 + 无活动）直接返回；否则按 @Order 依次执行 `StateHandler`，任一处理器返回 true 则整行保存
- 状态处理器通过 Spring 注入 `List<StateHandler>`，新增自动结算只需追加实现类，不改 `UserStateService`

**常量：**

- 气血恢复间隔：5 分钟
- 濒死超时：30 分钟
- 历练中途结算间隔：60 分钟
- 追踪字段：`last_hp_recovery_time`、`dying_start_time`、`last_settlement_minute`

### 8.2 历史迁移记录（文档原文保留）

文档记录当时将 13 个服务、27 处调用点从 `userRepository.findById()` 迁移至 `userStateService.loadUser(userId)`：

| 服务 | 变更 |
|------|------|
| BountyService | userRepository → userStateService |
| CharacterStatusService | userRepository → userStateService |
| CultivationService | userRepository → userStateService（保留 UserRepository 用于 findByNickname 等） |
| EquipmentService | userRepository → userStateService |
| FudiHelper | userRepository → userStateService |
| InventoryService | userRepository → userStateService |
| MapService | userRepository → userStateService |
| PillConsumptionService | userRepository → userStateService |
| SkillService | userRepository → userStateService |
| StackableItemService | userRepository → userStateService |
| TrainingService | userRepository → userStateService |
| TravelService | userRepository → userStateService |
| — | UserStateService（新增） |

### 8.3 后续扩展点

新增自动状态解析时，实现 `StateHandler` 并标注 `@Order` 即可（文档原设想的 `tryCompleteTravel/tryDyingRecovery/tryHpRecovery/tryExpireBuffs/tryResolveCombat/tryRegenMana` 已改为处理器列表形式；`tryResolveCombat`、`tryRegenMana` 仍未实现）。

---

## 9. 指令与可观察行为

### 9.1 注册（`我要修仙 「道号」`）

- 校验平台账号是否已绑定 → 已绑定返回「阁下已在仙路中~」
- 校验道号是否重复 → 重复返回「此道号已被他人使用，请另择佳名~」
- 创建角色：初始灵石 100、随机 MBTI 福地、入门法器「桃木剑」（不自动穿戴）
- 写入 3 条新手引导事件（背包/状态、历练/前往、突破/悬赏/帮助），随注册回复送达
- 成功回复：「欢迎踏入仙途！您的道号为：X\n输入「状态」查看您的角色信息」

### 9.2 改号（`改号 「新道号」`）

- 重名抛 `NICKNAME_TAKEN`；角色不存在抛 `CHARACTER_NOT_FOUND`
- 成功回复：「道号已改为【X】」

### 9.3 状态（`状态`）

展示内容（`StatusCommandHandler` + `CharacterStatusService`）：

- 道号、所在地
- 状态：赶路中显示「所在地 → 目的地」、旅途进度（已用/总时长，剩余或即将到达）；其他状态显示状态名
- 境界：`境界 · 层名` + 修为百分比；修为 ≥ 100% 时显示「修为圆满，可突破」
- 气血：当前/上限（百分比）
- 基础属性（力道/根骨/身法/悟性，含装备加成括号展示）
- 战斗属性（攻击/防御）
- 修为圆满且将跨大境界/渡劫期时显示「雷劫将至」预报（见 breakthrough 设计）
- 否则显示突破成功率与失败补偿次数
- 护道信息（为谁护道 x/3、谁在护道、总加成）
- 灵石
- 已穿戴装备列表

### 9.4 排行榜（`排行榜` / `排行榜 灵石`）

- `排行榜`：`【修为排行榜】`，至多 10 条，显示名次（前三名奖牌）与境界
- `排行榜 灵石`：`【灵石排行榜】`，至多 10 条，显示名次与灵石
- 无数据时返回「（暂无数据）」；结果缓存 `leaderboard`

### 9.5 查看（`查看 「目标」`）

解析顺序：装备 → 怪物 → 物品 → 玩家档案。

- 玩家档案：道号、境界、所在地、状态（可查看自己）
- 装备/物品「未找到」类错误被吞掉继续尝试下一类；「多重匹配」直接提示
- 全部未命中返回「未找到 [X]，可输入装备名/编号、怪物名或物品名」

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名**：`xt_user`/`xt_user_auth`/`xt_dao_protection`/`xt_player_buff`/`xt_game_event` 实际为 `player`/`user_auth`/`dao_protection`/`player_buff`/`game_event`。
- **授权表**：`bind_time` → `create_time`，`platform` 带 CHECK('QQ')；约束仍为 UNIQUE(platform, platform_open_id)。
- **索引**：无 `idx_xt_user_spirit_stones`；实际为 `idx_user_status`、`idx_user_location`、`idx_user_activity_type`、`idx_user_level_exp (level DESC, exp DESC)`。
- **新增字段/枚举**：`last_settlement_minute`、`last_fortune_date`、`gm`；`ActivityType` 增 `DUNGEON`/`BOUNTY_SIDE`，`UserStatus` 增 `DUNGEON`。
- **突破成功率数值**：逻辑斯蒂基础概率 `100/(1+(level/65)^4)` + 失败补偿 `5 + 20/(1+(level/50)^2)`，叠加护道/丹药后 clamp [0,100]（保留两位小数）。
- **突破分流**：跨大境界与渡劫期改为雷劫战斗（RNG 仅用于同大境界小境界），细节见 `breakthrough` 能力。
- **状态解析实现**：使用数据库列 `last_hp_recovery_time`/`dying_start_time`，由 `StateHandler` 列表（旅行→濒死→气血→过期 Buff→历练结算→运势）结算；`UserStateService` 另提供只读/批量加载与定向保存。

### B. 保留代码设计

- **四维起点恒定 +5**：四维字段默认 5，实际值 = 字段 + 4 + level，全等级比文档公式（4 + level）高 5 点且成长斜率一致；效果只是新号起步更从容，按文档回改等于对全体角色削 5 点四维并重平衡怪物，收益不足。
- **大境界奖励 +20% 与灵石**：跨大境界给有效四维 +20% 与 `(rank+1) × 2000` 灵石，里程碑有质变级正反馈，比仅靠等级自然 +1 更能承载「跨大境界」的分量。
- **护道加成下限 0**：`max(0, 5% + 等级差 × 1%)` 保证被护道者后来居上时护道不会变成负收益，护道始终是纯增益。
- **懒结算新增项**：历练每 60 分钟中途结算、每日运势跨天自动刷新；挂机玩家定期看到收益与事件，减少「等结算」的空转。
- **注册赠礼**：100 灵石 + 桃木剑 + 随机 MBTI 福地 + 3 条引导事件，降低新手第一分钟的理解成本与挫败。

### C. 按设计修正（待修）

无。

### D. 未实现（待办）

无。

> 已实现（本轮）：装备四维/防御参与战斗（与 `combat` 同源）：`EquipmentStats` 聚合已穿戴装备的四维与总攻/总防，注入 `PlayerCombatant`；状态页与档案改用同一聚合，口径统一。

### E. 缺陷修复

- **排行榜排序方向反了**（已修）：`findTopByLevel`/`findTopBySpiritStones` 改为 `level DESC, exp DESC` 与 `spirit_stones DESC`，榜单展示最高者。
- **新号开局残血**（已修）：创建角色时显式设置 1 级默认四维并回满 `hpCurrent = calculateMaxHp()`，不再出生即 2/3 气血。
