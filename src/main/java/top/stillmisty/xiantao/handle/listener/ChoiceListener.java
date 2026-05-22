package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.handle.command.ChoiceCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChoiceListener {

  private final ChoiceCommandHandler choiceCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @Filter("选 {{choice}}")
  public void choice(OneBotMessageEvent event, @FilterValue("choice") String choice) {
    log.debug("[OneBot] 收到选择请求 - AuthorId: {}", event.getAuthorId());
    String response =
        choiceCommandHandler.handleChoice(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), choice, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @Filter("选 {{choice}}")
  public void choiceQq(QGGroupAtMessageCreateEvent event, @FilterValue("choice") String choice) {
    log.debug("[QQ] 收到选择请求 - AuthorId: {}", event.getAuthorId());
    String response =
        choiceCommandHandler.handleChoice(
            PlatformType.QQ, event.getAuthorId().toString(), choice, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
