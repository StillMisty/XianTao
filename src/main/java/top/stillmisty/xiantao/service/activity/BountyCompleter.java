package top.stillmisty.xiantao.service.activity;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.EventTypeRepository;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

/** 悬赏完成器 — 悬赏领奖的子事件调节和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyCompleter {

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final EventTypeRepository eventTypeRepository;
  private final TriggerConditionChecker triggerConditionChecker;

  /** 悬赏完成叙事 */
  public void produceCompletionEvent(
      Long userId, String bountyName, List<BountyRewardItem> items, long spiritStones) {
    Map<String, Object> args = Map.of("bountyName", bountyName, "spiritStones", spiritStones);
    gameEventService.createEvent(
        userId,
        GameEventCategory.BOUNTY_COMPLETE,
        "委托「{{bountyName}}」已完成。\n你从发布人处领取了约定的报酬：\n✨ 灵石 +{{spiritStones}}",
        args);
  }

  /** 悬赏已可领取提示 */
  public void produceReadyEvent(Long userId, String bountyName) {
    Map<String, Object> args = Map.of("bountyName", bountyName);
    gameEventService.createEvent(
        userId, GameEventCategory.BOUNTY_READY, "悬赏「{{bountyName}}」已完成，请使用「悬赏结算」领取奖励。", args);
  }

  /** 悬赏子事件调节主奖励 — 通过 context 传出修改后的灵石数 */
  public void rollBountySideEvent(
      Long userId, User user, Long bountyId, String bountyName, Map<String, Object> context) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(ActivityType.BOUNTY_SIDE.getCode(), bountyId, 1.0);
    if (selected == null) return;

    context.put("bountyName", bountyName);
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    gameEventService.createEvent(
        userId, GameEventCategory.BOUNTY_SIDE_MODIFIER, selected.getCode(), templateArgs);
  }

  /** 检查悬赏隐藏事件 */
  public void checkHiddenEvents(Long userId, User user, UserBounty record) {
    var hiddenEvents =
        subEventSelector.findHiddenEvents(ActivityType.BOUNTY_SIDE.getCode(), record.getBountyId());
    for (ActivityEvent event : hiddenEvents) {
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ActivityType.BOUNTY_SIDE.getCode(), record.getBountyId(), event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ActivityType.BOUNTY_SIDE.getCode(), record.getBountyId(), event.getCode()));

      Map<String, Object> templateArgs =
          effectExecutor.execute(event, userId, user, Map.of("bountyName", record.getBountyName()));
      String narrativeKey =
          eventTypeRepository
              .findByCode(event.getCode())
              .map(e -> e.getDescription())
              .orElse(event.getCode());
      gameEventService.createEvent(
          userId, GameEventCategory.BOUNTY_HIDDEN, narrativeKey, templateArgs);
    }
  }
}
