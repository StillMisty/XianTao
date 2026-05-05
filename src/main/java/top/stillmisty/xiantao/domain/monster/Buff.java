package top.stillmisty.xiantao.domain.monster;

import lombok.Builder;
import lombok.Getter;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;

/** Buff/Debuff实体 表示战斗中的增益/减益效果 */
@Getter
@Builder
public class Buff {

  /** Buff类型 */
  private final BuffType type;

  /**
   * 效果数值 破甲：降低防御的百分比（0.2 = 20%） 减速：降低速度的百分比（0.3 = 30%） 持续伤害：每回合伤害值 控制：持续回合数 治疗：恢复生命值
   * 增益：提升属性的百分比（0.2 = 20%）
   */
  private final double value;

  /** 持续回合数 */
  private int remainingTurns;

  /** 来源（技能名称） */
  private final String source;

  /** 是否可叠加 */
  @Builder.Default private final boolean stackable = false;

  /** 当前叠加层数 */
  @Builder.Default private int stackCount = 1;

  /** 最大叠加层数 */
  @Builder.Default private final int maxStacks = 1;

  /** 是否为负面效果 */
  public boolean isDebuff() {
    return switch (type) {
      case ARMOR_BREAK, SLOW, DOT, STUN, FREEZE, SILENCE -> true;
      case HEAL, ATTACK_BUFF, DEFENSE_BUFF, SPEED_BUFF -> false;
    };
  }

  /** 是否为控制效果 */
  public boolean isControl() {
    return switch (type) {
      case STUN, FREEZE, SILENCE -> true;
      default -> false;
    };
  }

  /** 是否为持续效果（每回合触发） */
  public boolean isOverTime() {
    return type == BuffType.DOT || type == BuffType.HEAL;
  }

  /** 回合结束时减少持续时间 */
  public void tick() {
    if (remainingTurns > 0) {
      remainingTurns--;
    }
  }

  /** 是否已过期 */
  public boolean isExpired() {
    return remainingTurns <= 0;
  }

  /**
   * 尝试叠加Buff
   *
   * @return 是否成功叠加
   */
  public boolean tryStack() {
    if (!stackable || stackCount >= maxStacks) {
      return false;
    }
    stackCount++;
    return true;
  }

  /** 刷新持续时间 */
  public void refreshDuration(int newDuration) {
    this.remainingTurns = Math.max(this.remainingTurns, newDuration);
  }
}
