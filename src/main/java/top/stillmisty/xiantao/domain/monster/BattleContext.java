package top.stillmisty.xiantao.domain.monster;

import lombok.Builder;
import lombok.Getter;

/** 战斗上下文 封装战斗所需的所有状态和配置 */
@Getter
@Builder
public class BattleContext {

  /** 战斗队伍 */
  private final CombatTeam teamA;

  private final CombatTeam teamB;

  /** 最大回合数 */
  @Builder.Default private final int maxRounds = 20;

  /** 战斗场景类型 */
  @Builder.Default private final BattleScene scene = BattleScene.TRAINING;

  /** 战斗场景枚举 */
  @Getter
  public enum BattleScene {
    TRAINING("历练"),
    DUNGEON("秘境"),
    PVP("玩家对战"),
    BOUNTY("悬赏");

    private final String name;

    BattleScene(String name) {
      this.name = name;
    }
  }
}
