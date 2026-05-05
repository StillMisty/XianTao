package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.LeaderboardCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardHandle {

  private final LeaderboardCommandHandler leaderboardCommandHandler;

  @Listener
  @ContentTrim
  @Filter("排行榜")
  public void levelLeaderboard(MessageEvent event) {
    log.debug("收到排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleLevelLeaderboard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    event.replyBlocking(response);
  }

  @Listener
  @ContentTrim
  @Filter("排行榜 灵石")
  public void spiritStoneLeaderboard(MessageEvent event) {
    log.debug("收到灵石排行榜请求 - AuthorId: {}", event.getAuthorId());
    String response =
        leaderboardCommandHandler.handleSpiritStoneLeaderboard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    event.replyBlocking(response);
  }
}
