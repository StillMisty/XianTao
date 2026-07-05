package top.stillmisty.xiantao.service.activity;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;
import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;

/** 活动子事件管线 — 统一子事件滚动、隐藏事件检查、效果执行和通知保存 */
@Component
@RequiredArgsConstructor
public class ActivitySubEventPipeline {

  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final ActivityEventHelper activityEventHelper;
  private final TriggerConditionChecker triggerConditionChecker;
  private final FortuneService fortuneService;
  private final GameEventService gameEventService;

  /** 滚动一个普通子事件：加权选择 → 执行效果 → 保存通知 */
  public @Nullable GameEvent rollSubEvent(
      String activityType,
      Long ownerId,
      double triggerChance,
      Long userId,
      Player user,
      GameEventCategory category,
      Function<FortuneVO, EventContext> contextFactory) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(activityType, ownerId, triggerChance, userId);
    if (selected == null) return null;
    return processEvent(selected, userId, user, category, contextFactory);
  }

  /** 检查并执行隐藏事件 */
  public void checkHiddenEvents(
      String activityType,
      Long ownerId,
      Long userId,
      Player user,
      GameEventCategory category,
      Function<FortuneVO, EventContext> contextFactory) {
    var fortune = fortuneService.calculate(userId);
    var hiddenEvents = subEventSelector.findHiddenEvents(activityType, ownerId);
    for (ActivityEvent event : hiddenEvents) {
      if (!activityEventHelper.checkPrerequisite(userId, event)) continue;
      boolean alreadyDone =
          hiddenCompletionRepository.exists(userId, activityType, ownerId, event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          HiddenCompletion.create(userId, activityType, ownerId, event.getCode()));

      EventContext context = contextFactory.apply(fortune);
      processEvent(event, userId, user, category, f -> context);
    }
  }

  /** 执行事件效果并保存通知 */
  public GameEvent processEvent(
      ActivityEvent event,
      Long userId,
      Player user,
      GameEventCategory category,
      Function<FortuneVO, EventContext> contextFactory) {
    var fortune = fortuneService.calculate(userId);
    EventContext context = contextFactory.apply(fortune);
    var templateArgs = effectExecutor.execute(event, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
    return gameEventService.save(
        GameEvent.create(userId, category).withNarrative(narrativeKey, templateArgs));
  }

  /** 执行事件效果并保存通知（使用已有上下文） */
  public GameEvent processEventWithContext(
      ActivityEvent event,
      Long userId,
      Player user,
      GameEventCategory category,
      EventContext context) {
    var templateArgs = effectExecutor.execute(event, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
    return gameEventService.save(
        GameEvent.create(userId, category).withNarrative(narrativeKey, templateArgs));
  }
}
