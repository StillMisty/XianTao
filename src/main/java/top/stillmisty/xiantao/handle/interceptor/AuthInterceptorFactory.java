package top.stillmisty.xiantao.handle.interceptor;

import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.JBlockEventInterceptor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.interceptor.AnnotationEventInterceptorFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;

/** 认证拦截器工厂 用于在事件监听层面统一处理认证，消除Service层的双层API */
@Component
public class AuthInterceptorFactory implements AnnotationEventInterceptorFactory {

  private final AuthenticationService authService;

  public AuthInterceptorFactory(AuthenticationService authService) {
    this.authService = authService;
  }

  @Override
  public @Nullable Result create(Context context) {
    return Result.build(
        config -> {
          config.interceptor(new AuthInterceptor(authService));
          config.configuration(properties -> properties.setPriority(context.getPriority()));
        });
  }

  /** 认证拦截器实现 */
  private static class AuthInterceptor implements JBlockEventInterceptor {

    private final AuthenticationService authService;

    public AuthInterceptor(AuthenticationService authService) {
      this.authService = authService;
    }

    @Override
    public EventResult intercept(JBlockEventInterceptor.Context context) throws Exception {
      var event = context.getSource().getEventListenerContext().getEvent();

      if (!(event instanceof MessageEvent messageEvent)) {
        throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
      }

      PlatformType platform = resolvePlatform(messageEvent);
      String openId = messageEvent.getAuthorId().toString();

      ServiceResult<Long> auth = authService.authenticate(platform, openId, null);
      if (auth instanceof ServiceResult.Failure<Long>(var errorCode, var errorMessage)) {
        return EventResult.of(errorCode + ": " + errorMessage);
      }

      Long userId = ((ServiceResult.Success<Long>) auth).data();

      try {
        return UserContext.withUser(userId, context::invoke);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private PlatformType resolvePlatform(MessageEvent event) {
      return switch (event) {
        case OneBotMessageEvent ignored -> PlatformType.ONE_BOT_V11;
        case QGGroupAtMessageCreateEvent ignored -> PlatformType.QQ;
        default -> throw new IllegalArgumentException("不支持的平台事件: " + event.getClass().getName());
      };
    }
  }
}
