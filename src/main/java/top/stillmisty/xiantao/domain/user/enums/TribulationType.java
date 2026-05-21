package top.stillmisty.xiantao.domain.user.enums;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

/** 雷劫类型 — 七种天劫，各有不同难度和战斗技能 */
@Getter
public enum TribulationType {
  THREE_PURE("三清雷劫", 1.0, 1, 0.55, List.of()),

  PURPLE_EMP(
      "紫霄神雷",
      1.3,
      1,
      0.22,
      List.of(
          new TribulationSkillSpec(
              "紫霄贯体",
              3,
              List.of(
                  SkillEffect.damage("attack*1.8"),
                  new SkillEffect(
                      EffectType.EXECUTE, "attack*1.5", 0.3, null, null, null, null, null))))),

  AZURE_EAST(
      "太乙青雷",
      1.3,
      2,
      0.12,
      List.of(new TribulationSkillSpec("青雷蚀骨", 4, List.of(SkillEffect.dot(0.15, 3, 3))))),

  GOLDEN_NINE(
      "九霄金雷",
      1.6,
      4,
      0.07,
      List.of(new TribulationSkillSpec("金雷破罡", 4, List.of(SkillEffect.armorBreak(0.3, 3))))),

  DARK_VOID(
      "玄冥黑雷",
      1.6,
      5,
      0.03,
      List.of(
          new TribulationSkillSpec(
              "冥雷封魂",
              5,
              List.of(new SkillEffect(EffectType.FREEZE, null, 1.0, 2, null, null, null, null))))),

  ANNIHILATION(
      "灭世神雷",
      2.0,
      7,
      0.009,
      List.of(
          new TribulationSkillSpec(
              "灭世雷暴",
              5,
              List.of(
                  SkillEffect.damage("attack*1.5"),
                  new SkillEffect(
                      EffectType.AOE_DAMAGE, "attack*1.3", null, null, null, null, null, null))))),

  NINE_COLOR(
      "九色神雷",
      3.0,
      8,
      0.001,
      List.of(
          new TribulationSkillSpec(
              "九色天罚",
              4,
              List.of(
                  SkillEffect.damage("attack*2.5"),
                  SkillEffect.dot(0.1, 3, 5),
                  SkillEffect.armorBreak(0.2, 3),
                  new SkillEffect(EffectType.FREEZE, null, 1.0, 1, null, null, null, null)))));

  private final String displayName;
  private final double difficultyMultiplier;
  private final int minRealmOrdinal;
  private final double weight;
  private final List<TribulationSkillSpec> skillSpecs;

  TribulationType(
      String displayName,
      double difficultyMultiplier,
      int minRealmOrdinal,
      double weight,
      List<TribulationSkillSpec> skillSpecs) {
    this.displayName = displayName;
    this.difficultyMultiplier = difficultyMultiplier;
    this.minRealmOrdinal = minRealmOrdinal;
    this.weight = weight;
    this.skillSpecs = skillSpecs;
  }

  /** 根据目标境界序号随机选择雷劫类型。 渡劫期内部等级（isTribulationRealm=true）始终解锁全类型。 */
  public static TribulationType randomForBreakthrough(
      int targetRealmOrdinal, boolean isTribulationRealm) {
    List<TribulationType> candidates =
        java.util.Arrays.stream(values())
            .filter(t -> isTribulationRealm || t.minRealmOrdinal <= targetRealmOrdinal)
            .toList();
    if (candidates.isEmpty()) {
      return THREE_PURE;
    }
    double totalWeight = candidates.stream().mapToDouble(TribulationType::getWeight).sum();
    double roll = ThreadLocalRandom.current().nextDouble() * totalWeight;
    double cumulative = 0;
    for (TribulationType t : candidates) {
      cumulative += t.weight;
      if (roll <= cumulative) {
        return t;
      }
    }
    return candidates.getLast();
  }

  /** 生成该雷劫类型的技能列表（in-memory Skill 对象，不写DB） */
  public List<Skill> buildSkills() {
    return skillSpecs.stream().map(spec -> spec.toSkill()).toList();
  }

  /** 雷劫技能规格，构造时转为 Skill 对象 */
  public record TribulationSkillSpec(String name, int cooldownSeconds, List<SkillEffect> effects) {
    Skill toSkill() {
      Skill skill = new Skill();
      skill.setName(name);
      skill.setCooldownSeconds(cooldownSeconds);
      skill.setEffects(effects);
      return skill;
    }
  }
}
