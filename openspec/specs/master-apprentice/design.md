# 师徒系统 详细设计

行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 概述

师徒系统是现有护道系统（`DaoProtection`）的上层封装——收徒自动建立护道关系，护道关系也可独立于师徒存在。师傅纯付出、徒弟纯获益，定位为高境界玩家引导新人的社交机制。

### 设计原则

- **护道泛化**：师徒自动建立护道关系，复用现有护道突破加成。护道可独立于师徒存在（不拜师也能找人护道）
- **师傅零物质收益**：师傅不收徒获取灵石/修为/声望，纯社交驱动
- **一徒一师**：每个徒弟同时只有一个师傅
- **师徒与宗门无关**：拜师不受宗门身份限制，宗门归属变动不影响师徒关系，师徒变动也不影响宗门归属
- **即时建立**：拜师/收徒无需对方同意，直接建立关系

---

## 2. 关系模型

### 2.1 与护道的关系

```
师徒 = 护道 + 额外徒弟收益（修炼速度加成）

护道可独立存在：
  高阶修士 ←护道→ 低阶修士（仅突破加成）

师徒自动包含护道：
  师傅 ←护道+师徒→ 徒弟（突破加成 + 修炼速度加成）
```

### 2.2 数量限制

| 方向 | 上限 | 说明 |
|------|:----:|------|
| 一位师傅最多收徒 | **3 人** | `MasterApprenticeService.MAX_APPRENTICE_COUNT = 3`；与护道 `DaoProtectionService.MAX_PROTECTOR_COUNT = 3` 数值相同但为独立常量 |
| 一位徒弟同时只能有 | **1 人** | 一名师傅（`master_apprentice.apprentice_id` 唯一约束） |

### 2.3 建立条件

| 条件 | 说明 |
|------|------|
| 师傅境界 | 至少高于徒弟 **一个大境界**（`CultivationRealm.fromLevel` 的 `rank` 严格大于，如筑基可收炼气） |
| 冷却检查 | 徒弟的 `cooldown_until` 已过期（被逐出/叛师后 24h 内不可重新拜师） |
| 已有关系 | 徒弟无 ACTIVE 状态的师徒关系；若存在旧记录（GRADUATED / DISMISSED / RENEGED 且冷却已过），会自动覆盖更新（复用同一行，重置状态与师傅 ID） |

---

## 3. 拜师与收徒

### 3.1 拜师流程（即时建立）

```
玩家：拜师 [道号]
  ↓
① 验证目标存在（不存在 → MASTER_NOT_FOUND）
② 验证目标不是自己（MASTER_CANNOT_SELF）
③ 验证自己无 ACTIVE 师徒关系（MASTER_ALREADY_HAS）
④ 验证目标境界比自己高至少一个大境界（MASTER_LEVEL_INSUFFICIENT）
⑤ 验证目标未满收徒上限（< 3 人 ACTIVE，MASTER_FULL）
⑥ 验证自己不在冷却期（MASTER_COOLDOWN，24h）
⑦ 直接建立师徒关系 + 自动建立护道关系
```

### 3.2 收徒流程（即时建立）

```
玩家：收徒 [道号]
  ↓
① 验证目标存在（不存在 → PLAYER_NOT_FOUND）
② 验证目标不是自己（MASTER_APPRENTICE_CANNOT_SELF）
③ 验证目标无 ACTIVE 师徒关系（MASTER_APPRENTICE_HAS_MASTER）
④ 验证自己境界比目标高至少一个大境界（MASTER_LEVEL_INSUFFICIENT）
⑤ 验证自己未满收徒上限（< 3 人 ACTIVE，MASTER_FULL）
⑥ 验证目标不在冷却期（MASTER_COOLDOWN，24h）
⑦ 直接建立师徒关系 + 自动建立护道关系
```

成功后回复文案：拜师为「已拜【道号】为师！护道关系已自动建立。」，收徒为「已收【道号】为徒！护道关系已自动建立。」。

