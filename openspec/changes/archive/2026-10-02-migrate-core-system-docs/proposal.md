# Proposal

## Why

核心系统设计文档（用户、突破雷劫、战斗、物品装备、法决、灵兽）位于 `docs/`，与 OpenSpec 规格体系并行存在，同一行为需要维护两份记录。需要把设计文档移交 OpenSpec 管理：行为契约进 `specs/<能力>/spec.md`，详细设计进同目录 `design.md`。

## What Changes

- 新增 6 个能力规格：`user`、`breakthrough`、`combat`、`items-equipment`、`skills`、`beasts`。
- 每个能力：`spec.md` 为可校验的行为契约（从原文档提炼，**以实际代码为准**），`design.md` 为原文档的详细设计（公式/表格/数据模型）。
- 迁移完成后删除对应的 `docs/*.md` 源文件（内容不丢失，全部迁入 openspec）。

## Capabilities

### New Capabilities

- `user`: 角色注册、属性、等级、突破、护道、排行榜、查看
- `breakthrough`: 境界突破与雷劫战斗规则
- `combat`: 战斗引擎、伤害公式、技能效果、Buff/Debuff、灵兽 AI
- `items-equipment`: 物品与装备模板、法器类型、克制矩阵、稀有度
- `skills`: 法决模板、效果类型、法器绑定、槽位与自动轮播
- `beasts`: 灵兽品质、进化、变异、技能、出战与恢复

### Modified Capabilities

（无）

## Impact

- `docs/` 下 6 个文档迁入 `openspec/specs/`；无 Java 代码、依赖、数据模型变化。
- 文档与代码冲突处按代码修正，并在各 `design.md` 文末记录差异。
