# 数值测试与调参工具

长线挂机游戏的进度节奏验证与内容生成。设计决策见 `openspec/adr/ADR-0005-progression-pacing.md`。

## 工具

```bash
python3 tools/balance/progression.py                    # 打印 1~110 级进度曲线
python3 tools/balance/progression.py --json out.json    # 输出完整数据
python3 tools/balance/progression.py --brief            # 只输出里程碑累计时间
python3 tools/balance/progression.py --set exp_coeff=300,exp_exp=2.3   # 参数覆盖（实验用）
python3 tools/balance/gen_high_level_content.py         # 重新生成 100+ 内容迁移
```

`progression.py` 直接读取本地数据库（`application-local.yml` 的连接信息）中的地图/怪物/遭遇种子，
按代码公式模拟「最优挂机玩家」每级耗时。

## 模型口径（与代码对齐）

| 项 | 公式 | 代码位置 |
|---|---|---|
| 修为需求 | `240 × 等级^2.2`（四舍五入） | `Player.calculateExpToNextLevel` |
| 存量修为 | 总修为 − 上一级需求；存储上限 = 需求 × 5 | `Player.getExpInCurrentLevel` |
| 突破成功率 | `100/(1+(等级/65)^4)` + 失败补偿（每次 +5%~25%，随等级衰减） | `Player.calculateBreakthroughSuccessRate` |
| 突破消耗 | 每次尝试消耗一份满需求（成功/失败同价） | `CultivationService` |
| 历练收益/分 | `max(地图等级×5, √悟性×12) × (1+身法×1%，上限3.0) × 等级衰减` | `TrainingRates` |
| 等级衰减 | 高于地图等级 5 级后每级 −4%，下限 0.1 | `TrainingRates.levelDecayMultiplier` |
| 战斗收益 | `Σ exp_reward × 数量 × (1+(怪级−玩家级)×5%, 钳制[0.1,3])` | `CombatEventHandler` |
| 遭遇频率 | 间隔 `(12−丰富度) × 等级偏差 / 地图凶险`，钳制 [3,20] 分钟 | `EncounterCalculator` |
| 属性成长 | 初始四维 5；有效值 = 存储 + 4 + 等级；大境界突破存储 +20%（复利） | `Player` / `CultivationService` |

模拟未计入装备、灵兽、丹药、师徒/宗门/运势加成——全部为收益项，实际进度快于基线（作为保守下界）。

## 标定目标与当前锚点

目标：**无装备基线约 6 个月到大乘圆满（111 级）**，开局简单、后期每级 1~2 天。

| 等级 | 累计天数（基线） |
|---|---|
| 10（炼气圆满） | 1.7 |
| 21（筑基·开光） | 11.0 |
| 31（金丹·凝丹） | 23.9 |
| 41（元婴·孕婴） | 45.7 |
| 56（炼虚·初虚） | 73.7 |
| 71（合体·开光） | 109.2 |
| 91（大乘·凝丹） | 139.6 |
| 111（渡劫·一劫） | 174.6 |

## 100+ 内容生成

`gen_high_level_content.py` 从数据库读取参考数据（掉落表/技能/物品 id），生成
`V1.0.57__seed_high_level_content.sql`：12 种怪物（102~125 级）、9 张地图（102~122，含 1 安全城 + 1 隐藏秘境）、
17 条遭遇池，并把新区域接入既有顶级地图（42/43/44 双向连通）。

新增内容或调整数值后，先跑 `progression.py` 看里程碑，再构建并重启应用应用迁移。

## 后续调参候选

- 41-56、71-91 段存在每级耗时回落（内容边界收入跃升所致），可微调中间地图怪物经验；
- 102 级新区域带来一次收入跃升，可压低新怪物经验使其衔接更平滑；
- 突破成功率后期约 8~11%，依赖失败补偿；如体感过苦可上调补偿上限。
