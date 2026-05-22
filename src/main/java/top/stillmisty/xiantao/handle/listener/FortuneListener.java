package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
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
    replyHelper.oneBot(event, fortuneCommandHandler::handleFortune);
  }

  @Listener
  @ContentTrim
  @Filter("今日运势")
  public void fortuneQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, fortuneCommandHandler::handleFortune);
  }
}
