# Tasks

## 1. 第一波：小接线与中型实现

### 1.1 历练线（training / beasts / master-apprentice / sect）

- [x] 1.1.1 历练结算给出战灵兽 +修为（分钟 × 2，`TrainingSettler` 逐段累计不重复）
- [x] 1.1.2 战斗胜利给出战灵兽 +修为（怪物等级 × 10，经 `EncounterResult.beastExpGained` 汇总）
- [x] 1.1.3 师徒 `calculateTrainingBonus` 接入历练修为（1 + 差 × 0.002，上限 1.5）
- [x] 1.1.4 练功房 +3%/级 接入历练修为（`getTrainingBonusForUser`，无宗门为 0）

### 1.2 突破线（breakthrough）

- [x] 1.2.1 招雷散「战胜 +50% 修为」补偿（基数 = 本次突破扣除修为，文案追加「招雷淬体」）
- [x] 1.2.2 渡劫后玩家与灵兽气血写回（复用 `PostCombatProcessor`，胜回满/败残血或濒死）

### 1.3 装备战斗线（user / combat）

- [x] 1.3.1 抽取 `EquipmentStats` 聚合供状态/档案/战斗共用（含词条与锻造）
- [x] 1.3.2 `PlayerCombatant` 接入装备四维/总攻/总防（攻击/防御/速度三式）
- [x] 1.3.3 `DefaultTeamBuilder` 一次加载并派生武器与聚合（无双重计算）

### 1.4 地图 / 宗门 / 秘境

- [x] 1.4.1 `地图列表` 命令（世界总览 + 妖兽概览 + 相邻关系；帮助条目、测试同步）
- [x] 1.4.2 炼丹房 +5%/级、锻造坊 -5% 强化费、护阵 -3%/级 接入（1 分钟缓存 + 建造/升级驱逐）
- [x] 1.4.3 秘境 SECT 准入校验（`DUNGEON_SECT_RESTRICTED`）
- [x] 1.4.4 隐藏区域按 `trigger_after_resolve` 解锁（跨区域保留探索记录）

### 1.5 事件线（world-events / game-events / pill-buffs / spirit-chat）

- [x] 1.5.1 REGIONAL 事件绑定 `region_map_node_id`（`MapRegionTags` 关键词推断 + 模板过滤）
- [x] 1.5.2 世界事件按区域展示（全局 + 当地，`findActiveByRegion` 生效）
- [x] 1.5.3 补 5 种隐藏触发条件（HAS_EQUIPMENT / LOCATION / TIME_OF_DAY / BEAST_DEPLOYED / LEVEL_RANGE）
- [x] 1.5.4 `BUFF_EXPIRED` 事件产出（懒清理合并一条，被动回复管道）
- [x] 1.5.5 悬赏自动完成 + `BOUNTY_READY` 提示（`BountyReadyHandler`，保留领奖动作）
- [x] 1.5.6 过期 Buff 全局定时清理（每小时，`PlayerBuffCleanupTask`）
- [x] 1.5.7 NARRATIVE 世界事件注入地灵对话（参考 ShopChatService）

### 1.6 结构重构：消除构造器循环依赖（无 `@Lazy`）

- [x] 1.6.1 拆出 `PlayerLoader` 叶子组件（只依赖 `UserRepository`，提供 load/loadReadOnly/findByNickname）
- [x] 1.6.2 结算收敛到命令边界：`CommandDispatcher` 认证后 `UserStateService.settle`，深层服务纯加载
- [x] 1.6.3 三处闭环类改用 `PlayerLoader`：`FudiHelper`、`EquipmentService`、`SectMemberService`
- [x] 1.6.4 移除全部 `@Lazy`（含 `DamageCalculator` 显式构造回归 Lombok）
- [x] 1.6.5 `AGENTS.md` 记录「State Settlement & Player Loading」约定

### 1.7 第一波验证

- [x] 1.7.1 `./gradlew build` 全绿（Spotless + NullAway + 全部测试）
- [x] 1.7.2 指令巡检（Map/Status/Leaderboard/Pill、新手指令集）+ 无环启动验证
- [x] 1.7.3 结算专项验证 7/7：旅行抵达、历练中途结算、历练结算回空闲、悬赏自动完成 + 领奖
- [x] 1.7.4 试玩工具 newbie + negative 阶段 43 步通过
- [x] 1.7.5 各 design.md 的 D 段同步为「已实现」

## 2. 第二波：大型功能

- [x] 2.1 skills：PASSIVE 习得即生效、不占槽位；`RESIST_BUFF`/`HP_BUFF`/`SURVIVE_LETHAL` 被动效果
- [x] 2.2 skills：`require_skill_id` 法决树前置（种子数据 + 学习校验）
- [x] 2.3 game-events：悬赏两阶段隐藏线索（`hidden_clues` 接取写入 + 领奖校验）
- [x] 2.4 game-events：秘境事件管道（DUNGEON_ENTER / EXPLORE / HIDDEN / COMPLETE）
- [x] 2.5 sect：宗门动态事件（`last_event_*` 写入 + 总览/宗灵展示）
- [x] 2.6 trade：调货机制（定金 → 调货时长 → 尾款取货）
- [x] 2.7 trade：旅行商人临时商铺（事件触发、价格浮动、LLM 对话）
- [x] 2.8 spirit-chat / world-events：福地事件系统（`fudi_event_template` + 地灵对话触发）
- [x] 2.9 spirit-chat：情绪状态机（updateEmotion 工具 + Prompt 注入）
- [ ] 2.10 dungeon：多人组队（**未做**：当前数据 `max_team_size=1`，属数据门槛，待组队玩法数据启用时另行实现）

## 3. 契约同步与归档

- [x] 3.1 为已实现行为补写各能力 spec 增量（16 个能力：ADDED/MODIFIED，含已知契约修订）
- [x] 3.2 `openspec validate implement-design-gaps --strict` 通过
- [x] 3.3 归档变更；`openspec validate --specs` 全绿
