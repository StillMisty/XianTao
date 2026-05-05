package top.stillmisty.xiantao.domain.monster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 遇怪计算器 根据地图难度、玩家等级、装备评分动态计算遇怪间隔 */
@Slf4j
@Component
public class EncounterCalculator {

  /** 基础遇怪间隔（分钟） */
  private static final double BASE_INTERVAL = 10.0;

  /** 地图难度因子系数（最高0.5） */
  private static final double DIFFICULTY_FACTOR_COEFFICIENT = 0.005;

  /** 玩家等级因子系数（最高0.3） */
  private static final double LEVEL_FACTOR_COEFFICIENT = 0.0015;

  /** 装备评分因子系数（最高0.2） */
  private static final double GEAR_FACTOR_COEFFICIENT = 0.0002;

  /** 最小遇怪间隔（分钟） */
  private static final double MIN_INTERVAL = 2.0;

  /**
   * 计算动态遇怪间隔
   *
   * @param mapLevel 地图等级
   * @param playerLevel 玩家等级
   * @param gearScore 装备评分
   * @return 遇怪间隔（分钟）
   */
  public double calculateInterval(int mapLevel, int playerLevel, int gearScore) {
    // 地图难度因子：地图等级越高，遇怪越频繁
    double difficultyFactor = Math.min(0.5, mapLevel * DIFFICULTY_FACTOR_COEFFICIENT);

    // 玩家等级因子：玩家等级越高，遇怪越频繁
    double levelFactor = Math.min(0.3, playerLevel * LEVEL_FACTOR_COEFFICIENT);

    // 装备评分因子：装备越好，遇怪越少（灵压威慑）
    double gearFactor = Math.min(0.15, gearScore * GEAR_FACTOR_COEFFICIENT);

    // 计算动态间隔
    double interval = BASE_INTERVAL * (1 - difficultyFactor) * (1 - levelFactor) * (1 + gearFactor);

    // 确保最小间隔
    interval = Math.max(MIN_INTERVAL, interval);

    log.debug(
        "遇怪间隔计算 - 地图等级: {}, 玩家等级: {}, 装备评分: {}, 间隔: {}分钟",
        mapLevel,
        playerLevel,
        gearScore,
        String.format("%.1f", interval));

    return interval;
  }

  /**
   * 计算遇怪次数
   *
   * @param durationMinutes 历练时长（分钟）
   * @param interval 遇怪间隔（分钟）
   * @return 遇怪次数
   */
  public int calculateEncounterChances(int durationMinutes, double interval) {
    return (int) (durationMinutes / interval);
  }

  /**
   * 计算遇怪概率（基于间隔） 间隔越短，概率越高
   *
   * @param interval 遇怪间隔（分钟）
   * @return 遇怪概率（0.0-1.0）
   */
  public double calculateEncounterChance(double interval) {
    // 基础概率40%，间隔越短概率越高
    double baseChance = 0.4;
    double intervalFactor = BASE_INTERVAL / interval;
    return Math.min(1.0, baseChance * intervalFactor);
  }
}