### 3.3 命令

| 指令 | 说明 |
|------|------|
| `拜师 [道号]` | 向目标拜师（直接建立关系） |
| `收徒 [道号]` | 收目标为徒（直接建立关系） |
| `师徒` | 查看师徒关系信息 |
| `逐出师门 [道号]` | 师傅将徒弟逐出师门 |
| `叛师` | 徒弟叛离师门 |

命令模板由 `MasterApprenticeListener` 注册：`拜师\s*{{targetNickname,\S+}}`、`收徒\s*{{targetNickname,\S+}}`、`师徒`、`逐出师门\s*{{targetNickname,\S+}}`、`叛师`，均要求已认证。

---

## 4. 徒弟收益

### 4.1 护道突破加成（复用）

师徒关系自动建立护道关系，徒弟突破时师傅提供成功率加成：

```
单人加成 = 5% + (师傅等级 - 徒弟等级) × 1%
总加成上限 = 20%
仅同地点护道者生效
```

实现位置：`ProtectionHelper.calculateProtectionBonus` / `calculateSingleProtectorBonus`，突破成功率计算时叠加（`CultivationService`）。

**注意（代码行为）**：任何一次突破尝试结束后（成功、失败、渡劫战斗），`CultivationService` 都会调用 `daoProtectionService.clearProtegeRelations(userId)` 清除被护道者的**全部**护道关系。即护道加成是一次性的，突破尝试后师徒自动建立的护道关系也会被解除，需重新建立护道才能再次获得加成。

### 4.2 修炼速度被动加成（设计参考，未接入）

徒弟在挂机修炼时（历练/挂机修为获取）获得额外速度加成：

```
加成倍率 = 1.0 + (师傅等级 - 徒弟等级) × 0.002
上限 = 1.5（最高 50% 加成）
```

**实现方式（设计）**：历练结算时检测师徒关系，取师傅当前等级计算加成，不要求同地点。

**代码现状**：`MasterApprenticeService.calculateTrainingBonus(Long)` 已实现该公式（`Math.clamp(1.0 + levelDiff × 0.002, 1.0, 1.5)`），但全仓库无调用方——历练结算并未使用该加成。本项仅为设计参考。

---

## 5. 师傅收益

**师傅无任何物质收益。** 不收灵石、不获修为、不返贡献。纯社交动机——帮助新人、门派传承感。

---

## 6. 出师机制

### 6.1 自动出师

徒弟境界大阶达到师傅当前的境界大阶时自动出师（`CultivationRealm.fromLevel(...).getRank() >= 师傅 rank`）：

```
示例：师傅等级 45（化神期）
      徒弟达到等级 41（化神期首级）→ 自动出师
```

**触发时机（代码）**：仅在徒弟突破成功时检查（`MasterApprenticeService.checkAndGraduate`，由 `CultivationService` 在普通突破成功与渡劫战斗成功路径调用）；突破失败不检查。

### 6.2 出师效果

| 变化 | 说明 |
|------|------|
| 师徒关系 | 状态设为 GRADUATED，记录 `graduated_at` |
| 护道关系 | 同时解除（突破时不再获得师傅加成） |
| 出师奖励 | **无**（出师即最大回报） |

---

## 7. 关系解除

### 7.1 逐出师门（师傅发起）

- 师傅单方面将徒弟逐出师门；目标必须是自己的 ACTIVE 徒弟（否则 MASTER_APPRENTICE_NOT_FOUND）
- 状态设为 DISMISSED，设置 `cooldown_until = now + 24h`
- 护道关系同步解除（仅解除该师徒对）
- 冷却期内徒弟无法重新拜师

### 7.2 叛师（徒弟发起）

- 徒弟单方面叛离师门；无 ACTIVE 师徒关系时报 MASTER_NO_MASTER
- 状态设为 RENEGED，设置 `cooldown_until = now + 24h`
- 护道关系同步解除（仅解除该师徒对）
- 冷却期内徒弟无法重新拜师

