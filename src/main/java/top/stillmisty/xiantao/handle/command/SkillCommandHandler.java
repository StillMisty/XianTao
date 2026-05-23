package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.skill.vo.SkillVO;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
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
    return equippedPart + fmt.separator() + learnedPart;
  }

  public String handleEquipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    if (skillInput == null || skillInput.isBlank()) {
      return "用法：法决装载 [法决名称或编号]\n示例：法决装载 御剑术";
    }
    log.debug("装载法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return CommandHandlerHelper.safeCall(
        () -> skillService.equipSkill(platform, openId, skillInput),
        fmt,
        result -> formatSkillSlotResult(result, fmt));
  }

  public String handleUnequipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    if (skillInput == null || skillInput.isBlank()) {
      return "用法：法决卸下 [法决名称或编号]\n示例：法决卸下 御剑术";
    }
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
    sb.append("\n");

    if (skill.effects() != null && !skill.effects().isEmpty()) {
      var effectText = new StringBuilder("效果：");
      for (int i = 0; i < skill.effects().size(); i++) {
        if (i > 0) effectText.append(" + ");
        effectText.append(SkillFormatter.formatEffect(skill.effects().get(i)));
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
            "调息："
                + skill.cooldownSeconds()
                + "息 | 修为："
                + CultivationRealm.realmDisplay(skill.levelRequirement())));
    if (skill.description() != null && !skill.description().isBlank()) {
      sb.append(fmt.listItem(skill.description()));
    }
    return sb.toString();
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
