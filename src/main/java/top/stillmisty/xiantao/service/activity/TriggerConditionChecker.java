package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;

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
      case "HAS_SKILL" -> {
        Long val = TypeUtils.getLong(triggerParams, "skill_id");
        yield val != null && hasSkill(userId, val);
      }
      case "HAS_ITEM" -> {
        Long val = TypeUtils.getLong(triggerParams, "item_template_id");
        yield val != null && hasItem(userId, val);
      }
      case "STAT_THRESHOLD" -> checkStatThreshold(triggerParams, user);
      default -> true;
    };
  }

  private boolean hasSkill(Long userId, Long skillId) {
    if (skillId == null) return false;
    return skillRepository
        .findById(skillId)
        .map(
            skill ->
                playerSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent())
        .orElse(false);
  }

  private boolean hasItem(Long userId, Long itemTemplateId) {
    if (itemTemplateId == null) return false;
    return itemTemplateRepository
        .findById(itemTemplateId)
        .map(
            template ->
                stackableItemRepository
                    .findByUserIdAndTemplateId(userId, template.getId())
                    .isPresent())
        .orElse(false);
  }

  private boolean checkStatThreshold(Map<String, Object> triggerParams, User user) {
    String stat = TypeUtils.getString(triggerParams, "stat");
    Integer minVal = TypeUtils.getInt(triggerParams, "min");
    if (stat == null || minVal == null) return true;
    int actual =
        switch (stat) {
          case "STR" -> user.getEffectiveStatStr();
          case "CON" -> user.getEffectiveStatCon();
          case "AGI" -> user.getEffectiveStatAgi();
          case "WIS" -> user.getEffectiveStatWis();
          default -> 0;
        };
    return actual >= minVal;
  }
}
