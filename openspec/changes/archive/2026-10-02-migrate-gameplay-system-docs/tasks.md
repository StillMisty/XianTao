# Tasks

## 1. 能力转换（子代理执行，代码为准）

- [x] 1.1 `alchemy`：docs/炼丹系统.md → openspec/specs/alchemy/（8 需求 / 19 场景）
- [x] 1.2 `pill-buffs`：docs/丹药Buff系统.md → openspec/specs/pill-buffs/（7 需求 / 14 场景）
- [x] 1.3 `forging`：docs/装备强化与锻造系统.md → openspec/specs/forging/（10 需求 / 23 场景）
- [x] 1.4 `map-travel`：docs/地图旅行历练.md → openspec/specs/map-travel/（8 需求 / 17 场景；冲突 8 项，含遭遇分布、旅行事件数据驱动）
- [x] 1.5 `training`：docs/历练系统设计.md → openspec/specs/training/（10 需求 / 20 场景；冲突 9 项，含中途懒结算、720 分钟封顶）
- [x] 1.6 `bounty`：docs/悬赏.md → openspec/specs/bounty/（10 需求 / 22 场景；冲突 9 项，含奖励池遍历发放、唯一悬赏）
- [x] 1.7 `fortune`：docs/运势系统.md → openspec/specs/fortune/（7 需求 / 16 场景；冲突 6 项，含维度命名、财运幅度与作用范围）
- [x] 1.8 `fudi`：docs/福地.md → openspec/specs/fudi/（11 需求 / 29 场景；冲突 10 项，含渡劫冷却、无独立指令、产量公式）
- [x] 1.9 `dungeon`：docs/秘境系统设计.md → openspec/specs/dungeon/（10 需求 / 25 场景；冲突 9 项，含 SECT 准入未实现、好感效果未实现）

## 2. 校验与归档

- [x] 2.1 逐份抽查：9 份 spec 契约可对应命令/错误码；9 份 design.md 均含「与文档的差异（以代码为准）」段
- [x] 2.2 `openspec validate migrate-gameplay-system-docs` 通过
- [x] 2.3 `openspec archive migrate-gameplay-system-docs --yes`，9 个能力进入 specs
- [x] 2.4 删除已迁移的 9 个 docs 源文件并确认无残留引用
