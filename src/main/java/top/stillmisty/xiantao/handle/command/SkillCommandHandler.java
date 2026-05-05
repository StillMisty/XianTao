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
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SkillService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillCommandHandler implements CommandGroup {

  private final SkillService skillService;

  public String handleEquippedSkills(PlatformType platform, String openId) {
    log.debug("查询已装载法决 - Platform: {}, OpenId: {}", platform, openId);
    return switch (skillService.getEquippedSkills(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var skills) -> formatEquippedSkills(skills);
    };
  }

  public String handleLearnedSkills(PlatformType platform, String openId) {
    log.debug("查询已学法决 - Platform: {}, OpenId: {}", platform, openId);
    return switch (skillService.getLearnedSkills(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var skills) -> formatLearnedSkills(skills);
    };
  }

  public String handleEquipSkill(PlatformType platform, String openId, String skillInput) {
    log.debug("装载法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return switch (skillService.equipSkill(platform, openId, skillInput)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatSkillSlotResult(result);
    };
  }

  public String handleUnequipSkill(PlatformType platform, String openId, String skillInput) {
    log.debug("卸下法决 - Platform: {}, OpenId: {}, SkillInput: {}", platform, openId, skillInput);
    return switch (skillService.unequipSkill(platform, openId, skillInput)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatSkillSlotResult(result);
    };
  }

  // ===================== 文本格式化方法 =====================

  private String formatEquippedSkills(List<SkillVO> skills) {
    if (skills.isEmpty()) {
      return "当前没有装载任何法决";
    }
    var sb = new StringBuilder("【已装载法决】\n");
    for (int i = 0; i < skills.size(); i++) {
      sb.append(formatSkillDetail(i + 1, skills.get(i)));
      if (i < skills.size() - 1) sb.append("\n");
    }
    return sb.toString();
  }

  private String formatLearnedSkills(List<SkillVO> skills) {
    if (skills.isEmpty()) {
      return "你还没有学会任何法决\n使用「使用 [玉简名称]」从玉简中习得法决";
    }
    var sb = new StringBuilder("【已学法决列表】\n");
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

  private String formatSkillSlotResult(SkillSlotResult result) {
    var sb = new StringBuilder();
    sb.append(result.getMessage());
    if (result.getSkill() != null) {
      sb.append("\n\n");
      sb.append(formatSkillDetail(null, result.getSkill()));
    }
    if (result.getEquippedCount() > 0 || result.getMaxSlots() > 0) {
      sb.append("\n");
      sb.append("法决槽位：").append(result.getEquippedCount()).append("/").append(result.getMaxSlots());
    }
    return sb.toString();
  }

  private String formatSkillDetail(Integer index, SkillVO skill) {
    var sb = new StringBuilder();
    if (index != null) {
      sb.append(index).append(". ");
    }
    sb.append("【").append(skill.name()).append("】");
    if (skill.equipped()) sb.append(" ◆");
    sb.append("\n");

    // 显示所有效果
    if (skill.effects() != null && !skill.effects().isEmpty()) {
      sb.append("  效果：");
      for (int i = 0; i < skill.effects().size(); i++) {
        var effect = skill.effects().get(i);
        if (i > 0) sb.append(" + ");
        sb.append(effect.type().getName());
        if (effect.formula() != null) sb.append("(").append(effect.formula()).append(")");
        if (effect.value() != null)
          sb.append("(").append(String.format("%.0f%%", effect.value() * 100)).append(")");
        if (effect.duration() != null) sb.append(" ").append(effect.duration()).append("回合");
      }
      sb.append("\n");
    }

    sb.append("  绑定：").append(skill.bindingTypeName());
    if (skill.bindingValue() != null && !skill.bindingValue().isBlank()) {
      sb.append("（").append(skill.bindingValue()).append("）");
    }
    sb.append("\n");

    sb.append("  CD：").append(skill.cooldownSeconds()).append("秒");
    sb.append(" | 等级要求：第").append(skill.levelRequirement()).append("层");
    if (skill.description() != null && !skill.description().isBlank()) {
      sb.append("\n  描述：").append(skill.description());
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