### 7.3 重新建立关系

冷却期过后（`cooldown_until < now`），徒弟可重新拜师/被收徒。`establishMasterApprentice()` 会更新已有记录（覆盖旧状态、旧师傅 ID、清空 `cooldown_until` 与 `graduated_at`），而非插入新记录。GRADUATED 记录 `cooldown_until` 为空，不构成冷却，也可被直接覆盖。

---

## 8. 数据模型

### 8.1 `master_apprentice` — 师徒关系表

实际迁移：`V1.0.43__create_master_apprentice.sql`（表名无 `xt_` 前缀）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `master_id` | BIGINT | 师傅用户 ID（FK → `player(id)` ON DELETE CASCADE） |
| `apprentice_id` | BIGINT | 徒弟用户 ID（FK → `player(id)` ON DELETE CASCADE） |
| `status` | VARCHAR(16) | ACTIVE / GRADUATED / DISMISSED / RENEGED，默认 ACTIVE |
| `created_at` | TIMESTAMP | 建立时间，默认 CURRENT_TIMESTAMP |
| `graduated_at` | TIMESTAMP | 出师时间（NULL=未出师） |
| `cooldown_until` | TIMESTAMP | 冷却截止时间（DISMISSED/RENEGED 后 24h） |
| 约束 | — | `UNIQUE (apprentice_id)` 一徒一师；`UNIQUE (master_id, apprentice_id)` 防重复；`CHECK (master_id != apprentice_id)`；`CHECK status IN (...)` |

状态枚举 `MasterApprenticeStatus`：

| code | 显示名 |
|------|--------|
| ACTIVE | 在师 |
| GRADUATED | 已出师 |
| DISMISSED | 已逐出 |
| RENEGED | 已叛师 |

### 8.2 与护道表的关联

收徒时双向操作（`establishMasterApprentice`）：

```
① 查询 apprentice_id 是否有旧记录 → 有则覆盖更新（复用 ID，重置状态/师傅/冷却/出师时间）
② INSERT/UPDATE master_apprentice (master_id, apprentice_id, status='ACTIVE', cooldown_until=NULL, graduated_at=NULL)
③ 查询 dao_protection (protector_id=master, protege_id=apprentice)
   不存在则 INSERT；已存在则复用（不重复建立）
```

护道关系解除（逐出/叛师/出师）时，按 `(protector_id, protege_id)` 精确删除该对关系。

### 8.3 `dao_protection` — 护道关系表

实际迁移：`V1.0.8__create_dao_protection.sql`。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `protector_id` | BIGINT | 护道者 ID（FK → `player(id)` CASCADE） |
| `protege_id` | BIGINT | 被护道者 ID（FK → `player(id)` CASCADE） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |
| 约束 | — | `UNIQUE (protector_id, protege_id)`；`CHECK (protector_id != protege_id)`；索引 `idx_dao_protection_protege(protege_id)` |

---

## 9. 护道基础关系（独立于师徒）

护道可独立于师徒存在，由 `DaoProtectionService` 提供，命令为 `护道 [道号]`、`护道解除 [道号]`、`护道查询`。

### 9.1 建立条件

| 条件 | 说明 |
|------|------|
| 目标存在 | 否则提示「未找到道号为【X】的修士」 |
| 不能为自己护道 | 拒绝 |
| 境界 | 护道者等级 **≥** 被护道者等级（允许同阶；低于则拒绝） |
| 数量上限 | 一位护道者最多同时为 **3 人**护道（`MAX_PROTECTOR_COUNT`） |
| 重复关系 | 同一对 (protector, protege) 已存在则拒绝 |

建立成功后提示：「已与X建立护道契约！当其在同地点突破时，你将提供 X.X% 的成功率加成」。

### 9.2 加成规则

