package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;

/** 历练完成器 — 产出完成叙事、代理子事件/隐藏事件执行 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingCompleter {

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final TriggerConditionChecker triggerConditionChecker;
  private final ActivityEventHelper activityEventHelper;
  private final FortuneService fortuneService;

  public void produceCompletionEvent(
      Long userId, User user, MapNode mapNode, long minutesTraining) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName(), "minutes", minutesTraining);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_COMPLETE)
            .withNarrative("你在{{mapName}}历练了 {{minutes}} 分钟，有所收获。", args));
  }

  public void produceInterruptedEvent(Long userId, MapNode mapNode) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName());
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_INTERRUPTED)
            .withNarrative("你在{{mapName}}的历练因重伤而中断。", args));
  }

  /** 处理单个非 COMBAT 事件（由统一循环调用） */
  public void handleNumericEvent(
      Long userId, User user, ActivityEvent event, Map<String, Object> context) {
    Map<String, Object> templateArgs = effectExecutor.execute(event, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
            .withNarrative(narrativeKey, templateArgs));
  }

  /** 检查历练隐藏事件 */
  public void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var fortune = fortuneService.calculate(userId);
    var hiddenEvents =
        subEventSelector.findHiddenEvents(ActivityType.TRAINING.getCode(), mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      if (!activityEventHelper.checkPrerequisite(userId, event)) continue;
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ActivityType.TRAINING.getCode(), mapNode.getId(), event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ActivityType.TRAINING.getCode(), mapNode.getId(), event.getCode()));

      Map<String, Object> context = new HashMap<>();
      EventContextKeys.MAP_NODE.put(context, mapNode);
      EventContextKeys.MAP_NAME.put(context, mapNode.getName());
      EventContextKeys.FORTUNE.put(context, fortune);
      Map<String, Object> templateArgs = effectExecutor.execute(event, userId, user, context);
      String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
      gameEventService.save(
          GameEvent.create(userId, GameEventCategory.TRAINING_HIDDEN)
              .withNarrative(narrativeKey, templateArgs));
    }
  }
}
