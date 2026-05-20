package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.EventTypeRepository;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
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
  private final EventTypeRepository eventTypeRepository;
  private final TriggerConditionChecker triggerConditionChecker;

  public void produceCompletionEvent(
      Long userId, User user, MapNode mapNode, long minutesTraining) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName(), "minutes", minutesTraining);
    gameEventService.createEvent(
        userId, GameEventCategory.TRAINING_COMPLETE, "你在{{mapName}}历练了 {{minutes}} 分钟，有所收获。", args);
  }

  public void produceInterruptedEvent(Long userId, MapNode mapNode) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName());
    gameEventService.createEvent(
        userId, GameEventCategory.TRAINING_INTERRUPTED, "你在{{mapName}}的历练因重伤而中断。", args);
  }

  /** 处理单个非 COMBAT 事件（由统一循环调用） */
  public void handleNumericEvent(
      Long userId, User user, ActivityEvent event, Map<String, Object> context) {
    Map<String, Object> templateArgs = effectExecutor.execute(event, userId, user, context);
    String narrativeKey = resolveNarrativeKey(event.getCode());
    gameEventService.createEvent(
        userId, GameEventCategory.TRAINING_EVENT, narrativeKey, templateArgs);
  }

  /** 检查历练隐藏事件 */
  public void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var hiddenEvents =
        subEventSelector.findHiddenEvents(ActivityType.TRAINING.getCode(), mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ActivityType.TRAINING.getCode(), mapNode.getId(), event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ActivityType.TRAINING.getCode(), mapNode.getId(), event.getCode()));

      Map<String, Object> templateArgs =
          effectExecutor.execute(
              event, userId, user, Map.of("mapNode", mapNode, "mapName", mapNode.getName()));
      String narrativeKey = resolveNarrativeKey(event.getCode());
      gameEventService.createEvent(
          userId, GameEventCategory.TRAINING_HIDDEN, narrativeKey, templateArgs);
    }
  }

  private String resolveNarrativeKey(String code) {
    return eventTypeRepository.findByCode(code).map(e -> e.getDescription()).orElse(code);
  }
}
