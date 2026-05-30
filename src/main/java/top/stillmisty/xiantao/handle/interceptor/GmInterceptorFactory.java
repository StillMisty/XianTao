package top.stillmisty.xiantao.handle.interceptor;

import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.JBlockEventInterceptor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.interceptor.AnnotationEventInterceptorFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.UserContext;

/** GM权限拦截器工厂 用于在事件监听层面统一处理GM权限验证 */
@Component
public class GmInterceptorFactory implements AnnotationEventInterceptorFactory {

  private final UserRepository userRepository;

  public GmInterceptorFactory(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public @Nullable Result create(Context context) {
    return Result.build(
        config -> {
          config.interceptor(new GmInterceptor(userRepository));
          config.configuration(properties -> properties.setPriority(context.getPriority()));
        });
  }

  /** GM权限拦截器实现 */
  private static class GmInterceptor implements JBlockEventInterceptor {

    private final UserRepository userRepository;

    private GmInterceptor(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
    public EventResult intercept(JBlockEventInterceptor.Context context) throws Exception {
      var event = context.getSource().getEventListenerContext().getEvent();
      if (!(event instanceof MessageEvent)) {
        return context.invoke();
      }

      // AuthInterceptor 已将 userId 存入事件映射（原始线程），此处直接读取（IO 线程）
      Long userId = UserContext.retrieveFromEvent(event);
      if (userId == null) {
        return EventResult.of("AUTH_ERROR: 未登录");
      }

      boolean isGm =
          userRepository.findById(userId).map(u -> Boolean.TRUE.equals(u.getGm())).orElse(false);

      if (!isGm) {
        return EventResult.of("BUSINESS_ERROR: 你不是GM，无法执行GM指令");
      }

      return context.invoke();
    }
  }
}
