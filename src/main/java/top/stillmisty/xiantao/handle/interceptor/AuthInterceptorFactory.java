package top.stillmisty.xiantao.handle.interceptor;

import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.JBlockEventInterceptor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.interceptor.AnnotationEventInterceptorFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;

/** 认证拦截器工厂 用于在事件监听层面统一处理认证，消除Service层的双层API */
@Component
public class AuthInterceptorFactory implements AnnotationEventInterceptorFactory {

  private final AuthenticationService authService;
  private final PlatformRegistry platformRegistry;

  public AuthInterceptorFactory(
      AuthenticationService authService, PlatformRegistry platformRegistry) {
    this.authService = authService;
    this.platformRegistry = platformRegistry;
  }

  @Override
  public @Nullable Result create(Context context) {
    return Result.build(
        config -> {
          config.interceptor(new AuthInterceptor(authService, platformRegistry));
          config.configuration(properties -> properties.setPriority(context.getPriority()));
        });
  }

  /** 认证拦截器实现 */
  private static class AuthInterceptor implements JBlockEventInterceptor {

    private final AuthenticationService authService;
    private final PlatformRegistry platformRegistry;

    private AuthInterceptor(AuthenticationService authService, PlatformRegistry platformRegistry) {
      this.authService = authService;
      this.platformRegistry = platformRegistry;
    }

    @Override
    public EventResult intercept(JBlockEventInterceptor.Context context) throws Exception {
      var event = context.getSource().getEventListenerContext().getEvent();

      if (!(event instanceof MessageEvent messageEvent)) {
        return context.invoke();
      }

      // 同一事件已被之前某个 @RequireAuth 拦截器认证过，跳过
      Long existing = UserContext.retrieveFromEvent(messageEvent);
      if (existing != null) {
        return context.invoke();
      }

      var handler = platformRegistry.getHandler(messageEvent);
      var platform = handler.getPlatformType();
      var openId = handler.extractOpenId(messageEvent);

      ServiceResult<Long> auth = authService.authenticate(platform, openId);
      if (auth instanceof ServiceResult.Failure<Long>(var errorCode, var errorMessage)) {
        return EventResult.of(errorCode + ": " + errorMessage);
      }

      Long userId = ((ServiceResult.Success<Long>) auth).data();

      UserContext.bindIfAbsent(messageEvent, userId);
      return context.invoke();
    }
  }
}
