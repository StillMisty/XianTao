# Tasks

## 1. 能力转换（子代理执行，代码为准）

- [x] 1.1 `spirit-chat`：docs/地灵对话.md → openspec/specs/spirit-chat/（8 需求 / 18 场景；冲突 7 项，含情绪/福地事件未实现、工具集变更）
- [x] 1.2 `ai-chat`：docs/AI对话系统.md → openspec/specs/ai-chat/（7 需求 / 13 场景；冲突 6 项，含窗口/模型降级链/限流）
- [x] 1.3 `sect`：docs/宗门系统设计.md → openspec/specs/sect/（12 需求 / 30 场景；冲突 10 项，含任务系统不存在、邀请冷却记录无清理）
- [x] 1.4 `master-apprentice`：docs/师徒系统设计.md → openspec/specs/master-apprentice/（12 需求 / 29 场景；冲突 6 项，含护道一次性、修炼加成无调用方）
- [x] 1.5 `trade`：docs/交易系统设计.md → openspec/specs/trade/（12 需求 / 28 场景；冲突 7 项，含玩家间交易不存在、砍价标记）
- [x] 1.6 `game-events`：docs/异步事件系统.md → openspec/specs/game-events/（12 需求 / 24 场景；冲突 9 项，含 narrative 存模板文本、触发条件 3/9）
- [x] 1.7 `world-events`：docs/世界事件系统.md → openspec/specs/world-events/（9 需求 / 19 场景；冲突 13 项，含区域优先、cooldown 未实现）
- [x] 1.8 `commands`：docs/命令参考.md → openspec/specs/commands/（10 需求 / 21 场景；冲突 11 项，含 GM 未入帮助、选 A–Z）

## 2. game-features 收敛

- [x] 2.1 退休 `game-features`：`.openspec.yaml` 声明 `retire_capabilities: true`；REMOVED delta 覆盖全部 10 条需求（含 Reason 与 Migration，指向承接能力）
- [x] 2.2 归档后确认 `game-features` 已从 specs 移除，承接能力覆盖其内容

## 3. 结构清理

- [x] 3.1 ADR：docs/adr/ADR-000*.md → openspec/adr/（4 个，内容不变）
- [x] 3.2 agent 文档：docs/agents/*.md → openspec/agents/（3 个），更新其中对 docs/ 的引用
- [x] 3.3 试玩报告 → openspec/changes/archive/2026-10-02-record-qq-messaging-baseline/playtest-report-2026-10-02.html
- [x] 3.4 更新 `AGENTS.md` 引用（Domain docs、Agent skills）指向 `openspec/adr/`、`openspec/agents/`
- [x] 3.5 删除 docs/index.md 与本批 8 个源文档；确认 `docs/` 清空
- [x] 3.6 全仓 grep `docs/` 确认无残留引用（历史归档除外）

## 4. 校验与归档

- [x] 4.1 `openspec validate migrate-social-infra-docs` 通过
- [x] 4.2 `openspec archive migrate-social-infra-docs --yes`，8 个新能力生效 + `game-features` 退休
- [x] 4.3 `openspec validate --specs` 全绿；`openspec list --specs` 展示 24 个能力
