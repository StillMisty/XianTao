# 法决系统 详细设计

> 行为契约见同目录 spec.md；本文档为详细设计参考。

## 1. 法决定义 (skill)

### 1.1 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `name` | VARCHAR(64) | 法决名称（UNIQUE） |
| `description` | VARCHAR(256) | 描述 |
| `skill_type` | VARCHAR(16) | 技能类型: ACTIVE / PASSIVE（默认 ACTIVE） |
| `effects` | JSONB | 效果列表 `[{"type":"DAMAGE","formula":"attack*1.5","target":"single"}]` |
| `binding_type` | VARCHAR(32) | 法器绑定类型: NONE / WEAPON_TYPE / WEAPON_CATEGORY / ELEMENT（默认 NONE） |
| `binding_value` | VARCHAR(64) | 绑定值（法器子类型 code、法器大类 code 或元素名） |
| `cooldown_seconds` | INT | 独立 CD（秒，默认 30，CHECK ≥0） |
| `level_requirement` | INT | 等级要求（默认 1，CHECK ≥1） |
| `require_wis` | INT | 智慧要求（可空） |
| `require_skill_id` | BIGINT | 前置法决（可空，FK 自引用，法决树预留） |
| `tags` | JSONB | 标签（用于检索、分类） |
| `create_time` / `update_time` | TIMESTAMP | 时间戳 |

> 代码/迁移中的实际表名为 `skill`（无 `xt_` 前缀）；name 长度为 64（非 128）。

### 1.2 effects JSONB 结构

每个效果是一个 `SkillEffect` 对象，存储在 `effects` JSONB 数组中。字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | VARCHAR(32) | 效果类型 (EffectType) |
| `formula` | VARCHAR(128) | 伤害/治疗公式，变量: `attack`/`wis`/`str`/`agi`（含别名 `atk`），支持 `+`/`-`/`*`/`/` 和括号以及小数 |
| `value` | DOUBLE | 效果数值（buff百分比、dot每回合伤害比例、治疗比例、多段次数、斩杀阈值等） |
| `duration` | INT | 持续回合数（buff/debuff/dot 使用） |
| `maxStacks` | INT | 最大叠加层数（dot 使用） |
| `chance` | DOUBLE | 触发概率（0-1，默认 1.0） |
| `element` | VARCHAR(32) | 元素类型（可选） |
| `target` | VARCHAR(16) | 目标选择: single / aoe / random |

> **伤害公式说明**：`formula` 使用递归下降表达式解析器（`DamageCalculator.evaluateFormula`），支持小数点与运算符优先级。实际可用变量：`attack`（攻击）、`wis`（悟性）、`str`（力道）、`agi`（身法），以及 `atk`（攻击别名）；**不支持 `defense`/`con`**（写这两个变量会解析失败并回退为攻击力）。支持运算：`+`、`-`、`*`、`/`、括号 `()`。例：`"wis*3+20"`、`"attack*1.5"`。

### 1.3 获取方式

| 方式 | 说明 |
|------|------|
| 法决玉简 | 从背包使用法决玉简消耗习得（境界达标且未学过） |
| 历练顿悟 | 历练中按悟性触发，随机习得可学列表中的法决（不消耗物品） |
| 宗门共享功法 | 消耗宗门贡献学习（需境界达标） |
| 任务奖励 / NPC 购买 | 以法决玉简物品形式发放/出售，最终仍走玉简使用流程 |

### 1.4 技能类型 (skill_type)

| 类型 | 说明 |
|------|------|
| `ACTIVE` | 主动技能，战斗槽位中自动施放 |
| `PASSIVE` | 被动技能（预留），习得即生效，不占槽位 |

> 被动效果类型 `RESIST_BUFF`/`HP_BUFF`/`SURVIVE_LETHAL` 已注册但为 no-op（不会进入主动施放处理）。

---

## 2. 效果类型 (EffectType)

代码共 **22 种**效果类型（原文档 15 种 + 后续新增 7 种）。其中 19 种有主动战斗处理器，3 种为被动预留（no-op）。

### 2.1 伤害类

