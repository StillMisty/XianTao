package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.LeaderboardCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class LeaderboardListener {
  private final LeaderboardCommandHandler leaderboardCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("排行榜")
  public void levelLeaderboard(QqIncomingMessage event) {
    replyHelper.dispatch(event, "排行榜", leaderboardCommandHandler::handleLevelLeaderboard);
  }

  @RequireAuth
  @Command("排行榜 灵石")
  public void spiritStoneLeaderboard(QqIncomingMessage event) {
    replyHelper.dispatch(event, "灵石排行榜", leaderboardCommandHandler::handleSpiritStoneLeaderboard);
  }
}
