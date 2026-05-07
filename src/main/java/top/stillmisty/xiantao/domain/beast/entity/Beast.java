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
    if (recoveryUntil != null && recoveryUntil.isAfter(LocalDateTime.now())) return false;
    // recoveryUntil已过期，清除恢复状态
    if (recoveryUntil != null && !recoveryUntil.isAfter(LocalDateTime.now())) {
      recoveryUntil = null;
    }
    return true;
  }

  /** 计算升级所需经验 公式：50 × level^1.5 */
  public long calculateExpToNextLevel() {
    return (long) (50 * Math.pow(level, 1.5));
  }

  /** 检查是否可以升级 */
  public boolean canLevelUp() {
    return levelCap != null && level < levelCap && exp >= (int) calculateExpToNextLevel();
  }

  /** 升级 */
  public void levelUp() {
    if (!canLevelUp()) {
      return;
    }
    exp -= (int) calculateExpToNextLevel();
    level++;
    recalculateAttributes();
  }

  /**
   * 添加经验值
   *
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
    if (levelCap != null && level >= levelCap) {
      long maxExp = calculateExpToNextLevel();
      exp = (int) Math.min(exp + actualAdd, maxExp);
      actualAdd = 0;
    }
    return expToAdd - actualAdd;
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

  /** 计算速度 公式：level × 2 + 8 */
  public int calculateSpeed() {
    return level * 2 + 8;
  }

  /** 检查是否还需要提升等级才能进化 */
  public boolean needsMoreLevels() {
    return levelCap == null || level < levelCap;
  }

  /** 进化（升阶） */
  public void evolve() {
    tier++;
    levelCap = tier * 10 + 10;
    evolutionCount++;
    recalculateAttributes();
  }

  /** 品质突破（升品） */
  public void qualityBreak() {
    if (quality != BeastQuality.DIVINE) {
      quality = quality.next();
    }
  }
}
