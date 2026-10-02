package top.stillmisty.xiantao.domain.skill.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;
import top.stillmisty.xiantao.domain.skill.enums.SkillType;

class PassiveSkillBonusesTest {

  @Test
  void aggregatesPermanentPassiveEffectsAsPercent() {
    Skill defense = passive("金刚体", effect(EffectType.DEFENSE_BUFF, 10.0, null));
    Skill hp = passive("灵力护体", effect(EffectType.HP_BUFF, 8.0, null));
    Skill resist = passive("吐纳术", effect(EffectType.RESIST_BUFF, 5.0, null));
    Skill survive = passive("金蝉脱壳", effect(EffectType.SURVIVE_LETHAL, 20.0, null));

    PassiveSkillBonuses bonuses =
        PassiveSkillBonuses.aggregate(List.of(defense, hp, resist, survive));

    assertEquals(0.10, bonuses.defensePercent(), 1e-9);
    assertEquals(0.08, bonuses.hpPercent(), 1e-9);
    assertEquals(0.05, bonuses.resistPercent(), 1e-9);
    assertEquals(0.20, bonuses.surviveLethalPercent(), 1e-9);
  }

  @Test
  void ignoresActiveSkillsAndTemporaryEffects() {
    Skill active = new Skill();
    active.setSkillType(SkillType.ACTIVE);
    active.setEffects(List.of(effect(EffectType.ATTACK_BUFF, 50.0, null)));

    Skill passive = passive("莲华涅槃", effect(EffectType.DEFENSE_BUFF, 50.0, 1));

    PassiveSkillBonuses bonuses = PassiveSkillBonuses.aggregate(List.of(active, passive));

    assertEquals(PassiveSkillBonuses.NONE, bonuses);
  }

  private static Skill passive(String name, SkillEffect effect) {
    Skill skill = new Skill();
    skill.setName(name);
    skill.setSkillType(SkillType.PASSIVE);
    skill.setEffects(List.of(effect));
    return skill;
  }

  private static SkillEffect effect(EffectType type, double value, @Nullable Integer duration) {
    return new SkillEffect(type, null, value, duration, null, null, null, null);
  }
}
