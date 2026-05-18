package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

/** 旅行结算器 — 处理旅行到达、子事件、隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TravelCompleter {

  private static final double SUB_EVENT_CHANCE = 1.0; // 到达时必定 roll 一次
  private static final String ACTIVITY_TYPE = "TRAVEL";

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final StackableItemRepository stackableItemRepository;

  /** 处理旅行到达事件 (子事件和隐藏事件) */
  public void completeTravel(Long userId, User user, MapNode fromMap, MapNode toMap) {
    GameEventCategory arrivalCategory = GameEventCategory.TRAVEL_ARRIVED;
    Map<String, Object> arrivalArgs = new HashMap<>();
    arrivalArgs.put("from", fromMap.getName());
    arrivalArgs.put("to", toMap.getName());
    arrivalArgs.put("mapName", toMap.getName());
    arrivalArgs.put("mapDescription", toMap.getDescription() != null ? toMap.getDescription() : "");
    gameEventService.createEvent(userId, arrivalCategory, "你经过一路跋涉，终于抵达了{{to}}。", arrivalArgs);

    rollSubEvents(userId, user, toMap);
    checkHiddenEvents(userId, user, toMap);
  }

  /** 旅行子事件: 到达时 roll 1 次 */
  private void rollSubEvents(Long userId, User user, MapNode mapNode) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(ACTIVITY_TYPE, mapNode.getId(), SUB_EVENT_CHANCE);
    if (selected == null) return;

    processTravelSubEvent(userId, selected, user);
  }

  /** 处理旅行子事件 (独立发奖) */
  private void processTravelSubEvent(Long userId, ActivityEvent event, User user) {
    String code = event.getCode();
    Map<String, Object> params = event.getParams() != null ? event.getParams() : Map.of();

    switch (code) {
      case "AMBUSH" -> {
        int damage = getIntParam(params, "damage", 10, 30);
        user.takeDamage(damage);
        if (user.getHpCurrent() <= 0) {
          user.setDying();
        }
        Map<String, Object> args = Map.of("damage", damage, "hpCurrent", user.getHpCurrent());
        gameEventService.createEvent(
            userId,
            GameEventCategory.TRAVEL_EVENT,
            "你在旅途中遭遇伏击，受到 {{damage}} 点伤害（剩余 HP: {{hpCurrent}}）。",
            args);
      }
      case "FIND_TREASURE" -> {
        long stones = getLongParam(params, "spiritStones", 5, 20);
        user.setSpiritStones(user.getSpiritStones() + stones);
        Map<String, Object> args = Map.of("spiritStones", stones);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAVEL_EVENT, "你途经一处遗迹，意外发现了 {{spiritStones}} 灵石。", args);
      }
      case "WEATHER" ->
          gameEventService.createEvent(
              userId, GameEventCategory.TRAVEL_EVENT, "你遭遇了恶劣天气，艰难穿过才得以继续前行。", Map.of());
      case "ENLIGHTENMENT" -> {
        long exp = getLongParam(params, "exp", 15, 50);
        user.addExp(exp);
        Map<String, Object> args = Map.of("exp", exp);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAVEL_EVENT, "你途经竹林时，忽有所悟，顿悟 +{{exp}} exp。", args);
      }
      case "SAFE_PASSAGE" -> {
        // safe passage: no event text needed
      }
      default -> log.debug("未知旅行子事件: {}", code);
    }
  }

  /** 检查旅行隐藏事件 */
  private void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var hiddenEvents = subEventSelector.findHiddenEvents(ACTIVITY_TYPE, mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ACTIVITY_TYPE, mapNode.getId(), event.getCode());
      if (alreadyDone) continue;

      boolean triggered = checkTriggerConditions(event, user);
      if (triggered) {
        hiddenCompletionRepository.save(
            top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
                userId, ACTIVITY_TYPE, mapNode.getId(), event.getCode()));

        Map<String, Object> args = new HashMap<>();
        args.put("mapName", mapNode.getName());
        args.put("eventName", event.getCode());
        gameEventService.createEvent(
            userId, GameEventCategory.TRAVEL_HIDDEN, "你在{{mapName}}发现了隐藏的秘密：{{eventName}}。", args);
      }
    }
  }

  private boolean checkTriggerConditions(ActivityEvent event, User user) {
    String triggerType = event.getTriggerType();
    Map<String, Object> triggerParams = event.getTriggerParams();
    if (triggerType == null || triggerParams == null) return true;

    return switch (triggerType) {
      case "HAS_SKILL" -> hasSkill(user.getId(), (String) triggerParams.get("skill_name"));
      case "HAS_ITEM" -> hasItem(user.getId(), (String) triggerParams.get("item_name"));
      case "STAT_THRESHOLD" -> {
        Object statObj = triggerParams.get("stat");
        Object minObj = triggerParams.get("min");
        if (!(statObj instanceof String stat) || !(minObj instanceof Number min)) {
          yield true;
        }
        yield checkStatThresholdFromTrigger(stat, min.intValue(), user);
      }
      default -> true;
    };
  }

  private boolean hasSkill(Long userId, String skillName) {
    if (skillName == null) return true;
    List<Skill> skills = skillRepository.findByName(skillName);
    if (skills.isEmpty()) return false;
    return skills.stream()
        .anyMatch(
            skill ->
                playerSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent());
  }

  private boolean hasItem(Long userId, String itemName) {
    if (itemName == null) return true;
    List<StackableItem> items =
        stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
    return items.stream().anyMatch(item -> item.getName().equals(itemName));
  }

  private boolean checkStatThresholdFromTrigger(String stat, int min, User user) {
    return switch (stat) {
      case "STR" -> user.getEffectiveStatStr() >= min;
      case "CON" -> user.getEffectiveStatCon() >= min;
      case "AGI" -> user.getEffectiveStatAgi() >= min;
      case "WIS" -> user.getEffectiveStatWis() >= min;
      default -> true;
    };
  }

  private int getIntParam(Map<String, Object> params, String key, int min, int max) {
    if (params.containsKey(key)) return ((Number) params.get(key)).intValue();
    return min + (int) (Math.random() * (max - min + 1));
  }

  private long getLongParam(Map<String, Object> params, String key, long min, long max) {
    if (params.containsKey(key)) return ((Number) params.get(key)).longValue();
    return min + (long) (Math.random() * (max - min + 1));
  }
}
