package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("xt_beast")
public class Beast {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long fudiId;

    private Long templateId;

    private String beastName;

    private Integer tier;

    private String quality;

    private Boolean isMutant;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<String> mutationTraits;

    private Integer level;

    private Integer exp;

    private Integer attack;

    private Integer defense;

    private Integer maxHp;

    private Integer hpCurrent;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Long> skills;

    private Boolean isDeployed;

    private LocalDateTime recoveryUntil;

    private Integer pennedCellId;

    private LocalDateTime birthTime;

    private Integer evolutionCount;

    private Integer levelCap;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public boolean needsRecovery() {
        return hpCurrent != null && maxHp != null && hpCurrent < maxHp;
    }

    public boolean canFight() {
        return Boolean.TRUE.equals(isDeployed) && hpCurrent != null && hpCurrent > 0;
    }

    /**
     * 计算升级所需经验
     * 公式：50 × level^1.5
     */
    public long calculateExpToNextLevel() {
        return (long) (50 * Math.pow(level, 1.5));
    }

    /**
     * 检查是否可以升级
     */
    public boolean canLevelUp() {
        return level < levelCap && exp >= (int) calculateExpToNextLevel();
    }

    /**
     * 升级
     * @return 是否升级成功
     */
    public boolean levelUp() {
        if (!canLevelUp()) {
            return false;
        }
        exp -= (int) calculateExpToNextLevel();
        level++;
        recalculateAttributes();
        return true;
    }

    /**
     * 添加经验值
     * @param expToAdd 要添加的经验值
     * @return 实际添加的经验值
     */
    public long addExp(long expToAdd) {
        long actualAdd = expToAdd;
        // 如果升级后还会继续升级，需要计算实际能添加的经验
        while (actualAdd > 0 && canLevelUp()) {
            long needed = (int) calculateExpToNextLevel() - exp;
            if (actualAdd >= needed) {
                exp += (int) needed;
                actualAdd -= needed;
                levelUp();
            } else {
                exp += (int) actualAdd;
                actualAdd = 0;
            }
        }
        // 如果不能继续升级，经验存到上限
        if (level >= levelCap) {
            long maxExp = calculateExpToNextLevel();
            exp = (int) Math.min(exp + actualAdd, maxExp);
            actualAdd = 0;
        }
        return expToAdd - actualAdd;
    }

    /**
     * 重新计算属性（升级、进化后调用）
     */
    public void recalculateAttributes() {
        // 攻击 = base_attack × (1 + 0.05 × (level - 1))
        // 防御 = base_defense × (1 + 0.05 × (level - 1))
        // HP最大值 = tier × 200 + (level - 1) × tier × 20
        // 这里需要从模板获取base_attack和base_defense，暂时使用当前值作为基础
        // 实际应该从模板获取，这里简化处理
        if (attack != null && level != null) {
            // 假设base_attack是level=1时的攻击
            int baseAttack = attack / (1 + (int) (0.05 * (level - 1)));
            attack = (int) (baseAttack * (1 + 0.05 * (level - 1)));
        }
        if (defense != null && level != null) {
            int baseDefense = defense / (1 + (int) (0.05 * (level - 1)));
            defense = (int) (baseDefense * (1 + 0.05 * (level - 1)));
        }
        if (tier != null && level != null) {
            maxHp = tier * 200 + (level - 1) * tier * 20;
            // 确保当前HP不超过最大HP
            if (hpCurrent != null && hpCurrent > maxHp) {
                hpCurrent = maxHp;
            }
        }
    }

    /**
     * 计算速度
     * 公式：level × 2 + 8
     */
    public int calculateSpeed() {
        return level * 2 + 8;
    }

    /**
     * 检查是否可以进化
     * @param isQualityBreak 是否是品质突破
     */
    public boolean canEvolve(boolean isQualityBreak) {
        if (isQualityBreak) {
            // 品质突破需要达到等级上限
            return level >= levelCap;
        } else {
            // 升阶需要达到等级上限
            return level >= levelCap;
        }
    }

    /**
     * 进化（升阶）
     */
    public void evolve() {
        tier++;
        levelCap = tier * 10 + 10;
        evolutionCount++;
        recalculateAttributes();
    }

    /**
     * 品质突破（升品）
     */
    public void qualityBreak() {
        // 品质提升逻辑，需要根据当前品质提升到下一级
        // 这里简化处理，实际应该有品质枚举
        if ("mortal".equals(quality)) {
            quality = "spirit";
        } else if ("spirit".equals(quality)) {
            quality = "immortal";
        } else if ("immortal".equals(quality)) {
            quality = "saint";
        } else if ("saint".equals(quality)) {
            quality = "divine";
        }
    }
}
