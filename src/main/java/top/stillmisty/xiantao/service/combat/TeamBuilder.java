package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.Player;

/**
 * 玩家队伍构建接口
 *
 * <p>将队伍构建逻辑与战斗模拟解耦，支持不同场景（历练/秘境/PvP）的差异化构建。 默认实现从数据库加载装备、技能、丹药 Buff 和灵兽来组装队伍。
 */
public interface TeamBuilder {

  record BuildOptions(
      Map<Long, Skill> skillLookup, String teamName, @Nullable List<Beast> deployedBeasts) {

    public static final BuildOptions DEFAULT = new BuildOptions(Map.of(), "Player", null);

    public BuildOptions withSkillLookup(Map<Long, Skill> skillLookup) {
      return new BuildOptions(skillLookup, teamName, deployedBeasts);
    }

    public BuildOptions withTeamName(String teamName) {
      return new BuildOptions(skillLookup, teamName, deployedBeasts);
    }

    public BuildOptions withDeployedBeasts(@Nullable List<Beast> deployedBeasts) {
      return new BuildOptions(skillLookup, teamName, deployedBeasts);
    }
  }

  CombatTeam buildPlayerTeam(Player user);

  CombatTeam buildPlayerTeam(Player user, BuildOptions options);
}
