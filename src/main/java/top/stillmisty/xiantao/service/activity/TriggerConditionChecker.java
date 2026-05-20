package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 隐藏事件触发条件检查器 — 统一的 HAS_SKILL / HAS_ITEM / STAT_THRESHOLD 检查 */
@Component
@RequiredArgsConstructor
public class TriggerConditionChecker {

  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;

  public boolean check(ActivityEvent event, Long userId, User user) {
    String triggerType = event.getTriggerType();
    Map<String, Object> triggerParams = event.getTriggerParams();
    if (triggerType == null || triggerParams == null) return true;

    return switch (triggerType) {
      case "HAS_SKILL" -> hasSkill(userId, (String) triggerParams.get("skill_name"));
      case "HAS_ITEM" -> hasItem(userId, (String) triggerParams.get("item_name"));
      case "STAT_THRESHOLD" -> checkStatThreshold(triggerParams, user);
      default -> true;
    };
  }

  private boolean hasSkill(Long userId, String skillName) {
    if (skillName == null) return false;
    return skillRepository.findByName(skillName).stream()
        .anyMatch(
            skill ->
                playerSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent());
  }

  private boolean hasItem(Long userId, String itemName) {
    if (itemName == null) return false;
    return itemTemplateRepository
        .findByName(itemName)
        .map(
            template ->
                stackableItemRepository
                    .findByUserIdAndTemplateId(userId, template.getId())
                    .isPresent())
        .orElse(false);
  }

  private boolean checkStatThreshold(Map<String, Object> triggerParams, User user) {
    String stat = (String) triggerParams.get("stat");
    Number minVal = (Number) triggerParams.get("min");
    if (stat == null || minVal == null) return true;
    int actual =
        switch (stat) {
          case "STR" -> user.getEffectiveStatStr();
          case "CON" -> user.getEffectiveStatCon();
          case "AGI" -> user.getEffectiveStatAgi();
          case "WIS" -> user.getEffectiveStatWis();
          default -> 0;
        };
    return actual >= minVal.intValue();
  }
}