| 效果 | 说明 | effects 示例 |
|------|------|-------------|
| `DAMAGE` | 直接伤害 | `[{"type":"DAMAGE","formula":"attack*1.5","target":"single"}]` |
| `MULTI_HIT` | 多段伤害（value=段数，默认 3） | `[{"type":"MULTI_HIT","formula":"attack*0.6","target":"single"}]` |
| `EXECUTE` | 斩杀（低血量额外伤害，value=气血阈值，默认 0.3，低于阈值伤害 ×2） | `[{"type":"EXECUTE","formula":"attack*3.0","chance":0.3,"target":"single"}]` |
| `LIFESTEAL` | 吸血（value=转化比例，默认 0.33） | `[{"type":"LIFESTEAL","value":15,"target":"single"}]` |
| `AOE_DAMAGE` | 群体伤害（副目标按 60% 伤害） | `[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"}]` |

### 2.2 控制类

| 效果 | 说明 | effects 示例 |
|------|------|-------------|
| `ARMOR_BREAK` | 破甲（降低防御） | `[{"type":"ARMOR_BREAK","value":30,"duration":2,"target":"single"}]` |
| `SLOW` | 减速（降低攻速） | `[{"type":"SLOW","value":20,"duration":2,"target":"single"}]` |
| `DOT` | 持续伤害（每回合按攻击×value 结算，可叠层） | `[{"type":"DOT","value":15,"duration":3,"maxStacks":3,"target":"single"}]` |
| `STUN` | 眩晕（跳过行动） | `[{"type":"STUN","chance":0.4,"duration":1,"target":"single"}]` |
| `FREEZE` | 冰冻（跳过行动+受伤增加） | `[{"type":"FREEZE","chance":0.3,"duration":2,"target":"single"}]` |
| `SILENCE` | 沉默（禁止技能） | `[{"type":"SILENCE","chance":0.5,"duration":2,"target":"single"}]` |

### 2.3 辅助类

| 效果 | 说明 | effects 示例 |
|------|------|-------------|
| `HEAL` | 治疗（按最大气血×value，默认 0.5） | `[{"type":"HEAL","formula":"wis*0.8+100","value":80,"target":"single"}]` |
| `ATTACK_BUFF` | 攻击增益 | `[{"type":"ATTACK_BUFF","value":15,"duration":3,"target":"single"}]` |
| `DEFENSE_BUFF` | 防御增益 | `[{"type":"DEFENSE_BUFF","value":10,"duration":999,"target":"single"}]` |
| `SPEED_BUFF` | 速度增益 | `[{"type":"SPEED_BUFF","value":15,"duration":2,"target":"single"}]` |

### 2.4 后续新增效果

| 效果 | 说明 |
|------|------|
| `RESIST_BUFF` | 抗性增益（被动预留，no-op） |
| `HP_BUFF` | 气血增益（被动预留，no-op） |
| `DODGE` | 闪避（施加闪避 buff） |
| `CLEANSE` | 净化（移除自身负面 buff） |
| `COUNTER` | 反击（施加反击 buff） |
| `REFLECT` | 反射（施加反伤 buff） |
| `SURVIVE_LETHAL` | 濒死生存（被动预留，no-op） |

> 效果处理位于 `EffectHandlerRegistry`（EffectType → EffectHandler 注册表），不再集中在 `DefaultCombatEngine.resolveAction`；新增效果类型只需注册 handler。

---

## 3. 绑定类型 (binding_type)

| 类型 | 说明 | 强度 |
|------|------|------|
| `NONE` | 不绑定法器，任何法器都能触发 | 较弱 |
| `WEAPON_TYPE` | 精确绑定（如必须装备"剑"） | 强 |
| `WEAPON_CATEGORY` | 大类绑定（如"刀兵"覆盖刀/剑/斧） | 中等 |
| `ELEMENT` | 元素绑定（预留，恒通过筛选） | — |

### 3.1 法器大类

| 大类（枚举中文） | DB/种子 code | 包含法器子类型 |
|-------------|--------------|-------------|
| 刀兵 | MELEE | 刀、剑、斧 |
| 长兵 | POLEARM | 枪、棍 |
| 远兵 | RANGED | 弓 |
| 奇兵 | EXOTIC | 鞭、戟、锤、匕首、扇、拂尘、圈、钟 |

