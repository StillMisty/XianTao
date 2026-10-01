# Spec Delta

## REMOVED Requirements

### Requirement: 角色与成长

**Reason**: 逐系统契约迁移至独立能力，避免与系统规格重复维护。
**Migration**: 行为契约见 `openspec/specs/user/spec.md`，详细设计见 `openspec/specs/user/design.md`。

### Requirement: 探索与历练

**Reason**: 拆分为地图/历练/悬赏/运势/世界事件等独立能力。
**Migration**: 见 `map-travel`、`training`、`bounty`、`fortune`、`world-events` 能力规格。

### Requirement: 战斗与装备

**Reason**: 拆分为战斗/物品装备/锻造强化等独立能力。
**Migration**: 见 `combat`、`items-equipment`、`forging` 能力规格。

### Requirement: 炼丹与物品

**Reason**: 拆分为炼丹与丹药 Buff 两个独立能力。
**Migration**: 见 `alchemy`、`pill-buffs` 能力规格。

### Requirement: 社交

**Reason**: 拆分为师徒与宗门两个独立能力。
**Migration**: 见 `master-apprentice`、`sect` 能力规格。

### Requirement: 福地与秘境

**Reason**: 拆分为福地与秘境两个独立能力。
**Migration**: 见 `fudi`、`dungeon` 能力规格。

### Requirement: 商铺交易

**Reason**: 独立为交易能力。
**Migration**: 见 `trade` 能力规格。

### Requirement: AI 对话能力

**Reason**: 拆分为统一对话基础设施与地灵对话两个独立能力。
**Migration**: 见 `ai-chat`、`spirit-chat` 能力规格。

### Requirement: 管理员能力

**Reason**: GM 指令权限属于指令体系契约。
**Migration**: 见 `commands` 能力规格。

### Requirement: 指令易用性

**Reason**: 缺参提示、帮助纠错、注册引导等属于指令体系契约。
**Migration**: 见 `commands` 能力规格。
