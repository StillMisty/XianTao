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
import top.stillmisty.xiantao.handle.command.DungeonCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class DungeonListener {

  private final DungeonCommandHandler dungeonCommandHandler;
  private final ReplyHelper replyHelper;

  // === 秘境列表 ===

  @Listener
  @ContentTrim
  @Filter("秘境")
  public void dungeonList(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到秘境列表请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, dungeonCommandHandler::handleDungeon);
  }

  @Listener
  @ContentTrim
  @Filter("秘境")
  public void dungeonListQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到秘境列表请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, dungeonCommandHandler::handleDungeon);
  }

  // === 秘境进入 ===

  @Listener
  @ContentTrim
  @Filter("秘境 {{dungeonName}}")
  public void dungeonEnter(
      OneBotMessageEvent event, @FilterValue("dungeonName") String dungeonName) {
    log.debug("[OneBot] 收到进入秘境请求 - AuthorId: {}, Dungeon: {}", event.getAuthorId(), dungeonName);
    replyHelper.oneBot(event, dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  @Listener
  @ContentTrim
  @Filter("秘境 {{dungeonName}}")
  public void dungeonEnterQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("dungeonName") String dungeonName) {
    log.debug("[QQ] 收到进入秘境请求 - AuthorId: {}, Dungeon: {}", event.getAuthorId(), dungeonName);
    replyHelper.qq(event, dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  // === 秘境探索 ===

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExplore(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到秘境探索请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, dungeonCommandHandler::handleDungeonExplore);
  }

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExploreQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到秘境探索请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, dungeonCommandHandler::handleDungeonExplore);
  }

  // === 秘境继续 ===

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinue(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到秘境继续请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, dungeonCommandHandler::handleDungeonContinue);
  }

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinueQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到秘境继续请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, dungeonCommandHandler::handleDungeonContinue);
  }

  // === 秘境撤退 ===

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreat(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到秘境撤退请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, dungeonCommandHandler::handleDungeonRetreat);
  }

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreatQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到秘境撤退请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, dungeonCommandHandler::handleDungeonRetreat);
  }
}
