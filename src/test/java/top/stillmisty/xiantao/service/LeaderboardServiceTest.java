package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardEntryVO;
import top.stillmisty.xiantao.domain.user.vo.LeaderboardVO;

@DisplayName("LeaderboardService 测试")
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private LeaderboardService leaderboardService;

  private User createUser(long id, String nickname, int level, long exp, long spiritStones) {
    return User.create()
        .setId(id)
        .setNickname(nickname)
        .setLevel(level)
        .setExp(exp)
        .setSpiritStones(spiritStones);
  }

  // ===================== buildLevelLeaderboard =====================

  @Test
  @DisplayName("buildLevelLeaderboard — 按等级降序排列并正确设置 showLevel=true")
  void buildLevelLeaderboard_shouldOrderByLevelDesc() {
    User user1 = createUser(1, "张三", 50, 5000L, 1000L);
    User user2 = createUser(2, "李四", 40, 8000L, 2000L);
    User user3 = createUser(3, "王五", 30, 3000L, 3000L);
    when(userRepository.findTopByLevel(10)).thenReturn(List.of(user1, user2, user3));

    LeaderboardVO result = leaderboardService.buildLevelLeaderboard();

    assertEquals("【修为排行榜】", result.title());
    assertTrue(result.showLevel());
    List<LeaderboardEntryVO> entries = result.entries();
    assertEquals(3, entries.size());
    assertEquals(1, entries.get(0).rank());
    assertEquals("张三", entries.get(0).nickname());
    assertEquals(50, entries.get(0).level());
    assertEquals(2, entries.get(1).rank());
    assertEquals("李四", entries.get(1).nickname());
    assertEquals(3, entries.get(2).rank());
    assertEquals("王五", entries.get(2).nickname());
  }

  @Test
  @DisplayName("buildLevelLeaderboard — 空数据返回空列表")
  void buildLevelLeaderboard_whenNoUsers_shouldReturnEmpty() {
    when(userRepository.findTopByLevel(10)).thenReturn(List.of());

    LeaderboardVO result = leaderboardService.buildLevelLeaderboard();

    assertTrue(result.entries().isEmpty());
  }

  // ===================== buildSpiritStoneLeaderboard =====================

  @Test
  @DisplayName("buildSpiritStoneLeaderboard — 按灵石降序排列并正确设置 showLevel=false")
  void buildSpiritStoneLeaderboard_shouldOrderBySpiritStonesDesc() {
    User user1 = createUser(1, "赵六", 30, 5000L, 9999L);
    User user2 = createUser(2, "钱七", 50, 8000L, 5000L);
    User user3 = createUser(3, "孙八", 40, 3000L, 1000L);
    when(userRepository.findTopBySpiritStones(10)).thenReturn(List.of(user1, user2, user3));

    LeaderboardVO result = leaderboardService.buildSpiritStoneLeaderboard();

    assertEquals("【灵石排行榜】", result.title());
    assertFalse(result.showLevel());
    List<LeaderboardEntryVO> entries = result.entries();
    assertEquals(3, entries.size());
    assertEquals(1, entries.get(0).rank());
    assertEquals("赵六", entries.get(0).nickname());
    assertEquals(9999L, entries.get(0).spiritStones());
    assertEquals(2, entries.get(1).rank());
    assertEquals("钱七", entries.get(1).nickname());
    assertEquals(3, entries.get(2).rank());
    assertEquals("孙八", entries.get(2).nickname());
  }

  @Test
  @DisplayName("buildSpiritStoneLeaderboard — 空数据返回空列表")
  void buildSpiritStoneLeaderboard_whenNoUsers_shouldReturnEmpty() {
    when(userRepository.findTopBySpiritStones(10)).thenReturn(List.of());

    LeaderboardVO result = leaderboardService.buildSpiritStoneLeaderboard();

    assertTrue(result.entries().isEmpty());
  }

  // ===================== 排名编号 =====================

  @Test
  @DisplayName("两种排行榜的排名编号都从1开始连续")
  void bothLeaderboards_shouldHaveConsecutiveRanks() {
    User u1 = createUser(1, "A", 50, 1000L, 100L);
    User u2 = createUser(2, "B", 49, 1000L, 99L);
    when(userRepository.findTopByLevel(10)).thenReturn(List.of(u1, u2));
    when(userRepository.findTopBySpiritStones(10)).thenReturn(List.of(u1, u2));

    LeaderboardVO levelResult = leaderboardService.buildLevelLeaderboard();
    assertEquals(1, levelResult.entries().get(0).rank());
    assertEquals(2, levelResult.entries().get(1).rank());

    LeaderboardVO spiritResult = leaderboardService.buildSpiritStoneLeaderboard();
    assertEquals(1, spiritResult.entries().get(0).rank());
    assertEquals(2, spiritResult.entries().get(1).rank());
  }
}
