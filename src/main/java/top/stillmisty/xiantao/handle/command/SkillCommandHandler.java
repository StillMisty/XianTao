package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.skill.vo.SkillVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.skill.SkillService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillCommandHandler implements CommandGroup {

  private final SkillService skillService;

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleSkills(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("查询法决 - Platform: {}, OpenId: {}", platform, openId);
    var equippedPart =
        CommandHandlerHelper.safeCall(
            () -> skillService.getEquippedSkills(platform, openId),
            fmt,
            skills -> formatEquippedSkills(skills, fmt),
            msg -> "❌ " + msg);
    var learnedPart =
        CommandHandlerHelper.safeCall(
            () -> skillService.getLearnedSkills(platform, openId),
            fmt,
            skills -> formatLearnedSkills(skills, fmt),
            msg -> "❌ " + msg);
    return equippedPart + "\n\n" + learnedPart;
  }

  public String handleEquipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    log.debug("装载法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return CommandHandlerHelper.safeCall(
        () -> skillService.equipSkill(platform, openId, skillInput),
        fmt,
        result -> formatSkillSlotResult(result, fmt));
  }

  public String handleUnequipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    log.debug("卸下法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return CommandHandlerHelper.safeCall(
        () -> skillService.unequipSkill(platform, openId, skillInput),
        fmt,
        result -> formatSkillSlotResult(result, fmt));
  }

  // ===================== 统一格式化方法 =====================

  private String formatEquippedSkills(List<SkillVO> skills, TextFormat fmt) {
    if (skills.isEmpty()) {
      return "当前没有装载任何法决";
    }
    var sb = new StringBuilder();
    sb.append(fmt.heading("已装载法决", "📿"));
    for (int i = 0; i < skills.size(); i++) {
      sb.append(formatSkillDetail(i + 1, skills.get(i), fmt));
      if (i < skills.size() - 1) sb.append(fmt.separator());
    }
    return sb.toString();
  }

  private String formatLearnedSkills(List<SkillVO> skills, TextFormat fmt) {
    if (skills.isEmpty()) {
      return "你还没有学会任何法决\n使用「使用 [玉简名称]」从玉简中习得法决";
    }
    var sb = new StringBuilder();
    sb.append(fmt.heading("已学法决列表", "📖"));
    for (int i = 0; i < skills.size(); i++) {
      var skill = skills.get(i);
      sb.append(i + 1).append(". ").append(skill.name());
      if (skill.effects() != null && !skill.effects().isEmpty()) {
        sb.append(" [").append(skill.effects().getFirst().type().getName()).append("]");
      }
      if (skill.equipped()) sb.append(" ◆");
      sb.append("\n");
    }
    sb.append("\n◆ 标记表示已装载到槽位");
    return sb.toString();
  }

  private String formatSkillSlotResult(SkillSlotResult result, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(result.getMessage());
    if (result.getSkill() != null) {
      sb.append("\n\n");
      sb.append(formatSkillDetail(null, result.getSkill(), fmt));
    }
    if (result.getEquippedCount() > 0 || result.getMaxSlots() > 0) {
      sb.append("\n");
      sb.append(fmt.listItem("法决槽位：" + result.getEquippedCount() + "/" + result.getMaxSlots()));
    }
    return sb.toString();
  }

  private String formatSkillDetail(Integer index, SkillVO skill, TextFormat fmt) {
    var sb = new StringBuilder();
    if (index != null) {
      sb.append(index).append(". ");
    }
    sb.append(fmt.bold(skill.name()));
    if (skill.equipped()) sb.append(" ◆");
    sb.append("\n");

    if (skill.effects() != null && !skill.effects().isEmpty()) {
      var effectText = new StringBuilder("效果：");
      for (int i = 0; i < skill.effects().size(); i++) {
        if (i > 0) effectText.append(" + ");
        effectText.append(formatEffect(skill.effects().get(i)));
      }
      sb.append(fmt.listItem(effectText.toString()));
    }

    var bindingText = new StringBuilder("绑定：").append(skill.bindingTypeName());
    if (skill.bindingValue() != null && !skill.bindingValue().isBlank()) {
      bindingText.append("（").append(skill.bindingValue()).append("）");
    }
    sb.append(fmt.listItem(bindingText.toString()));

    sb.append(
        fmt.listItem(
            "CD：" + skill.cooldownSeconds() + "秒 | 等级要求：第" + skill.levelRequirement() + "层"));
    if (skill.description() != null && !skill.description().isBlank()) {
      sb.append(fmt.listItem("描述：" + skill.description()));
    }
    return sb.toString();
  }

  private String formatEffect(SkillEffect effect) {
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

  private String formatFormula(String formula) {
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

  @Override
  public String groupName() {
    return "法决";
  }

  @Override
  public String groupDescription() {
    return "学习、装载、卸下法决";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("法决", "查看已装载法决与已学法决列表", "法决"),
        new CommandEntry("法决装载 「法决」", "装载法决到槽位", "法决装载 御剑术"),
        new CommandEntry("法决卸下 「法决」", "从槽位卸下法决", "法决卸下 御剑术"));
  }
}
