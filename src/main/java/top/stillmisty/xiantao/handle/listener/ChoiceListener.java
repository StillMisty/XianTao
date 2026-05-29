package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.ChoiceCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class ChoiceListener {

  private final ChoiceCommandHandler choiceCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("选\\s*{{choice}}")
  public void choice(OneBotMessageEvent event, @FilterValue("choice") String choice) {
    replyHelper.oneBot(event, "选择", choice, choiceCommandHandler::handleChoice);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("选\\s*{{choice}}")
  public void choiceQq(QGGroupAtMessageCreateEvent event, @FilterValue("choice") String choice) {
    replyHelper.qq(event, "选择", choice, choiceCommandHandler::handleChoice);
  }
}
