package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 子事件效果处理器接口 */
public interface SubEventEffect {

  SubEventEffectType type();

  /** 执行效果，返回叙事模板填充参数（旧接口，保留向后兼容） */
  default Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, EventContext context) {
    EffectParams typedParams = EffectParams.fromMap(type(), params);
    return execute(userId, user, typedParams, context);
  }

  /** 执行效果，返回叙事模板填充参数（类型安全版本） */
  Map<String, Object> execute(Long userId, User user, EffectParams params, EventContext context);
}
