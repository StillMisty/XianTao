package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.FortuneCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class FortuneListener {

  private final FortuneCommandHandler fortuneCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "今日运势")
  public void fortune(MessageEvent event) {
    replyHelper.dispatch(event, "今日运势", fortuneCommandHandler::handleFortune);
  }
}
