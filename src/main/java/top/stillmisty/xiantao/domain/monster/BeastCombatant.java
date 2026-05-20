package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

/** 灵兽战斗单位 — 特性加成在此计算 */
public class BeastCombatant implements Combatant {
  private final Beast beast;
  private final List<Skill> skills;
  private int hp;

  public BeastCombatant(Beast beast) {
    this(beast, List.of());
  }

  public BeastCombatant(Beast beast, List<Skill> skills) {
    this.beast = beast;
    this.skills = skills != null ? skills : List.of();
    this.hp = beast.getHpCurrent() != null ? beast.getHpCurrent() : beast.getMaxHp();
  }

  @Override
  public Long getId() {
    return beast.getId();
  }

  @Override
  public String getName() {
    return beast.getBeastName() != null ? beast.getBeastName() : "灵兽#" + beast.getId();
  }

  @Override
  public int getSpeed() {
    int base = beast.getLevel() != null ? beast.getLevel() * 2 + 8 : 10;
    return (int) (base * getSpeedMultiplier());
  }

  @Override
  public int getAttack() {
    int base = beast.getAttack() != null ? beast.getAttack() : 10;
    return (int) (base * getAttackMultiplier());
  }

  @Override
  public int getDefense() {
    int base = beast.getDefense() != null ? beast.getDefense() : 8;
    return (int) (base * getDefenseMultiplier());
  }

  @Override
  public int getHp() {
    return hp;
  }

  @Override
  public int getMaxHp() {
    return beast.getMaxHp() != null ? beast.getMaxHp() : 100;
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

  private double getAttackMultiplier() {
    double mult = 1.0;
    List<String> traits = beast.getMutationTraits();
    if (traits != null) {
      if (traits.contains("SHARP_FANG")) mult += 0.15;
      if (traits.contains("MALEVOLENCE")) mult += 0.25;
    }
    return mult;
  }

  private double getDefenseMultiplier() {
    double mult = 1.0;
    List<String> traits = beast.getMutationTraits();
    if (traits != null) {
      if (traits.contains("THICK_SKIN")) mult += 0.15;
      if (traits.contains("MYSTIC_ARMOR")) mult += 0.25;
    }
    return mult;
  }

  private double getSpeedMultiplier() {
    double mult = 1.0;
    List<String> traits = beast.getMutationTraits();
    if (traits != null) {
      if (traits.contains("SWIFT")) mult += 0.15;
      if (traits.contains("LIGHTNING_CHASE")) mult += 0.30;
    }
    return mult;
  }
}
