package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.StackableItemService;

/** 历练结算器 — 处理历练结算的子事件和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingCompleter {

  private static final String ACTIVITY_TYPE = "TRAINING";

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;

  /** 历练结算完成叙事 */
  public void produceCompletionEvent(
      Long userId, User user, MapNode mapNode, long minutesTraining) {
    Map<String, Object> args = new HashMap<>();
    args.put("mapName", mapNode.getName());
    args.put("minutes", minutesTraining);
    gameEventService.createEvent(
        userId, GameEventCategory.TRAINING_COMPLETE, "你在{{mapName}}历练了 {{minutes}} 分钟，有所收获。", args);
  }

  /** 历练中断叙事 */
  public void produceInterruptedEvent(Long userId, MapNode mapNode) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName());
    gameEventService.createEvent(
        userId, GameEventCategory.TRAINING_INTERRUPTED, "你在{{mapName}}的历练因重伤而中断。", args);
  }

  /**
   * 历练子事件: 每 15 分钟 roll 1 次，约 30% 概率触发
   *
   * @return 返回处理结果列表 (供 BountyCompleter 使用)
   */
  public void rollSubEvents(Long userId, User user, MapNode mapNode, long minutesTraining) {
    int rolls = Math.max(1, (int) (minutesTraining / 15));
    for (int i = 0; i < rolls; i++) {
      ActivityEvent selected = subEventSelector.selectSubEvent(ACTIVITY_TYPE, mapNode.getId(), 0.3);
      if (selected == null) continue;
      processTrainingSubEvent(userId, selected, user, mapNode);
    }
  }

  /** 检查历练隐藏事件 (结算时触发) */
  public void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var hiddenEvents = subEventSelector.findHiddenEvents(ACTIVITY_TYPE, mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ACTIVITY_TYPE, mapNode.getId(), event.getCode());
      if (alreadyDone) continue;

      // Stub: check trigger conditions based on user state
      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ACTIVITY_TYPE, mapNode.getId(), event.getCode()));

      Map<String, Object> args = new HashMap<>();
      args.put("mapName", mapNode.getName());
      args.put("eventName", event.getCode());
      gameEventService.createEvent(
          userId,
          GameEventCategory.TRAINING_HIDDEN,
          "你在{{mapName}}深处触发了一处修炼机缘：{{eventName}}。",
          args);
    }
  }

  private void processTrainingSubEvent(
      Long userId, ActivityEvent event, User user, MapNode mapNode) {
    String code = event.getCode();
    switch (code) {
      case "SPIRIT_SPRING" -> {
        long exp = 20 + (long) (Math.random() * 30);
        user.addExp(exp);
        Map<String, Object> args = Map.of("exp", exp);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAINING_EVENT, "你发现了一处灵泉，吸收灵力获得 +{{exp}} exp。", args);
      }
      case "RARE_BEAST" -> {
        long exp = 30 + (long) (Math.random() * 50);
        user.addExp(exp);
        Map<String, Object> args = Map.of("exp", exp);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAINING_EVENT, "你遭遇了一只罕见灵兽，击败后获得 +{{exp}} exp。", args);
      }
      case "HIDDEN_CAVE" -> {
        dropSpecialtyItem(userId, mapNode);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAINING_EVENT, "你拨开密林，发现一处隐秘山洞，拾得一些物品。", Map.of());
      }
      case "QI_DEVIATION" -> {
        int damage = (int) (user.calculateMaxHp() * 0.1);
        user.takeDamage(damage);
        Map<String, Object> args = Map.of("damage", damage);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAINING_EVENT, "你在修炼时遭遇气机紊乱，受到 {{damage}} 点伤害。", args);
      }
      case "HERB_GARDEN" -> {
        dropSpecialtyItem(userId, mapNode);
        gameEventService.createEvent(
            userId, GameEventCategory.TRAINING_EVENT, "你偶然发现一片药园，采集了一些药材。", Map.of());
      }
      case "NOTHING" -> {
        // no event
      }
      default -> log.debug("Unknown training sub-event: {}", code);
    }
  }

  private void dropSpecialtyItem(Long userId, MapNode mapNode) {
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return;

    SpecialtyEntry entry =
        WeightedRandom.select(
            specialties, SpecialtyEntry::weight, java.util.concurrent.ThreadLocalRandom.current());
    if (entry == null) return;

    ItemTemplate template = itemTemplateRepository.findById(entry.templateId()).orElse(null);
    if (template == null) return;

    stackableItemService.addStackableItem(
        userId, entry.templateId(), template.getType(), template.getName(), 1);
  }
}
