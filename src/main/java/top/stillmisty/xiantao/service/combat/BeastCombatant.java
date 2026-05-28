package top.stillmisty.xiantao.service.combat;

import java.util.List;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.entity.MutationEffect;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.beast.enums.TriggerType;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.service.beast.MutationEffectResolver;

public class BeastCombatant implements Combatant {
  private final Beast beast;
  private final List<Skill> skills;
  private final MutationEffectResolver effectResolver;
  private int hp;

  public BeastCombatant(Beast beast, MutationEffectResolver effectResolver) {
    this(beast, List.of(), effectResolver);
  }

  public BeastCombatant(Beast beast, List<Skill> skills, MutationEffectResolver effectResolver) {
    this.beast = beast;
    this.skills = skills != null ? skills : List.of();
    this.effectResolver = effectResolver;
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
    double bonus = effectResolver.sumEffectValue(beast, MutationEffectType.ATTACK_PERCENT);
    double mult = 1.0 + bonus / 100;

    List<MutationEffect> berserkEffects =
        effectResolver.getConditionalEffects(beast, MutationEffectType.LOW_HP_ATTACK_BOOST);
    for (MutationEffect effect : berserkEffects) {
      if (effect.condition() != null && effect.condition().trigger() == TriggerType.HP_BELOW) {
        double hpPercent = (double) hp / getMaxHp() * 100;
        if (hpPercent < effect.condition().threshold()) {
          mult += effect.value() / 100;
        }
      }
    }
    return mult;
  }

  private double getDefenseMultiplier() {
    double bonus = effectResolver.sumEffectValue(beast, MutationEffectType.DEFENSE_PERCENT);
    return 1.0 + bonus / 100;
  }

  private double getSpeedMultiplier() {
    double bonus = effectResolver.sumEffectValue(beast, MutationEffectType.SPEED_PERCENT);
    return 1.0 + bonus / 100;
  }
}
