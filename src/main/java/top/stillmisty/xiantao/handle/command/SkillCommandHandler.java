package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.skill.vo.SkillVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SkillService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillCommandHandler implements CommandGroup {

  private final SkillService skillService;

  // ===================== 委托方法（纯文本） =====================

  public String handleEquippedSkills(PlatformType platform, String openId) {
    return handleEquippedSkills(platform, openId, TextFormat.PLAIN);
  }

  public String handleLearnedSkills(PlatformType platform, String openId) {
    return handleLearnedSkills(platform, openId, TextFormat.PLAIN);
  }

  public String handleEquipSkill(PlatformType platform, String openId, String skillInput) {
    return handleEquipSkill(platform, openId, skillInput, TextFormat.PLAIN);
  }

  public String handleUnequipSkill(PlatformType platform, String openId, String skillInput) {
    return handleUnequipSkill(platform, openId, skillInput, TextFormat.PLAIN);
  }

  // ===================== 委托方法（Markdown） =====================

  public String handleEquippedSkillsMarkdown(PlatformType platform, String openId) {
    return handleEquippedSkills(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleLearnedSkillsMarkdown(PlatformType platform, String openId) {
    return handleLearnedSkills(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleEquipSkillMarkdown(PlatformType platform, String openId, String skillInput) {
    return handleEquipSkill(platform, openId, skillInput, TextFormat.MARKDOWN);
  }

  public String handleUnequipSkillMarkdown(
      PlatformType platform, String openId, String skillInput) {
    return handleUnequipSkill(platform, openId, skillInput, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleEquippedSkills(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("查询已装载法决 - Platform: {}, OpenId: {}", platform, openId);
    return switch (skillService.getEquippedSkills(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var skills) -> formatEquippedSkills(skills, fmt);
    };
  }

  public String handleLearnedSkills(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("查询已学法决 - Platform: {}, OpenId: {}", platform, openId);
    return switch (skillService.getLearnedSkills(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var skills) -> formatLearnedSkills(skills, fmt);
    };
  }

  public String handleEquipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    log.debug("装载法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return switch (skillService.equipSkill(platform, openId, skillInput)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatSkillSlotResult(result, fmt);
    };
  }

  public String handleUnequipSkill(
      PlatformType platform, String openId, String skillInput, TextFormat fmt) {
    log.debug("卸下法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return switch (skillService.unequipSkill(platform, openId, skillInput)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatSkillSlotResult(result, fmt);
    };
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
        var effect = skill.effects().get(i);
        if (i > 0) effectText.append(" + ");
        effectText.append(effect.type().getName());
        if (effect.formula() != null) effectText.append("(").append(effect.formula()).append(")");
        if (effect.value() != null)
          effectText.append("(").append(String.format("%.0f%%", effect.value() * 100)).append(")");
        if (effect.duration() != null)
          effectText.append(" ").append(effect.duration()).append("回合");
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
        new CommandEntry("法决", "查看已装载法决", "法决"),
        new CommandEntry("法决列表", "查看所有已学法决", "法决列表"),
        new CommandEntry("法决装载 {{法决}}", "装载法决到槽位", "法决装载 御剑术"),
        new CommandEntry("法决卸下 {{法决}}", "从槽位卸下法决", "法决卸下 御剑术"));
  }
}
