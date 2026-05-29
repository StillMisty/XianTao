package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.SectCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Slf4j
@Component
@RequiredArgsConstructor
public class SectListener {

  private final SectCommandHandler sectCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overviewOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleOverview);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门创建\\s*{{name,\\S+}}\\s+{{ethosDesc,.+?}}")
  public void createWithEthosOneBot(
      OneBotMessageEvent event,
      @FilterValue("name") String name,
      @FilterValue("ethosDesc") String ethosDesc) {
    replyHelper.oneBot(event, fmt -> sectCommandHandler.handleCreate(name, ethosDesc, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门创建\\s*{{name,\\S+}}")
  public void createOneBot(OneBotMessageEvent event, @FilterValue("name") String name) {
    replyHelper.oneBot(event, fmt -> sectCommandHandler.handleCreate(name, null, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗灵\\s*{{content,.+}}")
  public void sectSpiritOneBot(OneBotMessageEvent event, @FilterValue("content") String content) {
    replyHelper.oneBot(event, content, sectCommandHandler::handleSectSpiritChat);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leaveOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleLeave);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismissOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleDismiss);
  }

  // === QQ ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overviewQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleOverview);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门创建\\s*{{name,\\S+}}\\s+{{ethosDesc,.+?}}")
  public void createWithEthosQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("name") String name,
      @FilterValue("ethosDesc") String ethosDesc) {
    replyHelper.qq(event, fmt -> sectCommandHandler.handleCreate(name, ethosDesc, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门创建\\s*{{name,\\S+}}")
  public void createQq(QGGroupAtMessageCreateEvent event, @FilterValue("name") String name) {
    replyHelper.qq(event, fmt -> sectCommandHandler.handleCreate(name, null, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗灵\\s*{{content,.+}}")
  public void sectSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    replyHelper.qq(event, content, sectCommandHandler::handleSectSpiritChat);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leaveQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleLeave);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismissQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleDismiss);
  }
}
