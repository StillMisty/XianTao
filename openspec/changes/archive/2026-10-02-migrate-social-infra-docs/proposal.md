# Proposal

## Why

社交与基础设施文档（地灵对话、AI 对话、宗门、师徒、交易、异步事件、世界事件、命令参考）仍在 `docs/` 下；此外 `docs/` 还残留 ADR、agent 工作流文档、试玩报告与索引，需要一并移交 OpenSpec，彻底消除多份记录。

## What Changes

- 新增 8 个能力规格：`spirit-chat`、`ai-chat`、`sect`、`master-apprentice`、`trade`、`game-events`、`world-events`、`commands`。
- 修改 `game-features`：移除与系统能力重复的逐系统需求，保留跨领域基线（指令易用性、管理员能力），Purpose 指向各系统规格。
- 结构清理：ADR → `openspec/adr/`；agent 工作流文档 → `openspec/agents/`；试玩报告 → 归档到 `record-qq-messaging-baseline` 变更；删除 `docs/index.md`；清空 `docs/`；同步更新 `AGENTS.md` 与技能中的引用。

## Capabilities

### New Capabilities

- `spirit-chat`: 地灵人格、情绪、好感度、Function Calling 与福地经营
- `ai-chat`: 统一对话基础设施、ChatMemory、历史窗口、降级
- `sect`: 宗门创建、层级、贡献、任务、商店、功法
- `master-apprentice`: 拜师/收徒、护道、出师与冷却
- `trade`: 交易系统设计（NPC 商铺与玩家交易边界，以代码为准）
- `game-events`: 事件总线、Activity 模型、奖励身份、子事件
- `world-events`: 世界事件生成、参与、奖励与补偿投递
- `commands`: 全部指令的可用性与调度契约（含缺参提示、帮助纠错）

### Modified Capabilities

- `game-features`: 收敛为跨领域基线，逐系统需求由对应能力承载（避免重复维护）

## Impact

- `docs/` 目录迁移后清空；`AGENTS.md`、`openspec/agents/` 内引用路径更新。
- 无 Java 代码变化。
