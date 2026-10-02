package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;
import top.stillmisty.xiantao.domain.event.enums.EventTypeEnum;
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

  /**
   * 预检可触发的隐藏事件（两阶段隐藏线索的第一阶段）：按配置顺序返回第一条满足前置、未完成且条件匹配的事件。
   *
   * <p>仅返回事件本身，不执行效果、不写完成记录；线索写入与领奖发放由调用方负责。
   */
  public @Nullable ActivityEvent findTriggerableHiddenEvent(
      String activityType, Long ownerId, Long userId, Player user) {
    for (ActivityEvent event : subEventSelector.findHiddenEvents(activityType, ownerId)) {
      if (!activityEventHelper.checkPrerequisite(userId, event)) continue;
      boolean alreadyDone =
          hiddenCompletionRepository.exists(userId, activityType, ownerId, event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;
      return event;
    }
    return null;
  }

  /**
   * 按线索二段校验并执行指定隐藏事件（两阶段隐藏线索的第二阶段）。
   *
   * @return 产出的事件；条件不再满足或该事件已完成时返回 null（不发放隐藏奖励）
   */
  public @Nullable GameEvent resolveHiddenEvent(
      String activityType,
      Long ownerId,
      String code,
      Long userId,
      Player user,
      GameEventCategory category,
      Function<FortuneVO, EventContext> contextFactory) {
    for (ActivityEvent event : subEventSelector.findHiddenEvents(activityType, ownerId)) {
      if (!event.getCode().equals(code)) continue;
      if (hiddenCompletionRepository.exists(userId, activityType, ownerId, code)) return null;
      if (!triggerConditionChecker.check(event, userId, user)) return null;

      hiddenCompletionRepository.save(HiddenCompletion.create(userId, activityType, ownerId, code));
      return processEvent(event, userId, user, category, contextFactory);
    }
    return null;
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
    // CHOICE 事件不立即结算：把选项写入 game_event.effects，等玩家「选 X」后由 ChoiceService 执行
    if (event.getEventType() == EventTypeEnum.CHOICE) {
      return saveChoiceEvent(event, userId, category);
    }
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
    if (event.getEventType() == EventTypeEnum.CHOICE) {
      return saveChoiceEvent(event, userId, category);
    }
    var templateArgs = effectExecutor.execute(event, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
    return gameEventService.save(
        GameEvent.create(userId, category).withNarrative(narrativeKey, templateArgs));
  }

  /** 将 CHOICE 活动事件转为等待玩家抉择的 GameEvent（不立即执行选项效果） */
  private GameEvent saveChoiceEvent(ActivityEvent event, Long userId, GameEventCategory category) {
    EffectData.ChoiceOptions choiceData = EffectData.ChoiceOptions.fromParamsMap(event.getParams());
    String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
    return gameEventService.save(
        GameEvent.create(userId, category)
            .withNarrative(narrativeKey, Map.of())
            .withSourceEventCode(event.getCode())
            .withEffectData(choiceData));
  }
}
