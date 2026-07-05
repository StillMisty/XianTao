package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.monster.Battle;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.Player;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

  private final CombatEngine combatEngine;
  private final TeamBuilder teamBuilder;

  public BattleResultVO simulate(CombatTeam teamA, CombatTeam teamB, int maxRounds) {
    Battle battle = Battle.of(teamA, teamB, BattleContext.BattleScene.TRAINING, maxRounds);
    BattleResultVO result = battle.execute(combatEngine);
    if (result == null) {
      throw new IllegalStateException("战斗执行失败");
    }
    return result;
  }

  public CombatTeam buildPlayerTeam(Player user) {
    return teamBuilder.buildPlayerTeam(user);
  }

  public CombatTeam buildPlayerTeam(Player user, Map<Long, Skill> skillLookup) {
    return teamBuilder.buildPlayerTeam(
        user, new TeamBuilder.BuildOptions(skillLookup, "Player", null));
  }

  public CombatTeam buildPlayerTeam(Player user, Map<Long, Skill> skillLookup, String teamName) {
    return teamBuilder.buildPlayerTeam(
        user, new TeamBuilder.BuildOptions(skillLookup, teamName, null));
  }

  public CombatTeam buildPlayerTeam(
      Player user,
      Map<Long, Skill> skillLookup,
      String teamName,
      @Nullable List<Beast> deployedBeasts) {
    return teamBuilder.buildPlayerTeam(
        user, new TeamBuilder.BuildOptions(skillLookup, teamName, deployedBeasts));
  }

  /** 计算队伍属性统计 */
  public record TeamStats(int totalMaxHp, int avgAttack, int avgDef, int avgSpeed) {}

  public TeamStats calculateTeamStats(CombatTeam team) {
    List<Combatant> members = team.members();
    int totalMaxHp = 0, totalAtk = 0, totalDef = 0, totalSpd = 0;
    int count = 0;
    for (Combatant c : members) {
      if (c.isAlive()) {
        totalMaxHp += c.getMaxHp();
        totalAtk += c.getAttack();
        totalDef += c.getDefense();
        totalSpd += c.getSpeed();
        count++;
      }
    }
    count = Math.max(1, count);
    return new TeamStats(totalMaxHp, totalAtk / count, totalDef / count, totalSpd / count);
  }
}
