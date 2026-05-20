package top.stillmisty.xiantao.service.activity;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

/** 悬赏完成器 — 悬赏领奖的子事件调节和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyCompleter {

  private static final String ACTIVITY_TYPE = "BOUNTY_SIDE";

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;

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
    ActivityEvent selected = subEventSelector.selectSubEvent(ACTIVITY_TYPE, bountyId, 1.0);
    if (selected == null) return;

    context.put("bountyName", bountyName);
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    gameEventService.createEvent(
        userId, GameEventCategory.BOUNTY_SIDE_MODIFIER, selected.getCode(), templateArgs);
  }

  /** 检查悬赏隐藏事件 */
  public void checkHiddenEvents(Long userId, UserBounty record) {
    Map<String, Object> clues = record.getHiddenClues();
    if (clues == null || clues.isEmpty()) return;
    String code = (String) clues.get("code");
    if (code == null) return;

    var hiddenEvents = subEventSelector.findHiddenEvents(ACTIVITY_TYPE, record.getBountyId());
    for (ActivityEvent event : hiddenEvents) {
      if (!event.getCode().equals(code)) continue;
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ACTIVITY_TYPE, record.getBountyId(), event.getCode());
      if (alreadyDone) continue;

      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ACTIVITY_TYPE, record.getBountyId(), event.getCode()));

      Map<String, Object> args = Map.of("bountyName", record.getBountyName(), "eventName", code);
      gameEventService.createEvent(
          userId,
          GameEventCategory.BOUNTY_HIDDEN,
          "你按照线索找到了「{{bountyName}}」的隐藏收获：{{eventName}}。",
          args);
      return;
    }
  }
}
