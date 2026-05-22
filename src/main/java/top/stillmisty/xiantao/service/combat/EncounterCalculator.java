package top.stillmisty.xiantao.service.combat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 遇怪/事件计算器 — 根据地图密集度、等级匹配度动态计算触发间隔 */
@Slf4j
@Component
public class EncounterCalculator {

  /** 基准间隔（分钟） */
  private static final double BASE_INTERVAL = 10.0;

  /** 地图凶险系数 */
  private static final double MAP_DANGER_COEFFICIENT = 0.015;

  /** 等级偏差惩罚系数 */
  private static final double LEVEL_MISMATCH_COEFFICIENT = 2.5;

  /** 最小间隔 */
  private static final double MIN_INTERVAL = 3.0;

  /** 最大间隔 */
  private static final double MAX_INTERVAL = 20.0;

  public record EncounterParams(int slots, double perRollChance) {}

  public EncounterParams compute(Long userId, User user, MapNode mapNode, int durationMinutes) {
    double interval = calculateInterval(user, mapNode);
    int slots = (int) (durationMinutes / interval);
    double perRollChance = Math.min(1.0, 0.4 * BASE_INTERVAL / interval);
    return new EncounterParams(slots, perRollChance);
  }

  double calculateInterval(User user, MapNode mapNode) {
    int mapLevel = mapNode.getLevelRequirement() != null ? mapNode.getLevelRequirement() : 1;
    int playerLevel = user.getLevel();
    int richness = mapNode.getEncounterRichness() != null ? mapNode.getEncounterRichness() : 5;

    double mapDanger = 1.0 + (mapLevel - 1) * MAP_DANGER_COEFFICIENT;
    int levelDelta = Math.abs(playerLevel - mapLevel);
    double levelMismatch =
        1.0 + levelDelta / (double) Math.max(mapLevel, 1) * LEVEL_MISMATCH_COEFFICIENT;
    double baseInterval = 12.0 - richness;

    double interval = baseInterval * levelMismatch / mapDanger;
    interval = Math.max(MIN_INTERVAL, Math.min(MAX_INTERVAL, interval));

    log.debug(
        "遇怪间隔计算 - 地图Lv{} 富裕度{} 玩家Lv{} → 间隔={}分钟",
        mapLevel,
        richness,
        playerLevel,
        String.format("%.1f", interval));

    return interval;
  }
}