```
单人加成 = max(0, 5% + (护道者等级 - 被护道者等级) × 1%)
总加成 = min(20%, Σ 同地点护道者的单人加成)
```

- 仅统计与突破者 **同地点**（`location_id` 相同）的护道者
- 总加成上限 20%（`ProtectionHelper.MAX_TOTAL_BONUS_PERCENTAGE`）
- 加成在突破成功率计算时叠加
- **突破尝试后清除**：突破成功/失败/渡劫战斗结束后，被护道者的全部护道关系被删除

### 9.3 护道查询

`护道查询` 展示：

- 我正在为哪些道友护道（含对方地点、是否同地点、单人加成），以及当前数量 / 上限（3）
- 哪些道友正在为我护道（含对方地点、是否同地点、加成），并给出同地点总加成
- 无人为玩家护道时文案「天地孤寂，无道友相护。」；有护道但均不同地点时提示「虽有道友护道，但皆不在同地点，无法提供加成。」；同地点时汇总「共有 N 位道友为你护道，其中 M 位在同地点，总加成 X.X%」

---

## 10. 玩家指令

| 指令 | 说明 |
|------|------|
| `拜师 [道号]` | 拜师（即时建立关系） |
| `收徒 [道号]` | 收徒（即时建立关系） |
| `师徒` | 查看师徒关系信息 |
| `逐出师门 [道号]` | 师傅将徒弟逐出师门 |
| `叛师` | 徒弟叛离师门 |
| `护道 [道号]` | 建立独立护道关系 |
| `护道解除 [道号]` | 解除护道关系 |
| `护道查询` | 查看护道信息与总加成 |

`师徒` 输出：有在师关系时展示师傅道号、境界与状态；无师傅时提示「你尚未拜师，逍遥自在」及引导；作为师傅时列出在师徒弟（道号、境界、状态）与人数。

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- **表名与外键**：实际为 `master_apprentice` / `dao_protection`（无 `xt_` 前缀），外键指向 `player(id)`。
- **收徒上限常量**：`MAX_APPRENTICE_COUNT = 3` 为独立常量（与护道 `MAX_PROTECTOR_COUNT` 数值相同，但非复用）。
- **出师检查时机**：仅在突破成功路径调用 `checkAndGraduate`（普通突破与渡劫战斗成功），失败不检查；因境界只在成功时变化，检查范围等价完备。
- **出师判定**：按大境界 `rank`（`apprentice.rank >= master.rank`），与文档示例一致。
- **护道加成数值**：单人 `max(0, 5% + 等级差 × 1%)`、总上限 20%，与文档一致。
- **师徒与宗门无关**：代码无宗门联动；`SECT_MASTER_MUST_MANAGE_APPRENTICE` 等错误码无调用方。

### B. 保留代码设计

- **护道关系一次性（任意突破尝试后清除全部护道）**：护道成为一次性情谊，避免长期挂名白嫖加成；每次突破前重新结缘，强化社交互动（已写入 spec.md 契约）。
- **护道门槛宽于师徒（护道允许同阶，师徒需高一个大境界）**：护道是轻量互助，同阶可互护扩大社交面；师徒保留严门槛以维持传承意义。
- **出师无冷却（GRADUATED 记录 `cooldown_until` 为空，可立即另拜）**：出师是圆满结局而非惩罚；同境界下无法再拜原师（等级差不足），立即另投更高境界师傅不破坏平衡。

### C. 按设计修正（待修）

无

### D. 未实现（待办）

- **修炼速度被动加成**：设计意图：徒弟挂机修炼获得 `1.0 + 等级差 × 0.002`（上限 1.5）加成，历练结算时读取师徒关系；现状：`calculateTrainingBonus` 已实现公式但全仓库无调用方，历练不应用该加成。

### E. 缺陷修复

- **帮助列表与实际命令不一致**（已修）：触发词改为「逐出师门 「道号」」；拜师/收徒描述改为与即时建立一致。
