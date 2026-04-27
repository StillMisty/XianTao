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
     * 福地网格大小（3/4/5）
     */
    private Integer gridSize;

    /**
     * 地灵MBTI人格类型（锁定）
     */
    private MBTIPersonality mbtiType;

    /**
     * 地灵当前精力值（0-100）
     */
    private Integer spiritEnergy;

    /**
     * 地灵好感度
     */
    private Integer spiritAffection;

    /**
     * 地灵当前情绪状态
     */
    private EmotionState emotionState;

    /**
     * 是否开启自动管理模式
     */
    private Boolean autoMode;

    /**
     * 是否处于蛰伏模式（离线保底）
     */
    private Boolean dormantMode;

    /**
     * 上次灵气计算时间
     */
    private LocalDateTime lastAuraUpdate;

    /**
     * 上次上线时间
     */
    private LocalDateTime lastOnlineTime;

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
     * 公式：基础消耗(10) + 已占地块数 × 2 + Σ(灵兽等阶 × 5) + Σ(阵眼等级 × 3)
     */
    public int calculateHourlyAuraCost() {
        int baseCost = 10;
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

        return baseCost + cellCost + beastCost + nodeCost;
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

        int hourlyCost;
        if (Boolean.TRUE.equals(dormantMode)) {
            // 蛰伏模式：最低保护 1 灵气/小时
            hourlyCost = 1;
        } else {
            hourlyCost = calculateHourlyAuraCost();
        }

        int totalCost = (int) (hoursElapsed * hourlyCost);
        int newAura = Math.max(0, auraCurrent - totalCost);

        // 检查是否触发蛰伏模式
        if (!Boolean.TRUE.equals(dormantMode) && newAura < auraMax * 0.1) {
            // 离线超过 24 小时且灵气不足 10%，触发蛰伏
            long offlineHours = java.time.Duration.between(lastOnlineTime, now).toHours();
            if (offlineHours >= 24) {
                dormantMode = true;
                hourlyCost = 1;
                totalCost = (int) (hoursElapsed * hourlyCost);
                newAura = Math.max(0, auraCurrent - totalCost);
            }
        }

        return newAura;
    }

    /**
     * 更新灵气值（懒加载后保存）
     */
    public void updateAura() {
        auraCurrent = calculateCurrentAura();
        lastAuraUpdate = LocalDateTime.now();
        lastOnlineTime = LocalDateTime.now();

        // 上线后自动解除蛰伏
        if (Boolean.TRUE.equals(dormantMode)) {
            dormantMode = false;
        }
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
