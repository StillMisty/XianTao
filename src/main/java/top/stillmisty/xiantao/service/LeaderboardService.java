package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardEntryVO;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardVO;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "leaderboard", key = "'level'", sync = true)
  public ServiceResult<LeaderboardVO> getLevelLeaderboard(Long userId) {
    return new ServiceResult.Success<>(buildLevelLeaderboardInternal());
  }

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "leaderboard", key = "'spiritStones'", sync = true)
  public ServiceResult<LeaderboardVO> getSpiritStoneLeaderboard(Long userId) {
    return new ServiceResult.Success<>(buildSpiritStoneLeaderboardInternal());
  }

  LeaderboardVO buildLevelLeaderboardInternal() {
    List<User> users = userRepository.findTopByLevel(10);
    return new LeaderboardVO("【修为排行榜】", buildEntries(users), true);
  }

  LeaderboardVO buildSpiritStoneLeaderboardInternal() {
    List<User> users = userRepository.findTopBySpiritStones(10);
    return new LeaderboardVO("【灵石排行榜】", buildEntries(users), false);
  }

  private List<LeaderboardEntryVO> buildEntries(List<User> users) {
    List<LeaderboardEntryVO> entries = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      User u = users.get(i);
      entries.add(
          new LeaderboardEntryVO(i + 1, u.getNickname(), u.getLevel(), u.getSpiritStones()));
    }
    return entries;
  }
}
