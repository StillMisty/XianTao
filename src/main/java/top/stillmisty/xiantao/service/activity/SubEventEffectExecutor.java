package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.activity.effect.SubEventEffect;

/** 子事件效果执行引擎 — 读 params JSONB，分发到对应 effect handler */
@Component
@RequiredArgsConstructor
public class SubEventEffectExecutor {

  private final List<SubEventEffect> effectHandlers;

  /** 执行一个 ActivityEvent 的所有效果，返回叙事模板参数 */
  public Map<String, Object> execute(
      ActivityEvent event, Long userId, User user, Map<String, Object> context) {
    Map<String, Object> params = event.getParams();
    if (params == null) return Map.of();

    if (params.containsKey("branches")) {
      return executeBranches(params, userId, user, context);
    }
    return executeEffects(params, userId, user, context);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> executeBranches(
      Map<String, Object> params, Long userId, User user, Map<String, Object> context) {
    List<Map<String, Object>> branches = (List<Map<String, Object>>) params.get("branches");
    if (branches == null || branches.isEmpty()) return Map.of();

    double roll = ThreadLocalRandom.current().nextDouble();
    double cumulative = 0;
    for (Map<String, Object> branch : branches) {
      double chance = ((Number) branch.get("chance")).doubleValue();
      cumulative += chance;
      if (roll < cumulative || Math.abs(cumulative - 1.0) < 1e-9) {
        return executeEffects(branch, userId, user, context);
      }
    }
    return Map.of();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> executeEffects(
      Map<String, Object> container, Long userId, User user, Map<String, Object> context) {
    List<Map<String, Object>> effectList = (List<Map<String, Object>>) container.get("effects");
    if (effectList == null || effectList.isEmpty()) return Map.of();

    Map<String, Object> templateArgs = new HashMap<>();
    for (Map<String, Object> effectConfig : effectList) {
      String typeStr = (String) effectConfig.get("type");
      if (typeStr == null) continue;
      for (SubEventEffect handler : effectHandlers) {
        if (handler.type().name().equals(typeStr)) {
          Map<String, Object> result = handler.execute(userId, user, effectConfig, context);
          if (result != null) {
            templateArgs.putAll(result);
          }
          break;
        }
      }
    }
    return templateArgs;
  }
}
