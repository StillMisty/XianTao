package top.stillmisty.xiantao.service.combat;

/** 历练收益公式 — 纯函数，供结算与验证共用。 */
final class TrainingRates {

  /** 单次结算参与物品掉落判定的最大分钟数（12 小时），超出部分不参与判定。 */
  static final long MAX_SETTLEMENT_MINUTES = 720;

  static final double AGILITY_EFFICIENCY_COEFFICIENT = 0.01;
  static final double MAX_EFFICIENCY_BOOST = 2.0;
  static final double LEVEL_DECAY_RATE = 0.04;
  static final double MIN_LEVEL_DECAY_MULTIPLIER = 0.1;
  static final int LEVEL_DECAY_OFFSET = 5;

  private TrainingRates() {}

  /** 身法效率：1 + 身法 × 1%，上限 3.0。 */
  static double efficiencyMultiplier(int agility) {
    return 1.0 + Math.min(agility * AGILITY_EFFICIENCY_COEFFICIENT, MAX_EFFICIENCY_BOOST);
  }

  /** 等级衰减：高于地图等级 5 级后每级 -4%，下限 0.1。 */
  static double levelDecayMultiplier(int playerLevel, int mapLevel) {
    int levelDiff = playerLevel - mapLevel - LEVEL_DECAY_OFFSET;
    if (levelDiff <= 0) return 1.0;
    return Math.max(MIN_LEVEL_DECAY_MULTIPLIER, 1.0 - levelDiff * LEVEL_DECAY_RATE);
  }

  /** 基础修为/分钟 = max(地图等级 × 5, √有效悟性 × 12)。 */
  static long baseExpPerMinute(int mapLevelRequirement, int effectiveWis) {
    return Math.max(mapLevelRequirement * 5L, (long) (Math.sqrt(effectiveWis) * 12));
  }

  /** 单次结算参与物品掉落判定的分钟数（封顶 12 小时）。 */
  static long settlementMinutes(long minutes) {
    return Math.min(minutes, MAX_SETTLEMENT_MINUTES);
  }
}
