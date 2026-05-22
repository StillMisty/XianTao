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

@Slf4j
@Component
@RequiredArgsConstructor
public class SectListener {

  private final SectCommandHandler sectCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overviewOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleOverview);
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}} {{ethosDesc,.+?}}")
  public void createWithEthosOneBot(
      OneBotMessageEvent event,
      @FilterValue("name") String name,
      @FilterValue("ethosDesc") String ethosDesc) {
    replyHelper.oneBot(
        event, (p, o, f) -> sectCommandHandler.handleCreate(p, o, name, ethosDesc, f));
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}}")
  public void createOneBot(OneBotMessageEvent event, @FilterValue("name") String name) {
    replyHelper.oneBot(event, (p, o, f) -> sectCommandHandler.handleCreate(p, o, name, null, f));
  }

  @Listener
  @ContentTrim
  @Filter("宗灵 {{content,.+}}")
  public void sectSpiritOneBot(OneBotMessageEvent event, @FilterValue("content") String content) {
    replyHelper.oneBot(event, content, sectCommandHandler::handleSectSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leaveOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleLeave);
  }

  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismissOneBot(OneBotMessageEvent event) {
    replyHelper.oneBot(event, sectCommandHandler::handleDismiss);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("宗门")
  public void overviewQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleOverview);
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}} {{ethosDesc,.+?}}")
  public void createWithEthosQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("name") String name,
      @FilterValue("ethosDesc") String ethosDesc) {
    replyHelper.qq(event, (p, o, f) -> sectCommandHandler.handleCreate(p, o, name, ethosDesc, f));
  }

  @Listener
  @ContentTrim
  @Filter("宗门创建 {{name,[\\S]+}}")
  public void createQq(QGGroupAtMessageCreateEvent event, @FilterValue("name") String name) {
    replyHelper.qq(event, (p, o, f) -> sectCommandHandler.handleCreate(p, o, name, null, f));
  }

  @Listener
  @ContentTrim
  @Filter("宗灵 {{content,.+}}")
  public void sectSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    replyHelper.qq(event, content, sectCommandHandler::handleSectSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("宗门退出")
  public void leaveQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleLeave);
  }

  @Listener
  @ContentTrim
  @Filter("宗门解散")
  public void dismissQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, sectCommandHandler::handleDismiss);
  }
}
