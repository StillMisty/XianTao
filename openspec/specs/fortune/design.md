# 运势系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

每日运势系统，基于玩家 ID 与日期确定性生成三维运势值（0~100），影响历练、战斗、掉落、悬赏、商店砍价等核心玩法。运势天注定，不可逆转。实现入口为 `FortuneService`，玩家入口为「今日运势」指令与每日自动事件。

## 2. 运势维度（以代码为准）

| 维度（展示名） | 代码字段 | 影响范围 | 倍率 / 偏移公式 | 实际幅度 |
|---|---|---|---|---|
| 财运 | `wealth` | 掉落物数量、悬赏灵石奖励、商店砍价 | `0.85 + wealth/333.0` | 0.85× ~ 1.15×（约 ±15%） |
| 情缘 | `fate` | 子事件触发率（旅行 / 历练 / 悬赏） | `0.7 + fate/166.0` | 0.70× ~ 1.30×（±30%） |
| 机缘 | `luck` | 历练修为倍率、战斗怪物等级偏移 | 修为 `0.8 + luck/250.0`；等级偏移 `clamp((50 - luck)/30, -3, 3)` | 0.80× ~ 1.20×；偏移实际 -1 ~ +1 |

> **命名映射（重要）**：源文档中的三维为「财运 / 机缘 / 气运」，代码展示为「财运 / 情缘 / 机缘」。
> 文档的「机缘」（子事件触发）对应代码 `fate`（展示名「情缘」）；文档的「气运」（历练修为 / 战斗怪物）对应代码 `luck`（展示名「机缘」）。本文以下均以代码为准。

## 3. 运势等级与点评

五档判定（`determineLevel`，按顺序短路）：

| 等级 | code | 判定条件 |
|---|---|---|
| 大吉 | `GREAT_FORTUNE` | 三项均 ≥ 80 |
| 大凶 | `GREAT_MISFORTUNE` | 三项均 < 40 |
| 吉 | `GOOD_FORTUNE` | 最高值 ≥ 80 且最低值 ≥ 45 |
| 凶 | `BAD_FORTUNE` | 最低值 < 25 |
| 平 | `NEUTRAL` | 其余 |

点评生成（`generateComment`）：先收集好/坏词——≥80 记好词（财运→「财源广进」、情缘→「机缘天降」、机缘→「气运加身」），<30 记坏词（「财运不佳」「机缘未至」「气运低迷」），再按等级拼接：

- 大吉：「万事顺遂，宜大胆行事」
- 吉：好词以「，」拼接；无好词则「运势尚可」
- 凶：坏词 + 「，谨言慎行」；无坏词则「诸事小心」
- 大凶：「诸事不宜，宜闭关静修」
- 平：无好词「吉凶参半，随缘即可」；≥2 好词「彼消此长，贵在坚持」；1 好词「（好词）但亦有隐忧」

## 4. 数据流

1. 每次命令边界 `UserStateService.settle` 触发状态结算（`StateHandler` 列表）；`DailyFortuneHandler`（`@Order(6)`）检查 `player.last_fortune_date` 是否为今日。
2. 若尚未生成：写入 `player.last_fortune_date = today`，并创建 `GameEventCategory.FORTUNE` 事件（narrative 模板 `{{fortuneText}}` = 运势展示文本）。
3. 该事件由 `NotificationAppender` 在玩家下一次任意指令回复时追加送达（系统永不主动推送）。
4. 玩家可随时用「今日运势」主动查询。
5. 运势值不落库；`FortuneService.calculate` 标注 `@Cacheable(cacheNames="fortunes")`，key 为 `userId + '-' + today`，Caffeine `expireAfterWrite` 24 小时、上限 200 条。

**注意**：状态结算存在快速路径——`Player.isStableState()`（IDLE + 无活动 + 满血）时跳过全部 handler，因此稳定状态下的加载不会生成每日运势事件；玩家可通过主动查询获取。

## 5. 确定性生成算法

