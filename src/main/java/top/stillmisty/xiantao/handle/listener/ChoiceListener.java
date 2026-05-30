package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
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
  @Filter(mode = FilterMode.INTERCEPTOR, value = "选\\s*{{choice}}")
  public void choice(MessageEvent event, @FilterValue("choice") String choice) {
    replyHelper.dispatch(event, "选择", choice, choiceCommandHandler::handleChoice);
  }
}
