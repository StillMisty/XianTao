# Design

## Context

- 前两批（核心 6 + 玩法 9）完成迁移后，`docs/` 仅剩本批 8 个玩法文档、4 个 ADR、3 个 agent 文档、索引与试玩报告。
- `game-features` 规格（10 条需求）是上一变更建立的基线，与本批及前批的系统能力存在内容重叠，需收敛为跨领域基线。
- `AGENTS.md` 的「Domain docs」「Agent skills」段落引用了 `docs/adr/` 与 `docs/agents/` 路径，迁移后需同步。

## Goals / Non-Goals

**Goals:**

- 8 个能力完成契约 + 详情迁移；`docs/` 清空；引用全部指向新路径。
- `game-features` 不再承载逐系统契约，避免双份维护。

**Non-Goals:**

- 不删除任何 ADR 内容（仅换路径）；不重写 agent 工作流文档内容。
- 不把试玩报告拆解进规格（报告是证据，随归档变更保存）。

## Decisions

### D1 `game-features` 收敛方式

- 通过本变更的 MODIFIED 需求实现：移除 user/探索历练/战斗装备/炼丹物品/社交/福地秘境/商铺/AI 对话 8 条逐系统需求（内容由对应系统能力承载），保留「指令易用性」「管理员能力」，Purpose 改为「跨领域基线与系统规格索引」。

### D2 ADR 与 agent 文档的新家

- ADR → `openspec/adr/`（保持 ADR-0001..0004 命名）；agent 工作流 → `openspec/agents/`。
- 理由：OpenSpec 根目录成为唯一文档树；ADR/agent 文档不是行为契约，不进入 `specs/`。

### D3 试玩报告归属

- `docs/playtest/playtest-report-2026-10-02.html` → `openspec/changes/archive/2026-10-02-record-qq-messaging-baseline/playtest-report-2026-10-02.html`，作为该变更的证据附件。

## Risks / Trade-offs

- [`game-features` 收敛造成信息丢失] → 逐系统内容在对应能力中已有更细的契约；归档前抽查覆盖度。
- [引用遗漏] → 迁移后全仓 grep `docs/`（排除 `docs/` 已删）确认无残留；`AGENTS.md`、`openspec/agents/domain.md` 重点检查。
