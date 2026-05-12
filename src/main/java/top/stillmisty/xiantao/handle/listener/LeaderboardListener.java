package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.LeaderboardCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardListener {
  private final LeaderboardCommandHandler leaderboardCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("排行榜")
  public void levelLeaderboard(OneBotMessageEvent event) {
    log.debug("收到排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleLevelLeaderboard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("排行榜 灵石")
  public void spiritStoneLeaderboard(OneBotMessageEvent event) {
    log.debug("收到灵石排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleSpiritStoneLeaderboard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("排行榜")
  public void levelLeaderboardQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleLevelLeaderboardMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("排行榜 灵石")
  public void spiritStoneLeaderboardQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到灵石排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleSpiritStoneLeaderboardMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }
}
