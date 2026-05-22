package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.handle.command.FortuneCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class FortuneListener {

  private final FortuneCommandHandler fortuneCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @Filter("今日运势")
  public void fortune(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到运势查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fortuneCommandHandler.handleFortune(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("今日运势")
  public void fortuneQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到运势查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fortuneCommandHandler.handleFortune(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
