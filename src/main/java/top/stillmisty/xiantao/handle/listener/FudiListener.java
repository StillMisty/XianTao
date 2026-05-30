package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class FudiListener {

  private final FudiCommandHandler fudiCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "福地")
  public void handleFudi(MessageEvent event) {
    replyHelper.dispatch(event, "福地", fudiCommandHandler::handleFudiStatus);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "福地地块")
  public void handleFudiGrid(MessageEvent event) {
    replyHelper.dispatch(event, "福地地块", fudiCommandHandler::handleFudiGrid);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "地灵\\s*{{content}}")
  public void handleFudiSpirit(MessageEvent event, @FilterValue("content") String content) {
    replyHelper.dispatch(event, "地灵对话", content, fudiCommandHandler::handleSpiritChat);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "福地渡劫")
  public void handleFudiTribulation(MessageEvent event) {
    replyHelper.dispatch(event, "福地渡劫", fudiCommandHandler::handleTriggerTribulation);
  }
}
