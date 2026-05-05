package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

@DisplayName("TribulationService 测试")
@ExtendWith(MockitoExtension.class)
class TribulationServiceTest {

  @Mock private FudiRepository fudiRepository;
  @Mock private FudiCellRepository fudiCellRepository;
  @Mock private SpiritRepository spiritRepository;
  @Mock private BeastRepository beastRepository;
  @Mock private UserRepository userRepository;
  @Mock private FudiHelper fudiHelper;
  @Mock private CombatService combatService;

  @InjectMocks private TribulationService tribulationService;

  // ===================== resolveTribulation =====================

  @Test
  @DisplayName("resolveTribulation — 冷却期内且非强制触发返回 null")
  void resolveTribulation_whenWithinCooldown_shouldReturnNull() {
    Fudi fudi =
        Fudi.create()
            .setId(1L)
            .setUserId(1L)
            .setLastTribulationTime(LocalDateTime.now())
            .setCreateTime(LocalDateTime.now().minusDays(1));

    User user = User.create().setId(1L).setNickname("test").setLevel(1);

    String result = tribulationService.resolveTribulation(fudi, user, false);

    assertNull(result);
  }

  @Test
  @DisplayName("resolveTribulation — 超出冷却期但无存活单位")
  void resolveTribulation_whenPastCooldownButNoAliveUnits_shouldReturnWarning() {
    Fudi fudi =
        Fudi.create()
            .setId(1L)
            .setUserId(1L)
            .setLastTribulationTime(LocalDateTime.now().minusDays(30))
            .setCreateTime(LocalDateTime.now().minusDays(30))
            .setTribulationStage(0)
            .setTribulationWinStreak(0);

    User user =
        User.create()
            .setId(1L)
            .setNickname("test")
            .setLevel(1)
            .setHpCurrent(100)
            .setStatStr(1)
            .setStatCon(1)
            .setStatAgi(1)
            .setStatWis(1);

    // combatService returns an empty team (no beasts needed)
    Team emptyTeam = new Team(1L, "Player");
    when(combatService.buildPlayerTeam(any(User.class))).thenReturn(emptyTeam);

    String result = tribulationService.resolveTribulation(fudi, user, false);

    assertNotNull(result);
    assertTrue(result.contains("天劫无法降临"));
  }
}
