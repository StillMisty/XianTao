package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

public interface Combatant {

  Long getId();

  String getName();

  int getSpeed();

  int getAttack();

  int getDefense();

  int getHp();

  int getMaxHp();

  /** 获取攻击速度（影响CD恢复速度） 默认值1.0 */
  default double getAttackSpeed() {
    return 1.0;
  }

  void takeDamage(int amount);

  /**
   * 恢复生命值
   *
   * @param amount 恢复量
   */
  void heal(int amount);

  boolean isAlive();

  List<Skill> getSkills();
}
