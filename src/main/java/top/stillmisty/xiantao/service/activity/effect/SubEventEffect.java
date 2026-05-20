package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 子事件效果处理器接口 */
public interface SubEventEffect {

  SubEventEffectType type();

  /** 执行效果，返回叙事模板填充参数 */
  Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context);
}
