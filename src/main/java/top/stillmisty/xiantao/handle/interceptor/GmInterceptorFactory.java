package top.stillmisty.xiantao.handle.interceptor;

import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.JBlockEventInterceptor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.interceptor.AnnotationEventInterceptorFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.util.TextFormat;

/** GM权限拦截器工厂 用于在事件监听层面统一处理GM权限验证 */
@Component
public class GmInterceptorFactory implements AnnotationEventInterceptorFactory {

  private final AuthenticationService authService;
  private final PlatformRegistry platformRegistry;

  public GmInterceptorFactory(
      AuthenticationService authService, PlatformRegistry platformRegistry) {
    this.authService = authService;
    this.platformRegistry = platformRegistry;
  }

  @Override
  public @Nullable Result create(Context context) {
    return Result.build(
        config -> {
          config.interceptor(new GmInterceptor(authService, platformRegistry));
          config.configuration(properties -> properties.setPriority(context.getPriority()));
        });
  }

  /** GM权限拦截器实现 */
  private static class GmInterceptor implements JBlockEventInterceptor {

    private final AuthenticationService authService;
    private final PlatformRegistry platformRegistry;

    private GmInterceptor(AuthenticationService authService, PlatformRegistry platformRegistry) {
      this.authService = authService;
      this.platformRegistry = platformRegistry;
    }

    @Override
    public EventResult intercept(JBlockEventInterceptor.Context context) throws Exception {
      var event = context.getSource().getEventListenerContext().getEvent();
      if (!(event instanceof MessageEvent messageEvent)) {
        return context.invoke();
      }

      var handler = platformRegistry.getHandler(messageEvent);

      // AuthInterceptor 已将 userId 存入事件映射（原始线程），此处直接读取（IO 线程）
      Long userId = UserContext.retrieveFromEvent(messageEvent);
      if (userId == null) {
        replyError(handler, messageEvent, "尚未踏入仙途，请先输入「我要修仙 [道号]」");
        return EventResult.empty();
      }

      Boolean cachedGm = UserContext.getGmCheck(messageEvent);
      boolean isGm;
      if (cachedGm != null) {
        isGm = cachedGm;
      } else {
        boolean checked = authService.isGm(userId);
        Boolean first = UserContext.gmCheckIfAbsent(messageEvent, checked);
        isGm = (first != null) ? first : checked;
      }

      if (!isGm) {
        replyError(handler, messageEvent, "你不是 GM，无法执行 GM 指令");
        return EventResult.empty();
      }
      return context.invoke();
    }

    /** 拒绝时通过平台处理器向玩家显式回复错误信息 */
    private static void replyError(PlatformHandler handler, MessageEvent event, String message) {
      try {
        handler.replyText(event, TextFormat.get().error(message));
      } catch (Exception e) {
        // 回复失败仅记录日志，不阻断拦截流程
      }
    }
  }
}
