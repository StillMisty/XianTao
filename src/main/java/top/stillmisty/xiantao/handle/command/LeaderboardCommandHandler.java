package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardVO;
import top.stillmisty.xiantao.service.LeaderboardService;
import top.stillmisty.xiantao.service.ServiceResult;

@Component
@RequiredArgsConstructor
public class LeaderboardCommandHandler implements CommandGroup {

  private final LeaderboardService leaderboardService;

  public String handleLevelLeaderboard(PlatformType platform, String openId) {
    return switch (leaderboardService.getLevelLeaderboard(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatLeaderboard(vo);
    };
  }

  public String handleSpiritStoneLeaderboard(PlatformType platform, String openId) {
    return switch (leaderboardService.getSpiritStoneLeaderboard(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatLeaderboard(vo);
    };
  }

  private String formatLeaderboard(LeaderboardVO vo) {
    if (vo.entries() == null || vo.entries().isEmpty()) {
      return vo.title() + "（暂无数据）";
    }
    StringBuilder sb = new StringBuilder(vo.title()).append("\n");
    for (var e : vo.entries()) {
      String medal =
          switch (e.rank()) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "  " + e.rank() + ".";
          };
      sb.append(medal).append(" ").append(e.nickname());
      if (vo.showLevel()) {
        sb.append("  第").append(e.level()).append("层");
      } else {
        sb.append("  灵石：").append(e.spiritStones());
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "排行榜";
  }

  @Override
  public String groupDescription() {
    return "查看修为和财富排名";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("排行榜", "查看修为排行榜", "排行榜"), new CommandEntry("排行榜 灵石", "查看灵石排行榜", "排行榜 灵石"));
  }
}
