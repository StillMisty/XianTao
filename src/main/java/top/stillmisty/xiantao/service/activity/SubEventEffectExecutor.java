package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.activity.effect.SubEventEffect;
import top.stillmisty.xiantao.service.activity.effect.SubEventEffectType;

/** 子事件效果执行引擎 — 读 params JSONB，分发到对应 effect handler */
@Slf4j
@Component
public class SubEventEffectExecutor {

  private final Map<SubEventEffectType, SubEventEffect> handlerMap;

  public SubEventEffectExecutor(List<SubEventEffect> effectHandlers) {
    this.handlerMap =
        effectHandlers.stream()
            .collect(Collectors.toMap(SubEventEffect::type, Function.identity()));
  }

  /** 执行一个 ActivityEvent 的所有效果，返回叙事模板参数 */
  public Map<String, Object> execute(
      ActivityEvent event, Long userId, User user, EventContext context) {
    EffectData.ActivityConfig config = event.effectData();
    if (config.branches() != null) {
      return executeBranches(config.branches(), userId, user, context);
    }
    if (config.effects() != null) {
      return executeEffects(config.effects(), userId, user, context);
    }
    return Map.of();
  }

  private Map<String, Object> executeBranches(
      List<Map<String, Object>> branches, Long userId, User user, EventContext context) {
    if (branches.isEmpty()) return Map.of();

    double roll = ThreadLocalRandom.current().nextDouble();
    double cumulative = 0;
    for (Map<String, Object> branch : branches) {
      Object chanceObj = branch.get("chance");
      double chance = chanceObj instanceof Number n ? n.doubleValue() : 0;
      cumulative += chance;
      if (roll < cumulative || Math.abs(cumulative - 1.0) < 1e-9) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> effects = (List<Map<String, Object>>) branch.get("effects");
        if (effects != null) {
          return executeEffects(effects, userId, user, context);
        }
        return Map.of();
      }
    }
    return Map.of();
  }

  /** 执行效果列表，返回叙事模板参数 */
  public Map<String, Object> executeEffects(
      List<Map<String, Object>> effectList, Long userId, User user, EventContext context) {
    if (effectList == null || effectList.isEmpty()) return Map.of();

    Map<String, Object> templateArgs = new HashMap<>();
    for (Map<String, Object> effectConfig : effectList) {
      String typeStr = (String) effectConfig.get("type");
      if (typeStr == null) continue;
      SubEventEffectType effectType = SubEventEffectType.fromCode(typeStr);
      if (effectType == null) {
        log.warn("Unknown sub-event effect type: {}", typeStr);
        continue;
      }
      SubEventEffect handler = handlerMap.get(effectType);
      if (handler == null) {
        log.warn("No handler registered for effect type: {}", effectType);
        continue;
      }
      Map<String, Object> result = handler.execute(userId, user, effectConfig, context);
      if (result != null) {
        templateArgs.putAll(result);
      }
    }
    return templateArgs;
  }
}
