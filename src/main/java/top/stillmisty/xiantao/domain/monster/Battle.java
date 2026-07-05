package top.stillmisty.xiantao.domain.monster;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;

/**
 * 战斗实体 — 单场战斗的完整生命周期
 *
 * <p>泛型设计：只操作 {@link Combatant} 接口和 {@link CombatTeam}，不关心具体类型。 战后结算（HP 回写、掉落计算、灵兽休养）由调用方处理。
 */
@Getter
public class Battle {

  private final CombatTeam teamA;
  private final CombatTeam teamB;
  private final BattleContext.BattleScene scene;
  private final int maxRounds;

  private @Nullable BattleResultVO result;
  private boolean executed;

  private Battle(
      CombatTeam teamA, CombatTeam teamB, BattleContext.BattleScene scene, int maxRounds) {
    this.teamA = teamA;
    this.teamB = teamB;
    this.scene = scene;
    this.maxRounds = maxRounds;
  }

  /**
   * 创建一场战斗
   *
   * @param teamA 攻击方
   * @param teamB 防守方
   * @param scene 战斗场景
   * @param maxRounds 最大回合数
   */
  public static Battle of(
      CombatTeam teamA, CombatTeam teamB, BattleContext.BattleScene scene, int maxRounds) {
    return new Battle(teamA, teamB, scene, maxRounds);
  }

  /** 执行战斗，返回结果 */
  @Nullable
  public BattleResultVO execute(CombatEngine combatEngine) {
    if (executed) return result;
    BattleContext context =
        BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(maxRounds).scene(scene).build();
    result = combatEngine.simulate(context);
    executed = true;
    return result;
  }

  /** teamA（攻击方）是否胜利 */
  public boolean isTeamAWin() {
    return result != null && teamA.name().equals(result.winner());
  }

  /** teamA（攻击方）是否全灭 */
  public boolean isTeamADead() {
    return teamA.isAllDead();
  }
}