**设计原则：** 绑定法器的法决效果强于不绑定的，由此产生装备策略性——玩家需要根据地图怪物类型选择法器和配套法决。

> 战斗组队时按当前装备法器筛选已装载法决：`WEAPON_TYPE` 以法器子类型 code 精确比较；`WEAPON_CATEGORY` 以 `WeaponType.category`（中文）与 `binding_value` 比较；`ELEMENT` 恒通过。由于种子数据 binding_value 为英文 code，而 `WeaponType.category` 为中文，当前大类绑定实际无法命中（见文末差异）。

---

## 4. 战斗槽位

| 等级 | 可用槽位数 |
|------|----------|
| 初始 | 1 |
| 20 级 | 2 |
| 40 级 | 3 |
| 60 级 | 4 |
| 80 级 | 5 |

法决可以无限学习，但战斗中只有已放入槽位的法决生效。装载/卸载通过 `法决装载` / `法决卸下`；装载使用原子条件更新（`equipIfSlotAvailable`）防止并发超额。

---

## 5. 已学法决 (player_skill)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL | PK |
| `user_id` | BIGINT | FK → player(id) |
| `skill_id` | BIGINT | FK → skill(id) |
| `is_equipped` | BOOLEAN | 是否放入战斗槽位（默认 FALSE） |
| `source_sect_id` | BIGINT | 来源宗门 ID（退宗时按此列删除共享功法） |
| `create_time` | TIMESTAMP | 习得时间 |

`UNIQUE(user_id, skill_id)`：防止重复学习同一法决。

> 原文档的 `equipped` / `source`（JADE/REWARD/INITIAL）/ `learned_at` 三列不存在；实际为 `is_equipped` / `source_sect_id` / `create_time`，没有记录习得来源枚举。

---

## 6. 战斗自动施放

### 6.1 核心逻辑

```
每场自动战斗的每个行动帧：
  若行动者被沉默 → 不释放法决，转为普通攻击
  可用技能 = 已装载且未在冷却中的法决
  随机选择一个可用技能（无优先级）
  若技能所有效果都未命中/未生效 → 视为普通攻击，不进入冷却
  否则：触发效果，进入冷却
  CD 回合数 = max(1, cooldown_seconds / 攻速)
  每回合结束所有 CD -1，归零移除
```

### 6.2 设计原则

- **零消耗**：法决不消耗灵力/HP，纯 CD 限制
- **独立 CD**：每个法决各自独立冷却，互不干扰
- **自动施放**：无优先级、无触发条件判断（除效果自身 chance/斩杀阈值），CD 好就随机选用
- **法器约束**：绑定了法器类型/大类的法决，在组建队伍时若当前装备不匹配则被过滤（不进入战斗技能列表）
- **沉默**：被沉默时无法释放法决，只能普通攻击

---

## 7. 法决玉简 (SKILL_JADE)

法决玉简是一种**可堆叠物品**，通过 `ItemType.SKILL_JADE` 标识。

`ItemTemplate.properties` 中记录关联的法决 ID：

```json
{"skill_id": 1}
```

### 使用流程

玩家在背包中使用法决玉简 → 校验（玉简存在、法决存在、未学过、境界达标）→ 消耗 1 件玉简 → 向 `player_skill` 插入记录（未装载）→ 习得对应法决。

失败情形：背包无法决玉简、名称不匹配（列出持有玉简供参考）、玉简对应法决不存在、已学会、境界不足。

### 获取方式

- 怪物掉落（在怪物模板的 `drop_table.items` 中配置为普通物品掉落）
- 任务奖励
- 坊市购买

---

## 8. 批量设计思路

### 8.1 同效果类型 × 不同绑定

```
DAMAGE + 剑绑定 → 御剑术（CD 30s，wis×3）
DAMAGE + 刀绑定 → 破空斩（CD 40s，wis×4）
DAMAGE + 弓绑定 → 穿云箭（CD 35s，wis×2.5）
DAMAGE + 无绑定 → 烈焰诀（CD 45s，wis×2）← 无绑定但较弱
```

### 8.2 同绑定 × 不同效果

```
剑绑定 + DAMAGE → 御剑术
剑绑定 + MULTI_HIT → 万剑诀
剑绑定 + ARMOR_BREAK → 破剑式
```

