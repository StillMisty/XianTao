package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.beast.enums.BeastGender;
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

  private BeastGender gender;

  private Integer tier;

  private BeastQuality quality;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<Long> mutationTraits;

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

  private LocalDateTime breedingCooldownUntil;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()")
  private LocalDateTime updateTime;

  public boolean isMutant() {
    return mutationTraits != null && !mutationTraits.isEmpty();
  }

  public int getLevelCap() {
    return tier != null ? tier * 10 + 10 : 20;
  }

  public boolean needsRecovery() {
    return hpCurrent != null && maxHp != null && hpCurrent < maxHp;
  }

  public boolean canFight() {
    if (!Boolean.TRUE.equals(isDeployed)) return false;
    if (hpCurrent == null || hpCurrent <= 0) return false;
    return recoveryUntil == null || !recoveryUntil.isAfter(LocalDateTime.now());
  }

  public boolean canBreed() {
    return breedingCooldownUntil == null || !breedingCooldownUntil.isAfter(LocalDateTime.now());
  }

  public boolean isAdult() {
    return tier != null && tier >= 2;
  }

  /** 兽栏内自动回血：每 5 分钟恢复 1% 最大 HP（仅未出战且未满血时） */
  public boolean tryAutoHeal() {
    if (Boolean.TRUE.equals(isDeployed)) return false;
    if (hpCurrent == null || maxHp == null || hpCurrent >= maxHp) return false;
    int heal = Math.max(1, maxHp / 100);
    hpCurrent = Math.min(maxHp, hpCurrent + heal);
    return true;
  }

  public long calculateExpToNextLevel() {
    if (level == null || level <= 0) return 50;
    return (long) (50 * Math.pow(level, 1.5));
  }

  public boolean canLevelUp() {
    return level < getLevelCap() && exp >= (int) calculateExpToNextLevel();
  }

  public long addExp(long expToAdd) {
    if (expToAdd <= 0) return 0;
    int cap = getLevelCap();

    if (level >= cap) {
      long neededToFill = Math.max(0, calculateExpToNextLevel() - exp);
      long actualAdd = Math.min(expToAdd, neededToFill);
      exp += (int) actualAdd;
      return actualAdd;
    }

    exp += (int) Math.min(expToAdd, Integer.MAX_VALUE);
    long consumed = expToAdd;

    while (level < cap && calculateExpToNextLevel() > 0 && exp >= (int) calculateExpToNextLevel()) {
      exp -= (int) calculateExpToNextLevel();
      level++;
      recalculateAttributes();
    }

    if (level >= cap) {
      long overflow = exp - calculateExpToNextLevel();
      if (overflow > 0) consumed -= overflow;
      exp = Math.min(exp, (int) calculateExpToNextLevel());
    }

    return consumed;
  }

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

  public double getQualityMultiplier() {
    return switch (quality) {
      case SPIRIT -> 1.0;
      case IMMORTAL -> 1.3;
      case SAINT -> 1.6;
      case DIVINE -> 2.0;
      default -> 0.8;
    };
  }

  public boolean needsMoreLevels() {
    return level < getLevelCap();
  }

  public void evolve() {
    tier++;
    recalculateAttributes();
  }

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

  public String getQualityName() {
    return quality != null ? quality.getChineseName() : "凡品";
  }
}
