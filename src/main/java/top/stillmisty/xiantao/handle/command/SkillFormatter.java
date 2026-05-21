package top.stillmisty.xiantao.handle.command;

import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

/** 技能效果格式化，供 SkillCommandHandler 和 CultivationCommandHandler 共用 */
final class SkillFormatter {

  private SkillFormatter() {}

  static String formatEffect(SkillEffect effect) {
    var name = effect.type().getName();
    var detail =
        switch (effect.type()) {
          case DAMAGE, AOE_DAMAGE -> formatFormula(effect.formula());
          case MULTI_HIT -> {
            int hits = effect.value() != null ? effect.value().intValue() : 3;
            yield formatFormula(effect.formula()) + "×" + hits + "次";
          }
          case EXECUTE -> {
            int threshold = effect.value() != null ? (int) (effect.value() * 100) : 30;
            yield formatFormula(effect.formula()) + "，血量<" + threshold + "%时双倍";
          }
          case HEAL -> formatFormula(effect.formula());
          case LIFESTEAL -> {
            int pct = effect.value() != null ? effect.value().intValue() : 25;
            yield "恢复" + pct + "%伤害为生命";
          }
          case ATTACK_BUFF, DEFENSE_BUFF, SPEED_BUFF -> {
            int pct = effect.value() != null ? effect.value().intValue() : 20;
            var dur = effect.duration() != null ? "，" + effect.duration() + "回合" : "";
            yield "+" + pct + "%" + dur;
          }
          case STUN, FREEZE, SILENCE -> {
            var prob = new StringBuilder();
            int chance = effect.chance() != null ? (int) (effect.chance() * 100) : 100;
            if (chance < 100) prob.append(chance).append("%概率");
            if (effect.duration() != null) {
              if (!prob.isEmpty()) prob.append("，");
              prob.append(effect.duration()).append("回合");
            }
            yield prob.toString();
          }
          case ARMOR_BREAK, SLOW -> {
            int pct = effect.value() != null ? effect.value().intValue() : 20;
            var dur = effect.duration() != null ? "，" + effect.duration() + "回合" : "";
            yield pct + "%" + dur;
          }
          case DOT -> {
            int pct = effect.value() != null ? effect.value().intValue() : 15;
            int rounds = effect.duration() != null ? effect.duration() : 3;
            int stacks = effect.maxStacks() != null ? effect.maxStacks() : 3;
            yield pct + "%攻击力×" + rounds + "回合，可叠" + stacks + "层";
          }
        };
    return name + (detail.isEmpty() ? "" : "(" + detail + ")");
  }

  static String formatFormula(String formula) {
    if (formula == null || formula.isBlank()) return "";
    return formula
        .replace("attack", "攻击")
        .replace("defense", "防御")
        .replace("str", "力道")
        .replace("con", "根骨")
        .replace("agi", "身法")
        .replace("wis", "悟性")
        .replace("*", "×");
  }
}
