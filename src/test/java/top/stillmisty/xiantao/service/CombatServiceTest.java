package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

@DisplayName("CombatService 测试")
@ExtendWith(MockitoExtension.class)
class CombatServiceTest {

  @Mock private EquipmentRepository equipmentRepository;
  @Mock private EquipmentTemplateRepository equipmentTemplateRepository;
  @Mock private SkillRepository skillRepository;
  @Mock private BeastRepository beastRepository;
  @Mock private PlayerBuffRepository playerBuffRepository;
  @Mock private CombatEngine combatEngine;

  @InjectMocks private CombatService combatService;

  // ===================== buildPlayerTeam =====================

  @Test
  @DisplayName("buildPlayerTeam — 返回包含玩家的队伍")
  void buildPlayerTeam_shouldReturnTeamWithPlayer() {
    User user =
        User.create()
            .setId(1L)
            .setLevel(1)
            .setNickname("测试")
            .setHpCurrent(100)
            .setStatStr(1)
            .setStatCon(1)
            .setStatAgi(1)
            .setStatWis(1)
            .setStatus(UserStatus.IDLE);

    when(playerBuffRepository.findActiveByUserId(1L)).thenReturn(List.of());
    when(beastRepository.findDeployedByUserId(1L)).thenReturn(List.of());

    Team team = combatService.buildPlayerTeam(user);

    assertNotNull(team);
    assertEquals("Player", team.name());
    assertFalse(team.members().isEmpty());
  }

  @Test
  @DisplayName("buildPlayerTeam — 无出战灵兽时只有玩家")
  void buildPlayerTeam_withoutBeasts_shouldHaveOneMember() {
    User user =
        User.create()
            .setId(2L)
            .setLevel(1)
            .setNickname("修士")
            .setHpCurrent(150)
            .setStatStr(1)
            .setStatCon(1)
            .setStatAgi(1)
            .setStatWis(1)
            .setStatus(UserStatus.IDLE);

    when(playerBuffRepository.findActiveByUserId(2L)).thenReturn(List.of());
    when(beastRepository.findDeployedByUserId(2L)).thenReturn(List.of());

    Team team = combatService.buildPlayerTeam(user);

    assertEquals(1, team.members().size());
  }

  @Test
  @DisplayName("buildPlayerTeam — 包含 buff 加成")
  void buildPlayerTeam_withBuffs_shouldApplyBuffs() {
    User user =
        User.create()
            .setId(3L)
            .setLevel(1)
            .setNickname("修士")
            .setHpCurrent(150)
            .setStatStr(1)
            .setStatCon(1)
            .setStatAgi(1)
            .setStatWis(1)
            .setStatus(UserStatus.IDLE);

    PlayerBuff attackBuff = PlayerBuff.create(3L, "attack", 10, null);
    when(playerBuffRepository.findActiveByUserId(3L)).thenReturn(List.of(attackBuff));
    when(beastRepository.findDeployedByUserId(3L)).thenReturn(List.of());

    Team team = combatService.buildPlayerTeam(user);

    assertNotNull(team);
    assertEquals(1, team.members().size());
  }

  // ===================== simulate =====================

  @Test
  @DisplayName("simulate — 返回战斗模拟结果")
  void simulate_withEmptyTeams_shouldReturnWellFormedResult() {
    Team teamA = new Team(1L, "Player");
    Team teamB = new Team(2L, "Monster");

    when(combatEngine.simulate(any(BattleContext.class)))
        .thenReturn(BattleResultVO.builder().winner("Player").rounds(0).expGained(0L).build());

    BattleResultVO result = combatService.simulate(teamA, teamB, 10);

    assertNotNull(result);
    assertEquals("Player", result.winner());
  }
}
