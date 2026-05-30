package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.SectCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class SectListener {

  private final SectCommandHandler sectCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗门")
  public void overview(MessageEvent event) {
    replyHelper.dispatch(event, "宗门", sectCommandHandler::handleOverview);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗门创建\\s*{{name,\\S+}}\\s+{{ethosDesc,.+?}}")
  public void createWithEthos(
      MessageEvent event,
      @FilterValue("name") String name,
      @FilterValue("ethosDesc") String ethosDesc) {
    replyHelper.dispatch(
        event, "宗门创建", fmt -> sectCommandHandler.handleCreate(name, ethosDesc, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗门创建\\s*{{name,\\S+}}")
  public void create(MessageEvent event, @FilterValue("name") String name) {
    replyHelper.dispatch(event, "宗门创建", fmt -> sectCommandHandler.handleCreate(name, "", fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗灵\\s*{{content,.+}}")
  public void sectSpirit(MessageEvent event, @FilterValue("content") String content) {
    replyHelper.dispatch(event, "宗灵对话", content, sectCommandHandler::handleSectSpiritChat);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗门退出")
  public void leave(MessageEvent event) {
    replyHelper.dispatch(event, "宗门退出", sectCommandHandler::handleLeave);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "宗门解散")
  public void dismiss(MessageEvent event) {
    replyHelper.dispatch(event, "宗门解散", sectCommandHandler::handleDismiss);
  }
}