```
input   = userId + "-" + LocalDate.now()
digest  = MD5(input)                          // UTF-8
wealth  = abs(int32(digest[0..3]))  % 101
fate    = abs(int32(digest[4..7]))  % 101
luck    = abs(int32(digest[8..11])) % 101
```

无 MD5 实现时降级为 `hashCode` 系列（`h`、`h*31`、`h*127`）。同一玩家同一日期结果恒定，无需存储。

## 6. 展示格式

三维各一行：`维度名 + 10 格进度条 + 数值`，进度条按 `round(value / 10)` 填充 `█`、其余 `░`；末行 `「等级」 点评`（等级加粗）。示例：

```
财运  ██████░░░░ 58
情缘  ████░░░░░░ 42
机缘  ███████░░░ 73
「平」 彼消此长，贵在坚持
```

## 7. 玩法接入点

| 接入点 | 代码位置 | 用法 |
|---|---|---|
| 历练修为 | `TrainingService` | `baseExp × getLuckMultiplier(luck)` |
| 战斗怪物等级 | `CombatEventHandler.buildMonsterTeam` | `monsterLevel = baseLevel + rand(-2,3) + getMonsterLevelOffset(luck)`，最低 1 |
| 怪物掉落 | `DropProcessor` | 物品数量 `max(1, round(baseQty × max(1.0, wealthMultiplier)))`；掉率判定不参与 |
| 悬赏灵石 | `BountyCombatService` | 最终灵石 `× wealthMultiplier` |
| 商店砍价 | `ShopService.haggleItem` | 成功率 `× wealthMultiplier`（clamp 0.05~0.85）；让价比例 `× wealthMultiplier`（clamp 0.01~0.20） |
| 子事件触发 | `SubEventSelector` | `adjustedChance = clamp(triggerChance × fateMultiplier, 0, 1)` |
| 历练事件滚动 | `TrainingSettler` | 每分钟触发概率 `min(1, perRollChance × fateMultiplier)` |
| 上下文注入 | `EventContext.withFortune / withMapAndFortune` | 完整 `FortuneVO` 注入 `EventContextKeys.FORTUNE`，事件配置与效果处理器可直接读取，无需改动调用链 |

## 8. 设计考量（源文档）

- **确定性**：以玩家 ID + 日期为种子做 hash，无随机性，无需存储
- **非对称波动**：机缘波动范围最大（奇遇本就难以预测），财运气运相对保守
- **凶日也有价值**：凶日更适合高回报的极端事件，避免纯粹 debuff 体验

## 9. 扩展预留（源文档）

- 隐藏事件触发条件可增加运势阈值
- 事件 narratives 可引用运势值做动态分词

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **维度命名与映射**：代码展示「财运 / 情缘 / 机缘」；文档「机缘（子事件）」= 代码 `fate`（情缘）、「气运（历练/战斗）」= 代码 `luck`（机缘），§2 已标注映射。
- **等级阈值与点评规则**：为代码新增细节（文档只给五档名称与示例点评）；计算结果入 `fortunes` 缓存 24 小时，不落库。

### B. 保留代码设计

- **财运幅度**：`0.85 + wealth/333` → 0.85×~1.15×（±15%），取文档「±15%~20%」的保守端，与「财运气运相对保守」的设计考量一致，避免经济被运势过度放大。
- **财运对掉落**：只影响数量且只增不减（`max(1.0, wealthMultiplier)`），掉率由怪物掉落表独立判定——低财运不剥夺掉落，运势不制造负面剥夺。
- **财运影响砍价**：成功率与让价幅度均乘财运倍率（各自 clamp），让财运在商店经济中有存在感。
- **每日事件生成时机**：稳定状态（空闲满血）走快速路径跳过 handler，不生成事件；事件送达本就依赖玩家交互，稳定玩家可随时「今日运势」查询，首次非稳定状态加载时自动补发，避免为无状态变化的加载写无用事件。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

无

### E. 缺陷修复

- **怪物等级偏移**（已修）：保留设计意图的 ±3 上限，除数由 30 改为 10，极值可达 ±3，clamp 不再形同虚设（机缘对战斗难度的影响变得可感）。
