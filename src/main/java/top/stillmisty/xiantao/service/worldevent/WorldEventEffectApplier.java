package top.stillmisty.xiantao.service.worldevent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.service.activity.SubEventEffectExecutor;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldEventEffectApplier {

  private final SubEventEffectExecutor subEventEffectExecutor;

  public Map<String, Object> applyEffects(WorldEvent event, User user) {
    if (!event.hasEffects()) return Map.of();
    try {
      return applyFlatEffects(event.getEffects(), user.getId(), user);
    } catch (Exception e) {
      log.warn(
          "应用世界事件效果失败 - eventId: {}, userId: {}, error: {}",
          event.getId(),
          user.getId(),
          e.getMessage());
      return Map.of();
    }
  }

  public Map<String, Object> applyEffectsFromConfig(
      List<Map<String, Object>> effectConfigs, Long userId, User user) {
    if (effectConfigs == null || effectConfigs.isEmpty()) return Map.of();
    try {
      Map<String, Object> allResults = new HashMap<>();
      for (Map<String, Object> config : effectConfigs) {
        Map<String, Object> result;
        if (config.containsKey("branches")) {
          result = applyBranches(config, userId, user);
        } else {
          result = applyFlatItem(config, userId, user);
        }
        if (result != null) {
          allResults.putAll(result);
        }
      }
      return allResults;
    } catch (Exception e) {
      log.warn("应用效果失败 - userId: {}, error: {}", userId, e.getMessage());
      return Map.of();
    }
  }

  /** 处理平铺效果列表 (容器含 effects 字段) */
  private Map<String, Object> applyFlatEffects(
      List<Map<String, Object>> effects, Long userId, User user) {
    return subEventEffectExecutor.executeEffects(effects, userId, user, EventContext.empty());
  }

  /** 处理单个平铺效果项 (项内直接含 type 字段) */
  @SuppressWarnings("unchecked")
  private Map<String, Object> applyFlatItem(Map<String, Object> config, Long userId, User user) {
    List<Map<String, Object>> effectList = (List<Map<String, Object>>) config.get("effects");
    if (effectList != null && !effectList.isEmpty()) {
      return applyFlatEffects(effectList, userId, user);
    }
    return applyFlatEffects(List.of(config), userId, user);
  }

  /** 处理分支随机效果 */
  @SuppressWarnings("unchecked")
  private Map<String, Object> applyBranches(Map<String, Object> config, Long userId, User user) {
    List<Map<String, Object>> branches = (List<Map<String, Object>>) config.get("branches");
    if (branches == null || branches.isEmpty()) return Map.of();

    double roll = ThreadLocalRandom.current().nextDouble();
    double cumulative = 0;
    for (Map<String, Object> branch : branches) {
      Object chanceObj = branch.get("chance");
      double chance = chanceObj instanceof Number n ? n.doubleValue() : 0;
      cumulative += chance;
      if (roll < cumulative || Math.abs(cumulative - 1.0) < 1e-9) {
        List<Map<String, Object>> effects = (List<Map<String, Object>>) branch.get("effects");
        if (effects != null && !effects.isEmpty()) {
          return applyFlatEffects(effects, userId, user);
        }
        return Map.of();
      }
    }
    return Map.of();
  }
}
