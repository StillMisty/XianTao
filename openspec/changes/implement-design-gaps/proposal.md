# Proposal

## Why

迁移评估（311 项差异）把「设计有、代码缺」的项目统一登记为 D 类待办（34 项）。这些缺口让不少已设计好的玩法（灵兽历练修为、师徒加成、宗门建筑、区域世界事件、装备战斗数值等）无法生效，需要逐项实现并回归契约。

## What Changes

按「更合乎玩法」的评估结论实现 D 类待办，分两波：

**第一波（已派发）**

- `training` / `beasts` / `master-apprentice` / `sect`：灵兽历练修为（分钟×2）、战斗修为（怪物等级×10）、师徒修炼加成（1+差×0.002，上限 1.5）、练功房 +3%/级 接入历练结算
- `breakthrough`：招雷散「战胜 +50% 修为」补偿、渡劫后玩家与灵兽气血写回
- `user` / `combat`：装备四维/攻防/词条/锻造计入战斗数值（护甲成为防御源）
- `map-travel`：`地图列表` 命令（世界总览 + 怪物概览 + 相邻关系）
- `sect`：炼丹房 +5%/级、锻造坊 -5% 强化费、护阵 -3%/级 接入对应结算
- `dungeon`：SECT 准入校验、隐藏区域按 `trigger_after_resolve` 解锁
- `world-events`：REGIONAL 事件写 `region_map_node_id` + `valid_region_tags` 过滤 + 按区域展示；NARRATIVE 注入地灵对话
- `game-events`：补 5 种隐藏触发条件（装备/地点/时段/出战灵兽/等级区间）、`BUFF_EXPIRED` 事件、悬赏自动完成 + `BOUNTY_READY` 提示
- `pill-buffs`：过期 Buff 全局定时清理（存储卫生）

**第二波（规划）**

- `skills`：PASSIVE 法决习得即生效、不占槽位；`RESIST_BUFF`/`HP_BUFF`/`SURVIVE_LETHAL` 被动效果落地；`require_skill_id` 法决树前置（含种子补数据）
- `game-events`：悬赏两阶段隐藏线索（接取写 `hidden_clues`、领奖校验）、秘境事件管道（DUNGEON_ENTER/EXPLORE/HIDDEN/COMPLETE）
- `sect`：宗门动态事件（`last_event_*` 写入 + 总览/宗灵展示）
- `trade`：调货机制（定金 → 调货时长 → 尾款取货）、旅行商人临时商铺
- `spirit-chat` / `world-events`：福地事件系统（`fudi_event_template` + 地灵对话触发）、情绪状态机
- `dungeon`：多人组队（当前 `max_team_size=1`，随数据启用）

## Capabilities

### Modified Capabilities

- `training`、`beasts`、`master-apprentice`、`sect`、`breakthrough`、`user`、`combat`、`map-travel`、`dungeon`、`world-events`、`game-events`、`pill-buffs`、`skills`、`trade`、`spirit-chat`：按实现落地补充行为契约（新增/修改 Requirement 与 Scenario）。

## Impact

- 代码：`service/`（训练、战斗、突破、宗门、秘境、事件）、`domain/`（PlayerCombatant、Beast、Skill）、少量种子数据迁移（`require_skill_id`）。
- 契约：相关能力的 `spec.md` 增补新行为；`design.md` 的「迁移评估」D 段同步为已实现。
- 平衡：装备接入战斗、招雷散补偿、灵兽修为来源会提升玩家强度，数值观察后再调。