### 8.3 同效果 × 不同参数

```
DAMAGE + CD短 → 小技能（快速循环）
DAMAGE + CD长 → 大技能（高爆发）
```

### 8.4 法决数量估算

```
15 种效果 × 5 种绑定 × 3 种参数档位 = 225 种基础法决
再加上等级梯度（低/中/高）× 3 = 675 种
```

达到几百上千种并不困难（实际效果类型已扩展到 22 种）。

---

## 9. 玩家指令入口（按代码补充）

| 指令 | 说明 |
|------|------|
| `法决` | 已装载法决详情 + 已学法决编号列表（未装载显示 ◇/◆） |
| `法决装载 [名称/编号]` | 装载到槽位；槽位满/已装载/未学给出提示 |
| `法决卸下 [名称/编号]` | 从槽位卸下 |
| `使用 [玉简名]` | 习得玉简对应法决 |
| `查看 [玉简名]` | 展示玉简对应法决的类型、效果、绑定、调息、境界要求与描述 |

---

## 迁移评估：设计取舍

> 原设计文档与实现的差异评估。A 实现现状（文档已按代码修正）；B 保留代码设计（更合乎玩法）；C 按设计修正（设计意图更优，条目标注已修/待修）；D 未实现（待办）；E 缺陷修复。

### A. 实现现状（文档已修正）

- 表名无 `xt_` 前缀：实际为 `skill`、`player_skill`；`skill.name` 为 VARCHAR(64)。
- 效果类型实际 22 种（原 15 + 新增 7），处理已迁移到 `EffectHandlerRegistry`；伤害公式变量为 `attack`/`wis`/`str`/`agi`（含 `atk` 别名），不支持 `defense`/`con`（解析失败回退攻击力）。
- `player_skill` 实际列为 `is_equipped` / `source_sect_id` / `create_time`，无 `equipped` / `source`(JADE/REWARD/INITIAL) / `learned_at`。
- 学习门槛：玉简学习只校验 `level_requirement`（以境界名展示）；`require_wis` 仅在历练顿悟 `findLearnable` 生效；`require_skill_id` 仅用于排除顿悟候选。
- 获取途径为玉简、历练顿悟、宗门共享功法三条；任务/NPC 奖励以玉简物品形式间接实现。
- 槽位装载使用原子条件更新 `equipIfSlotAvailable`，防止并发超额。

### B. 保留代码设计

- **施放为随机选择而非槽位轮播**：可用技能（CD 已好）中随机选一个，避免固定顺序的机械套路，也让装载组合更有变化；与「无优先级」设计一致。
- **冷却按攻速换算**：`冷却回合 = max(1, cooldown_seconds / 攻速)`，攻速成为有实际意义的法器属性（spec 要求二者共同决定），而不是只看法决秒数。
- **未生效不进冷却**：所有效果均未命中/生效时退化为普通攻击且不进 CD，一次未命中不白等整轮冷却，减少挫败。

### C. 按设计修正（待修）

- 无。

### D. 未实现（待办）

- **被动技能与被动效果**：设计意图为 PASSIVE 法决习得即生效、不占槽位，`RESIST_BUFF`/`HP_BUFF`/`SURVIVE_LETHAL` 为被动效果；现状三种效果注册为 no-op，PASSIVE 法决仍需装载占槽并在战斗中作为 buff 施放，「习得即生效、不占槽位」未实现。
- **法决树前置**：设计意图为 `require_skill_id` 串联法决前置（种子描述已有「需要静心诀」等文案）；现状该列未写入种子（全 NULL）、玉简学习不校验，仅「非空即排除顿悟候选」，前置实际未接线。

### E. 缺陷修复

- **WEAPON_CATEGORY 大类绑定全部失效**（已修）：`WeaponType` 新增 `categoryCode()`，绑定过滤按 code（MELEE/POLEARM/RANGED/EXOTIC）比较。
- **无武器时绑定法决未被过滤**（已修）：`DefaultTeamBuilder.loadEquippedSkills` 去掉 `weapon == null ||` 短路，未装备法器时绑定类法决不再进入战斗技能列表。
- **错误提示指向不存在的指令**（已修）：`SkillService.equipSkill` 提示改为「请使用『法决』查看」。
