# Proposal

## Why

玩法系统设计文档（炼丹、丹药 Buff、锻造强化、地图旅行历练、历练、悬赏、运势、福地、秘境）仍在 `docs/` 下，与 OpenSpec 规格并行维护。延续 `migrate-core-system-docs` 的方式，把行为契约与详细设计一并迁入 OpenSpec。

## What Changes

- 新增 9 个能力规格：`alchemy`、`pill-buffs`、`forging`、`map-travel`、`training`、`bounty`、`fortune`、`fudi`、`dungeon`。
- 每个能力：`spec.md` 行为契约（以实际代码为准）+ `design.md` 详细设计。
- 迁移完成后删除对应的 9 个 `docs/*.md` 源文件。

## Capabilities

### New Capabilities

- `alchemy`: 五行炼丹、药材、丹方、丹药效果与抗性
- `pill-buffs`: 丹药 Buff 类型、有效期、战斗/突破加成
- `forging`: 锻材三性、图纸、品质分→稀有度、强化三阶段
- `map-travel`: 地图节点、怪物、旅行、历练结算、悬赏入口
- `training`: 历练核心组件、计算公式、输出格式与结算节奏
- `bounty`: 悬赏配置、奖励预确定、D20 事件、LLM 美化
- `fortune`: 今日运势的维度与影响
- `fudi`: 福地管理、灵气、地块、种植、天劫
- `dungeon`: 秘境准入、区域探索、进度与奖励、秘灵

### Modified Capabilities

（无）

## Impact

- `docs/` 下 9 个文档迁入 `openspec/specs/`；无代码变化。
- 冲突按代码修正并在 `design.md` 记录差异。
