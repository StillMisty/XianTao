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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
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
    log.debug("收到秘境列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeon(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("秘境")
  public void dungeonListQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到秘境列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeon(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  // === 秘境进入 ===

  @Listener
  @ContentTrim
  @Filter("秘境 {{dungeonName}}")
  public void dungeonEnter(
      OneBotMessageEvent event, @FilterValue("dungeonName") String dungeonName) {
    log.debug("收到进入秘境请求 - AuthorId: {}, Dungeon: {}", event.getAuthorId(), dungeonName);
    String response =
        dungeonCommandHandler.handleDungeonEnter(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            dungeonName,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("秘境 {{dungeonName}}")
  public void dungeonEnterQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("dungeonName") String dungeonName) {
    log.debug("收到进入秘境请求 - AuthorId: {}, Dungeon: {}", event.getAuthorId(), dungeonName);
    String response =
        dungeonCommandHandler.handleDungeonEnter(
            PlatformType.QQ, event.getAuthorId().toString(), dungeonName, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  // === 秘境探索 ===

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExplore(OneBotMessageEvent event) {
    log.debug("收到秘境探索请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonExplore(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExploreQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到秘境探索请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonExplore(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  // === 秘境继续 ===

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinue(OneBotMessageEvent event) {
    log.debug("收到秘境继续请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonContinue(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinueQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到秘境继续请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonContinue(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  // === 秘境撤退 ===

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreat(OneBotMessageEvent event) {
    log.debug("收到秘境撤退请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonRetreat(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreatQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到秘境撤退请求 - AuthorId: {}", event.getAuthorId());
    String response =
        dungeonCommandHandler.handleDungeonRetreat(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
