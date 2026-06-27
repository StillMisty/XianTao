package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import org.springframework.stereotype.Component;
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

    log.debug("[{}] {}请求 - AuthorId: {}", "QQ", command, handler.extractOpenId(event));

    executeWithUserContext(
        event,
        () -> {
          String text = fn.execute(TextFormat.get());
          replyWithHandler(handler, event, text);
        });
  }

  public void dispatch(MessageEvent event, String command, String arg, CommandFn1 fn) {
    PlatformHandler handler = platformRegistry.getHandler(event);

    log.debug(
        "[{}] {}请求 - AuthorId: {}, Arg: {}", "QQ", command, handler.extractOpenId(event), arg);

    executeWithUserContext(
        event,
        () -> {
          String text = fn.execute(arg, TextFormat.get());
          replyWithHandler(handler, event, text);
        });
  }

  // ===================== 内部辅助方法 =====================

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
