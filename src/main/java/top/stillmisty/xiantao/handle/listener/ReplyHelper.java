package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.service.UserContext;

/** 平台回复辅助 集中处理多平台回复方式 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyHelper {

  private final PlatformRegistry platformRegistry;

  /** 无额外参数的指令方法签名: (TextFormat) → String */
  @FunctionalInterface
  public interface CommandFn {
    String execute(TextFormat fmt);
  }

  /** 含一个额外参数的指令方法签名 */
  @FunctionalInterface
  public interface CommandFn1 {
    String execute(String arg, TextFormat fmt);
  }

  // ===================== 通用 dispatch 方法（支持多平台） =====================

  public void dispatch(MessageEvent event, String command, CommandFn fn) {
    PlatformHandler handler = platformRegistry.getHandler(event);
    PlatformType platform = handler.getPlatformType();
    TextFormat fmt = getTextFormat(platform);

    log.debug("[{}] {}请求 - AuthorId: {}", platform, command, handler.extractOpenId(event));

    executeWithUserContext(
        event,
        () -> {
          String text = fn.execute(fmt);
          replyWithHandler(handler, event, text);
        });
  }

  public void dispatch(MessageEvent event, String command, String arg, CommandFn1 fn) {
    PlatformHandler handler = platformRegistry.getHandler(event);
    PlatformType platform = handler.getPlatformType();
    TextFormat fmt = getTextFormat(platform);

    log.debug(
        "[{}] {}请求 - AuthorId: {}, Arg: {}", platform, command, handler.extractOpenId(event), arg);

    executeWithUserContext(
        event,
        () -> {
          String text = fn.execute(arg, fmt);
          replyWithHandler(handler, event, text);
        });
  }

  // ===================== 平台类型识别 =====================

  public static PlatformType platformTypeOf(MessageEvent event) {
    return switch (event) {
      case love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent __ ->
          PlatformType.ONE_BOT_V11;
      case love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent __ ->
          PlatformType.QQ;
      default -> throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
    };
  }

  // ===================== 内部辅助方法 =====================

  private TextFormat getTextFormat(PlatformType platform) {
    return switch (platform) {
      case ONE_BOT_V11 -> TextFormat.plain();
      case QQ -> TextFormat.markdown();
      case WEB -> TextFormat.plain();
    };
  }

  private void executeWithUserContext(MessageEvent event, Runnable action) {
    Long userId = UserContext.retrieveFromEvent(event);
    if (userId != null) {
      UserContext.withUser(
          userId,
          () -> {
            action.run();
            return null;
          });
    } else {
      action.run();
    }
  }

  private void replyWithHandler(PlatformHandler handler, MessageEvent event, String text) {
    try {
      handler.replyText(event, text);
    } catch (Exception e) {
      log.warn("{} 回复失败: {}", handler.getPlatformType(), e.getMessage(), e);
    }
  }
}
