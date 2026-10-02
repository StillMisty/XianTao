package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.vo.EquipmentStats;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.Player;

/** 玩家战斗单位 */
public class PlayerCombatant implements Combatant {
  private final Player user;
  @Nullable private final Equipment weapon;
  private final double attackSpeed;
  private final List<Skill> skills;
  private final EquipmentStats equipmentStats;
  private int hp;
  private int attackBuff;
  private int defenseBuff;
  private int speedBuff;

  public PlayerCombatant(Player user, @Nullable Equipment weapon, double attackSpeed) {
    this(user, weapon, attackSpeed, List.of(), EquipmentStats.EMPTY);
  }

  public PlayerCombatant(
      Player user, @Nullable Equipment weapon, double attackSpeed, List<Skill> skills) {
    this(user, weapon, attackSpeed, skills, EquipmentStats.EMPTY);
  }

  public PlayerCombatant(
      Player user,
      @Nullable Equipment weapon,
      double attackSpeed,
      List<Skill> skills,
      EquipmentStats equipmentStats) {
    this.user = user;
    this.hp = user.getHpCurrent();
    this.weapon = weapon;
    this.attackSpeed = attackSpeed;
    this.skills = skills != null ? skills : List.of();
    this.equipmentStats = equipmentStats;
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
    return (user.getEffectiveStatAgi() + equipmentStats.agi()) * 2 + 10 + speedBuff;
  }

  @Override
  public int getAttack() {
    return (user.getEffectiveStatStr() + equipmentStats.str()) * 2
        + equipmentStats.attack()
        + attackBuff;
  }

  @Override
  public int getDefense() {
    return user.getEffectiveStatCon()
        + equipmentStats.con()
        + equipmentStats.defense()
        + defenseBuff;
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
    return skills;
  }

  @Nullable
  public WeaponType getWeaponType() {
    return weapon != null ? weapon.getWeaponType() : null;
  }

  public int getWis() {
    return user.getEffectiveStatWis() + equipmentStats.wis();
  }

  public int getStr() {
    return user.getEffectiveStatStr() + equipmentStats.str();
  }

  public int getAgi() {
    return user.getEffectiveStatAgi() + equipmentStats.agi();
  }

  @Override
  public double getAttackSpeed() {
    return attackSpeed;
  }
}
