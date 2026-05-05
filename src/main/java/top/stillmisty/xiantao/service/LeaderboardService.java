package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardEntryVO;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardVO;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

  private final UserRepository userRepository;

  @Authenticated
  @Cacheable(cacheNames = "leaderboard", key = "'level'")
  public ServiceResult<LeaderboardVO> getLevelLeaderboard(PlatformType platform, String openId) {
    return new ServiceResult.Success<>(buildLevelLeaderboard());
  }

  @Authenticated
  @Cacheable(cacheNames = "leaderboard", key = "'spiritStones'")
  public ServiceResult<LeaderboardVO> getSpiritStoneLeaderboard(
      PlatformType platform, String openId) {
    return new ServiceResult.Success<>(buildSpiritStoneLeaderboard());
  }

  LeaderboardVO buildLevelLeaderboard() {
    List<User> users = userRepository.findTopByLevel(10);
    List<LeaderboardEntryVO> entries = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      User u = users.get(i);
      entries.add(
          new LeaderboardEntryVO(i + 1, u.getNickname(), u.getLevel(), u.getSpiritStones()));
    }
    return new LeaderboardVO("【修为排行榜】", entries, true);
  }

  LeaderboardVO buildSpiritStoneLeaderboard() {
    List<User> users = userRepository.findTopBySpiritStones(10);
    List<LeaderboardEntryVO> entries = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      User u = users.get(i);
      entries.add(
          new LeaderboardEntryVO(i + 1, u.getNickname(), u.getLevel(), u.getSpiritStones()));
    }
    return new LeaderboardVO("【灵石排行榜】", entries, false);
  }
}
