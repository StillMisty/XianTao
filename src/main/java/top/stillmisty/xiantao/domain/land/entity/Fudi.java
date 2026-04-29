package top.stillmisty.xiantao.domain.land.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.land.enums.BeastQuality;
import top.stillmisty.xiantao.domain.land.enums.EmotionState;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.land.enums.MutationTrait;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 福地核心实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_fudi")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class Fudi extends Model<Fudi> {

    /**
     * 福地唯一ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 所属玩家ID
     */
    private Long userId;

    /**
     * 当前灵气值
     */
    private Integer auraCurrent;

    /**
     * 灵气上限（由劫数和天劫胜利积累）
     */
    private Integer auraMax;

    /**
     * 当前劫数（每渡过一次天劫+1，无上限）
     */
    private Integer tribulationStage;

    /**
     * 地灵MBTI人格类型（锁定）
     */
    private MBTIPersonality mbtiType;

    /**
     * 地灵形态id（关联xt_spirit表）
     */
    private Integer spiritId;

    /**
     * 地灵当前精力值
     */
    private Integer spiritEnergy;

    /**
     * 地灵好感度
     */
    private Integer spiritAffection;

    /**
     * 好感度上限（默认1000）
     */
    private Integer affectionMax;

    /**
     * 地灵当前情绪状态
     */
    private EmotionState emotionState;

    /**
     * 是否开启自动管理模式
     */
    private Boolean autoMode;

    /**
     * 上次灵气计算时间
     */
    private LocalDateTime lastAuraUpdate;

    /**
     * 上次上线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 精力最后更新时间（用于懒恢复计算）
     */
    private LocalDateTime lastEnergyUpdate;

    /**
     * 福地网格布局（JSONB存储）
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> gridLayout;

    /**
     * 地灵配置（JSONB存储人格、表情、形态等）
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> spiritConfig;

    /**
     * 天劫最后发生时间
     */
    private LocalDateTime lastTribulationTime;

    /**
     * 天劫连续胜利次数
     */
    private Integer tribulationWinStreak;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // ===================== 业务逻辑方法 =====================

    /**
     * 计算每小时灵气消耗
     * 公式：已占地块数 × 2 + Σ(灵兽等阶 × 5 × 品质倍率) + Σ(阵眼等级 × 3)
     * 灵气耗尽后所有功能（除献祭外）不可用
     */
    public int calculateHourlyAuraCost() {
        int occupiedCells = getOccupiedCellCount();
        int cellCost = occupiedCells * 2;

        int beastCost = 0;
        int nodeCost = 0;

        if (gridLayout != null && gridLayout.containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) gridLayout.get("cells");
            for (Map<String, Object> cell : cells) {
                String type = (String) cell.get("type");
                if ("pen".equals(type) && cell.containsKey("beast_tier")) {
                    int tier = (Integer) cell.get("beast_tier");
                    double qualityMultiplier = cell.containsKey("quality") ?
                            getPenQualityAuraMultiplier((String) cell.get("quality")) : 1.0;
                    beastCost += (int) (tier * 5 * qualityMultiplier);
                } else if ("node".equals(type) && cell.containsKey("level")) {
                    int level = (Integer) cell.get("level");
                    nodeCost += level * 3;
                }
            }
        }

        return cellCost + beastCost + nodeCost;
    }

    /**
     * 获取已占地块数
     */
    public int getOccupiedCellCount() {
        if (gridLayout == null || !gridLayout.containsKey("cells")) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) gridLayout.get("cells");
        return (int) cells.stream()
                .filter(cell -> !"empty".equals(cell.get("type")))
                .count();
    }

    /**
     * 懒加载计算当前灵气值
     * 根据 (当前时间 - 上次更新时间) × 每小时消耗 计算实际消耗
     * 灵气可降至 0，耗尽后所有功能（除献祭外）不可用
     */
    public int calculateCurrentAura() {
        if (lastAuraUpdate == null) {
            return auraCurrent;
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursElapsed = java.time.Duration.between(lastAuraUpdate, now).toHours();

        if (hoursElapsed <= 0) {
            return auraCurrent;
        }

        int hourlyCost = calculateHourlyAuraCost();
        int totalCost = (int) (hoursElapsed * hourlyCost);
        return Math.max(0, auraCurrent - totalCost);
    }

    /**
     * 更新灵气值（懒加载后保存）
     */
    public void updateAura() {
        auraCurrent = calculateCurrentAura();
        lastAuraUpdate = LocalDateTime.now();
        lastOnlineTime = LocalDateTime.now();
    }

    /**
     * 献祭装备获得灵气
     */
    public int calculateSacrificeAura(int itemBaseValue, double qualityMultiplier, int itemLevel) {
        double result = itemBaseValue * qualityMultiplier * (1 + itemLevel * 0.05);
        return (int) result;
    }

    /**
     * 更新地灵情绪状态（基于灵气、精力、焦土等日常因素，不覆盖天劫）
     */
    public void updateEmotionState() {
        if (spiritEnergy != null && spiritEnergy <= 0) {
            emotionState = EmotionState.FATIGUED;
            return;
        }

        double auraRatio = auraCurrent.doubleValue() / auraMax.doubleValue();
        if (auraRatio < 0.3) {
            emotionState = EmotionState.ANXIOUS;
        } else if (auraRatio > 0.8) {
            emotionState = EmotionState.HAPPY;
        } else {
            emotionState = EmotionState.CALM;
        }
    }

    /**
     * 计算福地防御力（用于天劫结算）
     * 公式：Σ(阵眼耐久) + Σ(灵兽战力×护主加成) + 玩家STR × 10 + 劫数 × 50
     *
     * @param playerStr 玩家力量值
     * @return 福地总防御力
     */
    @SuppressWarnings("unchecked")
    public int calculateTribulationDefense(int playerStr) {
        int defense = playerStr * 10 + (tribulationStage != null ? tribulationStage : 0) * 50;

        if (gridLayout == null || !gridLayout.containsKey("cells")) {
            return defense;
        }

        List<Map<String, Object>> cells = (List<Map<String, Object>>) gridLayout.get("cells");
        for (Map<String, Object> cell : cells) {
            String type = (String) cell.get("type");
            if ("node".equals(type) && cell.containsKey("durability")) {
                defense += ((Number) cell.get("durability")).intValue();
            } else if ("pen".equals(type) && cell.containsKey("power_score")) {
                double power = ((Number) cell.get("power_score")).doubleValue();
                // 护主特性：灵兽携带GUARDIAN变异时战力+50%
                if (cell.containsKey("mutation_traits")) {
                    List<String> traits = (List<String>) cell.get("mutation_traits");
                    if (traits.contains(MutationTrait.GUARDIAN.getCode())) {
                        power *= 1.5;
                    }
                }
                defense += (int) power;
            }
        }

        return defense;
    }

    /**
     * 计算下次天劫触发时间
     */
    public LocalDateTime calculateNextTribulationTime() {
        if (lastTribulationTime == null) {
            return createTime.plusDays(7);
        }
        return lastTribulationTime.plusDays(7);
    }

    /**
     * 获取精力上限（随劫数成长）
     * 公式：100 + 劫数 × 20
     */
    public int getSpiritEnergyMax() {
        int stage = tribulationStage != null ? tribulationStage : 0;
        return 100 + stage * 20;
    }

    /**
     * 懒恢复精力
     * 恢复量 = 精力上限 × 5% × 距上次使用小时数，最多恢复到上限
     */
    public void restoreEnergy() {
        if (lastEnergyUpdate == null || spiritEnergy == null) {
            lastEnergyUpdate = LocalDateTime.now();
            return;
        }

        int maxEnergy = getSpiritEnergyMax();
        if (spiritEnergy >= maxEnergy) return;

        long hoursElapsed = java.time.Duration.between(lastEnergyUpdate, LocalDateTime.now()).toHours();
        if (hoursElapsed <= 0) return;

        int recoveryRate = (int) (maxEnergy * 0.05 * hoursElapsed);
        if (recoveryRate > 0) {
            spiritEnergy = Math.min(maxEnergy, spiritEnergy + recoveryRate);
        }
    }

    /**
     * 计算实际精力消耗（含好感减免）
     * 实际消耗 = 基础消耗 × (1 - min(0.5, affection / 1000))
     */
    public int calculateEnergyConsumption(int baseCost) {
        int affection = spiritAffection != null ? spiritAffection : 0;
        int maxAffection = affectionMax != null ? affectionMax : 1000;
        double discount = Math.min(0.5, (double) affection / maxAffection);
        return (int) Math.max(1, baseCost * (1.0 - discount));
    }

    /**
     * 扣除精力，返回是否耗尽（≤0触发放松）
     */
    public boolean deductEnergy(int cost) {
        if (spiritEnergy == null) spiritEnergy = 0;
        spiritEnergy = Math.max(0, spiritEnergy - cost);
        if (spiritEnergy <= 0) {
            emotionState = EmotionState.FATIGUED;
            return true;
        }
        return false;
    }

    /**
     * 设置好感度（限制在 [0, affectionMax] 范围内）
     */
    public void addAffection(int amount) {
        int maxAffection = affectionMax != null ? affectionMax : 1000;
        spiritAffection = Math.max(0, Math.min(maxAffection,
                (spiritAffection != null ? spiritAffection : 0) + amount));
    }

    /**
     * 从JSONB字段解析灵兽品质灵气消耗倍率
     */
    private static double getPenQualityAuraMultiplier(String qualityCode) {
        try {
            return BeastQuality.fromCode(qualityCode).getAuraCostMultiplier();
        } catch (IllegalArgumentException e) {
            return 1.0;
        }
    }
}
