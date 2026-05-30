package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.LeaderboardCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class LeaderboardListener {
  private final LeaderboardCommandHandler leaderboardCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "排行榜")
  public void levelLeaderboard(MessageEvent event) {
    replyHelper.dispatch(event, "排行榜", leaderboardCommandHandler::handleLevelLeaderboard);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "排行榜 灵石")
  public void spiritStoneLeaderboard(MessageEvent event) {
    replyHelper.dispatch(event, "灵石排行榜", leaderboardCommandHandler::handleSpiritStoneLeaderboard);
  }
}
