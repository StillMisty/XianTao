package top.stillmisty.xiantao.handle.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.handle.platform.ReplyDelivery;
import top.stillmisty.xiantao.util.TextFormat;

/** 平台回复辅助 集中处理多平台回复方式 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyHelper {

  private final PlatformRegistry platformRegistry;
  private final ReplyDelivery replyDelivery;

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

  public void dispatch(QqIncomingMessage message, String command, CommandFn fn) {
    PlatformHandler handler = platformRegistry.getHandler(message);

    log.debug(
        "[{}] {}请求 - AuthorId: {}",
        handler.getPlatformType(),
        command,
        handler.extractOpenId(message));

    NextActions.Collected<String> collected;
    try {
      collected = NextActions.collect(() -> fn.execute(TextFormat.get()));
    } catch (Exception e) {
      log.error("[{}] {}执行异常", handler.getPlatformType(), command, e);
      collected = new NextActions.Collected<>(TextFormat.get().error("系统繁忙，请稍后再试"), List.of());
    }
    reply(handler, message, sanitize(collected.value(), command, handler), collected.suggestions());
  }

  public void dispatch(QqIncomingMessage message, String command, String arg, CommandFn1 fn) {
    PlatformHandler handler = platformRegistry.getHandler(message);

    log.debug(
        "[{}] {}请求 - AuthorId: {}, Arg: {}",
        handler.getPlatformType(),
        command,
        handler.extractOpenId(message),
        arg);

    NextActions.Collected<String> collected;
    try {
      collected = NextActions.collect(() -> fn.execute(arg, TextFormat.get()));
    } catch (Exception e) {
      log.error("[{}] {}执行异常", handler.getPlatformType(), command, e);
      collected = new NextActions.Collected<>(TextFormat.get().error("系统繁忙，请稍后再试"), List.of());
    }
    reply(handler, message, sanitize(collected.value(), command, handler), collected.suggestions());
  }

  // ===================== 内部辅助方法 =====================

  /**
   * 兜底：命令处理器可能因 AI 返回空内容等原因产出 null/空白文本（此类 null 能绕过 NullAway 的泛型推断）， 直接发送会变成 {@code
   * markdown.content = null} 被平台拒绝。这里统一替换为错误文案并告警。
   */
  private static String sanitize(String text, String command, PlatformHandler handler) {
    if (text == null || text.isBlank()) {
      log.warn("[{}] {}产出空回复，已替换为错误文案", handler.getPlatformType(), command);
      return TextFormat.get().error("系统繁忙，请稍后再试");
    }
    return text;
  }

  /** 通过平台处理器回复，失败仅记录日志。 */
  public void reply(PlatformHandler handler, QqIncomingMessage message, String text) {
    reply(handler, message, text, List.of());
  }

  /** 通过回复投递管线回复（附带下一步建议按钮），失败仅记录日志。 */
  public void reply(
      PlatformHandler handler,
      QqIncomingMessage message,
      String text,
      List<NextActions.Suggestion> suggestions) {
    try {
      replyDelivery.deliver(handler, message, text, suggestions);
    } catch (Exception e) {
      log.warn("{} 回复失败: {}", handler.getPlatformType(), e.getMessage(), e);
    }
  }
}
