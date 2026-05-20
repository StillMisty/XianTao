package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(chain = true)
@Table("xt_beast")
public class Beast {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private Long fudiId;

  private Long templateId;

  private String beastName;

  private Integer tier;

  private BeastQuality quality;

  private Boolean isMutant;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<String> mutationTraits;

  private Integer level;

  private Integer exp;

  private Integer attack;

  private Integer defense;

  private Integer maxHp;

  private Integer hpCurrent;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<Long> skills;

  private Boolean isDeployed;

  private LocalDateTime recoveryUntil;

  private Integer pennedCellId;

  private LocalDateTime birthTime;

  private Integer evolutionCount;

  private Integer levelCap;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()")
  private LocalDateTime updateTime;

  public boolean needsRecovery() {
    return hpCurrent != null && maxHp != null && hpCurrent < maxHp;
  }

  public boolean canFight() {
    if (!Boolean.TRUE.equals(isDeployed)) return false;
    if (hpCurrent == null || hpCurrent <= 0) return false;
    return recoveryUntil == null || !recoveryUntil.isAfter(LocalDateTime.now());
  }

  /** 计算升级所需经验 公式：50 × level^1.5 */
  public long calculateExpToNextLevel() {
    if (level == null || level <= 0) return 50;
    return (long) (50 * Math.pow(level, 1.5));
  }

  /** 检查是否可以升级 */
  public boolean canLevelUp() {
    return levelCap != null && level < levelCap && exp >= (int) calculateExpToNextLevel();
  }

  /**
   * 添加经验值
   *
   * @param expToAdd 要添加的经验值
   * @return 实际吸收的经验值
   */
  public long addExp(long expToAdd) {
    if (expToAdd <= 0) return 0;

    if (levelCap != null && level >= levelCap) {
      long neededToFill = Math.max(0, calculateExpToNextLevel() - exp);
      long actualAdd = Math.min(expToAdd, neededToFill);
      exp += (int) actualAdd;
      return actualAdd;
    }

    exp += (int) Math.min(expToAdd, Integer.MAX_VALUE);
    long consumed = expToAdd;

    while (levelCap != null
        && level < levelCap
        && calculateExpToNextLevel() > 0
        && exp >= (int) calculateExpToNextLevel()) {
      exp -= (int) calculateExpToNextLevel();
      level++;
      recalculateAttributes();
    }

    if (levelCap != null && level >= levelCap) {
      long overflow = exp - calculateExpToNextLevel();
      if (overflow > 0) {
        consumed -= overflow;
      }
      exp = Math.min(exp, (int) calculateExpToNextLevel());
    }

    return consumed;
  }

  /** 重新计算属性（升级、进化后调用） 使用与 FudiService.calculateBeastAttack/Defense 一致的公式 */
  public void recalculateAttributes() {
    if (level != null) {
      double q = getQualityMultiplier();
      attack = (int) Math.round((10 + (level - 1) * 3 * q) * q);
      defense = (int) Math.round((8 + (level - 1) * 2 * q) * q);
    }
    if (tier != null && level != null) {
      maxHp = tier * 200 + (level - 1) * tier * 20;
      if (hpCurrent != null && hpCurrent > maxHp) {
        hpCurrent = maxHp;
      }
    }
  }

  /** 获取品质属性倍率 */
  public double getQualityMultiplier() {
    return switch (quality) {
      case SPIRIT -> 1.0;
      case IMMORTAL -> 1.3;
      case SAINT -> 1.6;
      case DIVINE -> 2.0;
      default -> 0.8;
    };
  }

  /** 检查是否还需要提升等级才能进化 */
  public boolean needsMoreLevels() {
    return levelCap == null || level < levelCap;
  }

  /** 进化（升阶），品质有10%概率连带提升 */
  public void evolve() {
    tier++;
    levelCap = tier * 10 + 10;
    evolutionCount++;
    recalculateAttributes();
  }

  /** 获取等阶修仙名称 */
  public static String getTierName(int tier) {
    return switch (tier) {
      case 1 -> "通灵";
      case 2 -> "凝魄";
      case 3 -> "化形";
      case 4 -> "渡劫";
      case 5 -> "归真";
      default -> "T" + tier;
    };
  }

  /** 获取品质修仙名称 */
  public String getQualityName() {
    return quality != null ? quality.getChineseName() : "凡品";
  }
}
