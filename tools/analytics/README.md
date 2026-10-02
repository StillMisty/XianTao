# 数据采集与分析方案（Analytics）

> 目的：让「下一步做什么」由数据决定。本文档定义整个系统的采集范围、口径、判据与决策回路。
> 配套：`report.py`（报告生成）、`openspec/adr/ADR-0006-analytics-telemetry.md`（决策记录）。

## 1. 原则

1. **运行时零依赖**：分析数据不参与任何游戏逻辑，表可随时清空重建；采集失败只丢数据、不影响玩法。
2. **异步批写**：业务线程只入队（微秒级），独立线程每 2 秒或积满 200 条落库；队列满丢弃并计数。
3. **口径与代码一致**：报告中的目标值引用 `ADR-0005`（无装备基线 ~6 个月到大乘圆满）等设计基准，避免自说自话。
4. **最小必要 + JSONB 扩展**：`analytics_event(kind, subject, value, payload)` 四元组覆盖全部；新增字段不改表。
5. **不依赖 LLM 文本**：所有指标来自结构化事件与快照，不解析叙事文案。

## 2. 分层架构

| 层 | 载体 | 内容 | 更新 |
|---|---|---|---|
| L0 原始事件 | `analytics_event` | 指令、突破、战斗、经济等 append-only 事件 | 实时异步 |
| L1 每日快照 | `player_daily_snapshot` | 每玩家每日等级/修为/灵石/状态（当日末次写入） | 每小时 upsert |
| L2 报告 | `tools/analytics/report.py` | 按需计算报告（不落库，随时可重算） | 手动/巡检 |

## 3. 事件字典（L0）

| kind | subject | value | payload | 埋点位置 |
|---|---|---|---|---|
| `command` | 指令名 | 耗时 ms | `ok`（成败） | CommandDispatcher |
| `breakthrough_attempt` | Lv{n} | 等级 | `exp/major/tribulation` | CultivationService |
| `breakthrough_result` | small/combat | 等级 | `success/rate/failCount/major` | CultivationService |
| `level_up` | Lv{new} | 新等级 | `from/major/mode` | CultivationService |
| `encounter` | 怪物名 | 回合数 | `won/kills/exp/owner(地图 id)` | CombatEventHandler |
| `stones_gain` | 来源 | 数量 | — | SpiritStoneService / ShopService |
| `stones_spend` | 用途 | 数量 | — | SpiritStoneService / ShopService |

**来源/用途词表**（`stones_*` 的 subject）：`bounty`（悬赏）、`dungeon`（秘境）、`shop_buy` / `shop_buy_equipment`（购买）、`shop_sell` / `shop_sell_equipment`（出售）、`forge`（强化）、`sect_create` / `sect_donate`（宗门）、`beast_hatch` / `beast_breed` / `beast_evolve`（灵兽）、`fudi_build` / `fudi_upgrade`（福地）、`breakthrough_reward`（大境界奖励）、`tribulation`（福地天劫）、`activity_event`（活动事件）、`special_order_*`（调货）、`traveler`（旅行商人）、`gm`（GM）。

### 二期事件（待接入）

| kind | 说明 | 依赖改动 |
|---|---|---|
| `exp_gain` | 修为来源拆分（历练/战斗/顿悟/事件/丹药） | TrainingCompleter / RewardGrant 埋点 |
| `item_gain` / `item_spend` | 物品账本（掉落/炼丹/锻造/丢弃） | RewardGrant / ItemUseService |
| `death` / `dying` | 阵亡与濒死 | PostCombatProcessor |
| `feature_use` | 系统渗透（福地/宗门/秘境/交易/灵兽具体动作） | 各工具类 |
| `ai_chat` | LLM 时延与降级（当前可从 `chat_history` 推导用量） | AbstractChatService.converse |
| `error_code` | 业务错误码分布（当前仅有命令级成败） | CommandHandlerHelper 静态桥 |

## 4. 指标与判据（决定下一步走向）

| 维度 | 指标 | 判据（触发关注） | 对应动作 |
|---|---|---|---|
| 进度 | 各段位实际每级耗时中位数 | 偏离 ADR-0005 目标 ±40% | 调 `Player` 曲线系数/指数 |
| 进度 | 单级突破次数（期望失败补偿后 1~5） | 某段位 >5 次占比 >10% | 调成功率/补偿 |
| 进度 | 等级驻留（同等级快照天数） | 出现「卡死级」聚集 | 查该级内容/数值 |
| 经济 | 灵石产出/消耗比 | 比值 >1.5 或 <0.8 持续一周 | 调商店/悬赏/强化价格 |
| 经济 | 各来源占比 | 单一来源 >50% | 检查是否存在刷币路径 |
| 经济 | 各段位余额 P50/P90 | 中位余额随段位不增长 | 检查产出与消耗错位 |
| 战斗 | 遭遇胜率 | <90% | 调怪物属性（见 README 战斗标定规则） |
| 战斗 | 平局/负率 | >5% | 同上（重点查 20 回合平局） |
| 战斗 | 均回合 | >10 | 降怪物 HP |
| 指令 | 失败率 / p95 耗时 | 失败率 >2%；p95 >5s | 查错误码与慢路径 |
| 留存 | 次日/7 日活跃留存 | 次日 <30% | 查新手流失点（结合驻留数据） |
| 内容 | 新系统使用渗透率 | 上线两周 <10% | 检查入口与引导 |
| 质量 | AI 降级/空回复占比 | >5% | 检查模型与降级链 |

## 5. 分析节奏与决策回路

```
采集（实时/每小时）→ 报告（report.py，按需）→ 定位异常（对照上表判据）
→ 提出改动方案（数值/内容/代码）→ OpenSpec 变更 → 上线
→ 对比改动前后数据（同口径）→ 记录结论（ADR 或设计文档）
```

- **暂不主动推送**（平台限制）：报告由我（agent）在会话中按需生成；建议每次版本发布后与每周固定跑一次。
- 数据量级评估：单 QQ 群、百级玩家、日均数千命令 → 事件表年增 <100 万行，无需分区。

## 6. 使用方式

```bash
python3 tools/analytics/report.py                # 近 7 天报告
python3 tools/analytics/report.py --days 30      # 近 30 天
python3 tools/analytics/report.py --json out.json
```

采集表由 Flyway `V1.0.63__create_analytics.sql` 创建；部署后自动生效。

## 7. 实现状态

- [x] L0 事件表 + 异步批写服务（`AnalyticsService`）
- [x] L1 每日快照（`PlayerSnapshotTask`，每小时）
- [x] 一期埋点：指令、突破/升级、遭遇、灵石经济（含来源）
- [x] L2 报告生成器
- [ ] 二期埋点：修为/物品账本、阵亡、系统渗透、AI 降级、错误码分布
- [ ] 报告自动化：每日生成存档（当前手动）
