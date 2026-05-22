package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.LeaderboardCommandHandler;

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
    replyHelper.oneBot(event, "排行榜", leaderboardCommandHandler::handleLevelLeaderboard);
  }

  @Listener
  @ContentTrim
  @Filter("排行榜 灵石")
  public void spiritStoneLeaderboard(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "灵石排行榜", leaderboardCommandHandler::handleSpiritStoneLeaderboard);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("排行榜")
  public void levelLeaderboardQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "排行榜", leaderboardCommandHandler::handleLevelLeaderboard);
  }

  @Listener
  @ContentTrim
  @Filter("排行榜 灵石")
  public void spiritStoneLeaderboardQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "灵石排行榜", leaderboardCommandHandler::handleSpiritStoneLeaderboard);
  }
}
