package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 玩家战斗单位 */
public class PlayerCombatant implements Combatant {
  private final User user;
  private final Equipment weapon;
  private final double attackSpeed;
  private int hp;
  private int attackBuff;
  private int defenseBuff;
  private int speedBuff;

  public PlayerCombatant(User user, Equipment weapon, double attackSpeed) {
    this.user = user;
    this.hp = user.getHpCurrent() != null ? user.getHpCurrent() : user.calculateMaxHp();
    this.weapon = weapon;
    this.attackSpeed = attackSpeed;
  }

  public PlayerCombatant withBuffs(int attackBuff, int defenseBuff, int speedBuff) {
    this.attackBuff = attackBuff;
    this.defenseBuff = defenseBuff;
    this.speedBuff = speedBuff;
    return this;
  }

  @Override
  public Long getId() {
    return user.getId();
  }

  @Override
  public String getName() {
    return user.getNickname();
  }

  @Override
  public int getSpeed() {
    return user.getEffectiveStatAgi() * 2 + 10 + speedBuff;
  }

  @Override
  public int getAttack() {
    int statValue = user.getEffectiveStatStr();
    int equipAttack = 0;
    if (weapon != null) {
      equipAttack = weapon.getFinalAttack();
    }
    return statValue * 2 + equipAttack + attackBuff;
  }

  @Override
  public int getDefense() {
    return user.getEffectiveStatCon() + defenseBuff;
  }

  @Override
  public int getHp() {
    return hp;
  }

  @Override
  public int getMaxHp() {
    return user.calculateMaxHp();
  }

  @Override
  public void takeDamage(int amount) {
    hp = Math.max(0, hp - amount);
  }

  @Override
  public void heal(int amount) {
    hp = Math.min(getMaxHp(), hp + amount);
  }

  @Override
  public boolean isAlive() {
    return hp > 0;
  }

  @Override
  public List<Skill> getSkills() {
    return List.of();
  }

  public WeaponType getWeaponType() {
    return weapon != null ? weapon.getWeaponType() : null;
  }

  public int getWis() {
    return user.getEffectiveStatWis();
  }

  @Override
  public double getAttackSpeed() {
    return attackSpeed;
  }
}
