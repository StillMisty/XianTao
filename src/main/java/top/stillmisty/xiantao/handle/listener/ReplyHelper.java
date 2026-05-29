package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.NotificationAppender;

/** 平台回复辅助 集中处理 OneBotV11 / QQ 两种回复方式 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyHelper {

  private final NotificationAppender notificationAppender;

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

  // ===================== 便捷 dispatch 方法（封装 platform/openId/fmt 常量的提取 + 日志） =====================

  public void oneBot(OneBotMessageEvent event, String command, CommandFn fn) {
    log.debug("[OneBot] {}请求 - AuthorId: {}", command, event.getAuthorId());
    String text = fn.execute(TextFormat.PLAIN);
    replyOneBot(event, text);
  }

  public void oneBot(OneBotMessageEvent event, String command, String arg, CommandFn1 fn) {
    log.debug("[OneBot] {}请求 - AuthorId: {}, Arg: {}", command, event.getAuthorId(), arg);
    String text = fn.execute(arg, TextFormat.PLAIN);
    replyOneBot(event, text);
  }

  public void qq(QGGroupAtMessageCreateEvent event, String command, CommandFn fn) {
    log.debug("[QQ] {}请求 - AuthorId: {}", command, event.getAuthorId());
    String text = fn.execute(TextFormat.MARKDOWN);
    replyQQ(event, text);
  }

  public void qq(QGGroupAtMessageCreateEvent event, String command, String arg, CommandFn1 fn) {
    log.debug("[QQ] {}请求 - AuthorId: {}, Arg: {}", command, event.getAuthorId(), arg);
    String text = fn.execute(arg, TextFormat.MARKDOWN);
    replyQQ(event, text);
  }

  // ===================== 无日志便捷方法（向后兼容/特殊场景） =====================

  public void oneBot(OneBotMessageEvent event, CommandFn fn) {
    String text = fn.execute(TextFormat.PLAIN);
    replyOneBot(event, text);
  }

  public void oneBot(OneBotMessageEvent event, String arg, CommandFn1 fn) {
    String text = fn.execute(arg, TextFormat.PLAIN);
    replyOneBot(event, text);
  }

  public void qq(QGGroupAtMessageCreateEvent event, CommandFn fn) {
    String text = fn.execute(TextFormat.MARKDOWN);
    replyQQ(event, text);
  }

  public void qq(QGGroupAtMessageCreateEvent event, String arg, CommandFn1 fn) {
    String text = fn.execute(arg, TextFormat.MARKDOWN);
    replyQQ(event, text);
  }

  // ===================== 底层 reply 方法 =====================

  public void replyOneBot(OneBotMessageEvent event, String text) {
    try {
      var result =
          notificationAppender.prepareAppend(
              PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), text);
      event.replyBlocking(result.text());
      notificationAppender.markDelivered(result.eventIds());
    } catch (Exception e) {
      log.warn("OneBot 回复失败: {}", e.getMessage(), e);
    }
  }

  public void replyQQ(QGGroupAtMessageCreateEvent event, String text) {
    try {
      var result =
          notificationAppender.prepareAppend(PlatformType.QQ, event.getAuthorId().toString(), text);
      event.replyBlocking(QGMarkdown.create(result.text()));
      notificationAppender.markDelivered(result.eventIds());
    } catch (Exception e) {
      log.warn("QQ 回复失败: {}", e.getMessage(), e);
    }
  }
}
