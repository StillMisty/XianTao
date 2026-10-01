# Tasks

## 1. 能力转换（子代理执行，代码为准）

- [x] 1.1 `user`：docs/用户.md → openspec/specs/user/（11 需求 / 27 场景；冲突 8 项，含四维基础值、突破公式、表名）
- [x] 1.2 `breakthrough`：docs/突破雷劫系统.md → openspec/specs/breakthrough/（9 需求 / 22 场景；冲突 6 项，含境界层数 120、雷劫解锁层级）
- [x] 1.3 `combat`：docs/战斗系统.md → openspec/specs/combat/（10 需求 / 22 场景；冲突 10 项，含目标选择、攻速机制、EffectType 22 种）
- [x] 1.4 `items-equipment`：docs/物品装备.md → openspec/specs/items-equipment/（9 需求 / 20 场景；冲突 12 项，含表结构、克制实现、命名规则）
- [x] 1.5 `skills`：docs/法决.md → openspec/specs/skills/（9 需求 / 17 场景；冲突 8 项，含公式变量、施放机制）
- [x] 1.6 `beasts`：docs/灵兽.md → openspec/specs/beasts/（11 需求 / 28 场景；冲突 9 项，含无指令入口、孵化硬编码、特性槽）

## 2. 校验与归档

- [x] 2.1 逐份抽查：6 份 spec 契约可对应命令/错误码/常量；6 份 design.md 均含「与文档的差异（以代码为准）」段
- [x] 2.2 `openspec validate migrate-core-system-docs` 通过
- [x] 2.3 `openspec archive migrate-core-system-docs --yes`，6 个能力进入 specs
- [x] 2.4 删除已迁移的 6 个 docs 源文件并 grep 确认无残留引用
