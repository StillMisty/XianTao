package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.vo.EquipmentStats;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.vo.PassiveSkillBonuses;
import top.stillmisty.xiantao.domain.user.entity.Player;

/** 玩家战斗单位 */
public class PlayerCombatant implements Combatant {
  private final Player user;
  @Nullable private final Equipment weapon;
  private final double attackSpeed;
  private final List<Skill> skills;
  private final EquipmentStats equipmentStats;
  private PassiveSkillBonuses passiveBonuses = PassiveSkillBonuses.NONE;
  private boolean surviveLethalUsed;
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

  /** 应用已习得被动法决的恒定加成（在构造后调用；气血按上限增幅同步放大，保持当前气血占比）。 */
  public PlayerCombatant withPassiveBonuses(PassiveSkillBonuses bonuses) {
    this.passiveBonuses = bonuses;
    if (bonuses.hpPercent() > 0) {
      this.hp = Math.min(getMaxHp(), (int) Math.round(this.hp * (1 + bonuses.hpPercent())));
    }
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
    int base = (user.getEffectiveStatAgi() + equipmentStats.agi()) * 2 + 10 + speedBuff;
    return applyPercent(base, passiveBonuses.speedPercent());
  }

  @Override
  public int getAttack() {
    int base =
        (user.getEffectiveStatStr() + equipmentStats.str()) * 2
            + equipmentStats.attack()
            + attackBuff;
    return applyPercent(base, passiveBonuses.attackPercent());
  }

  @Override
  public int getDefense() {
    int base =
        user.getEffectiveStatCon() + equipmentStats.con() + equipmentStats.defense() + defenseBuff;
    return applyPercent(base, passiveBonuses.defensePercent());
  }

  @Override
  public int getHp() {
    return hp;
  }

  @Override
  public int getMaxHp() {
    return applyPercent(user.calculateMaxHp(), passiveBonuses.hpPercent());
  }

  @Override
  public void takeDamage(int amount) {
    int remaining = hp - amount;
    if (hp > 0
        && remaining <= 0
        && passiveBonuses.surviveLethalPercent() > 0
        && !surviveLethalUsed) {
      // 濒死生存（SURVIVE_LETHAL）：每场战斗一次免死，保留 value% 气血
      surviveLethalUsed = true;
      hp = Math.max(1, (int) Math.round(getMaxHp() * passiveBonuses.surviveLethalPercent()));
      return;
    }
    hp = Math.max(0, remaining);
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

  /** 被动法决提供的闪避率（0-1） */
  public double getPassiveDodgePercent() {
    return passiveBonuses.dodgePercent();
  }

  /** 被动法决提供的受击减伤比例（0-1） */
  public double getPassiveResistPercent() {
    return passiveBonuses.resistPercent();
  }

  private static int applyPercent(int base, double percent) {
    if (percent == 0) return base;
    return Math.max(1, (int) Math.round(base * (1 + percent)));
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
