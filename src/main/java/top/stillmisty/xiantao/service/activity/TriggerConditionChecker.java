package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;

/**
 * 隐藏事件触发条件检查器 — 统一校验各 trigger_type。
 *
 * <p>参数键：HAS_SKILL `skill_id`、HAS_ITEM `item_template_id`、HAS_EQUIPMENT `equipment_template_id`、
 * STAT_THRESHOLD `stat`+`min`、LOCATION `map_node_id`、TIME_OF_DAY `period`（子时/午时…）或
 * `start_hour`+`end_hour`、 BEAST_DEPLOYED `beast_template_id`、LEVEL_RANGE `min`/`max`。
 *
 * <p>未知 trigger_type 与无法解析的参数一律放行（不抛异常），避免配置笔误导致隐藏事件静默失效。
 */
@Component
@RequiredArgsConstructor
public class TriggerConditionChecker {

  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final EquipmentRepository equipmentRepository;
  private final BeastRepository beastRepository;

  public boolean check(ActivityEvent event, Long userId, Player user) {
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
      case "HAS_EQUIPMENT" -> checkHasEquipment(userId, triggerParams);
      case "STAT_THRESHOLD" -> checkStatThreshold(triggerParams, user);
      case "LOCATION" -> checkLocation(user, triggerParams);
      case "TIME_OF_DAY" -> checkTimeOfDay(triggerParams);
      case "BEAST_DEPLOYED" -> checkBeastDeployed(userId, triggerParams);
      case "LEVEL_RANGE" -> checkLevelRange(triggerParams, user);
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

  /** 穿戴了指定装备模板（兼容 equipment_template_id / template_id 两种键名）。 */
  private boolean checkHasEquipment(Long userId, Map<String, Object> triggerParams) {
    Long configuredId = TypeUtils.getLong(triggerParams, "equipment_template_id");
    if (configuredId == null) {
      configuredId = TypeUtils.getLong(triggerParams, "template_id");
    }
    if (configuredId == null) return true;
    Long templateId = configuredId;
    return equipmentRepository.findEquippedByUserId(userId).stream()
        .anyMatch(equipment -> templateId.equals(equipment.getTemplateId()));
  }

  private boolean checkStatThreshold(Map<String, Object> triggerParams, Player user) {
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

  /** 途经/位于指定地图。 */
  private boolean checkLocation(Player user, Map<String, Object> triggerParams) {
    Long mapNodeId = TypeUtils.getLong(triggerParams, "map_node_id");
    if (mapNodeId == null) return true;
    return mapNodeId.equals(user.getLocationId());
  }

  /** 特定时间段：period 支持十二时辰（子时/丑时…，每段 2 小时，跨夜处理）；也可用 start_hour/end_hour。 */
  private boolean checkTimeOfDay(Map<String, Object> triggerParams) {
    String period = TypeUtils.getString(triggerParams, "period");
    if (period != null && !period.isBlank()) {
      Integer startHour = startHourOfPeriod(period.trim());
      if (startHour == null) return true;
      int hour = TimeUtil.now().getHour();
      return Math.floorMod(hour - startHour, 24) < 2;
    }
    Integer startHour = TypeUtils.getInt(triggerParams, "start_hour");
    Integer endHour = TypeUtils.getInt(triggerParams, "end_hour");
    if (startHour == null || endHour == null) return true;
    int hour = TimeUtil.now().getHour();
    return startHour <= endHour
        ? hour >= startHour && hour < endHour
        : hour >= startHour || hour < endHour;
  }

  private @Nullable Integer startHourOfPeriod(String period) {
    return switch (period) {
      case "子", "子时" -> 23;
      case "丑", "丑时" -> 1;
      case "寅", "寅时" -> 3;
      case "卯", "卯时" -> 5;
      case "辰", "辰时" -> 7;
      case "巳", "巳时" -> 9;
      case "午", "午时" -> 11;
      case "未", "未时" -> 13;
      case "申", "申时" -> 15;
      case "酉", "酉时" -> 17;
      case "戌", "戌时" -> 19;
      case "亥", "亥时" -> 21;
      default -> null;
    };
  }

  /** 出战了指定灵兽模板。 */
  private boolean checkBeastDeployed(Long userId, Map<String, Object> triggerParams) {
    Long templateId = TypeUtils.getLong(triggerParams, "beast_template_id");
    if (templateId == null) return true;
    return beastRepository.findDeployedByUserId(userId).stream()
        .anyMatch(beast -> templateId.equals(beast.getTemplateId()));
  }

  /** 等级在 [min, max] 区间；缺省边界表示不限。 */
  private boolean checkLevelRange(Map<String, Object> triggerParams, Player user) {
    Integer min = TypeUtils.getInt(triggerParams, "min");
    Integer max = TypeUtils.getInt(triggerParams, "max");
    int level = user.getLevel();
    return (min == null || level >= min) && (max == null || level <= max);
  }
}
