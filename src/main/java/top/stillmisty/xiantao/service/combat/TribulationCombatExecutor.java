package top.stillmisty.xiantao.service.combat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 渡劫战斗公共流程：构建队伍 → 执行战斗 → 结算 HP */
@Component
@RequiredArgsConstructor
@Slf4j
public class TribulationCombatExecutor {

  public static final int TRIBULATION_MAX_ROUNDS = 40;

  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;

  public record TribulationBattleResult(
      CombatTeam defendingTeam, BattleResultVO battleResult, boolean playerWon) {}

  /**
   * 构建玩家队伍并检查是否有存活成员。
   *
   * @return 玩家队伍；若无可出战单位返回 null
   */
  public @Nullable CombatTeam buildTeamOrReturnNull(User user) {
    CombatTeam team = combatService.buildPlayerTeam(user);
    return team.aliveMembers().isEmpty() ? null : team;
  }

  public CombatService.TeamStats calculateTeamStats(CombatTeam team) {
    return combatService.calculateTeamStats(team);
  }

  /**
   * 执行渡劫战斗并结算 HP。
   *
   * @param defendingTeam 已构建的玩家队伍
   * @param boss 已构建好的天劫 Boss
   * @return 战斗结果
   */
  public TribulationBattleResult execute(CombatTeam defendingTeam, TribulationBoss boss) {
    CombatTeam bossTeam = new CombatTeam(0L, "天劫");
    bossTeam.addMember(boss);

    BattleResultVO result = combatService.simulate(defendingTeam, bossTeam, TRIBULATION_MAX_ROUNDS);
    boolean playerWon = "Player".equals(result.winner());

    return new TribulationBattleResult(defendingTeam, result, playerWon);
  }
}
