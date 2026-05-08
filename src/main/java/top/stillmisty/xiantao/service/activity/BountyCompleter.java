package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
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
import top.stillmisty.xiantao.service.GameEventService;

/** 悬赏完成器 — 处理悬赏领奖的子事件调节和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyCompleter {

  private static final String ACTIVITY_TYPE = "BOUNTY_SIDE";

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final HiddenCompletionRepository hiddenCompletionRepository;

  /** 悬赏完成叙事 */
  public void produceCompletionEvent(
      Long userId, String bountyName, List<BountyRewardItem> items, long spiritStones) {
    Map<String, Object> args = new HashMap<>();
    args.put("bountyName", bountyName);
    args.put("spiritStones", spiritStones);
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

  /**
   * 悬赏子事件调节主奖励
   *
   * @return 调节后的 spiritStones 数量
   */
  public long applySideModifier(
      Long userId,
      Long bountyId,
      String bountyName,
      List<BountyRewardItem> items,
      long spiritStones) {
    ActivityEvent selected = subEventSelector.selectSubEvent(ACTIVITY_TYPE, bountyId, 1.0);
    if (selected == null) return spiritStones;

    return switch (selected.getCode()) {
      case "BONUS_PAY" -> {
        long bonus = (long) (spiritStones * 0.5);
        Map<String, Object> args = Map.of("spiritStones", spiritStones + bonus);
        gameEventService.createEvent(
            userId,
            GameEventCategory.BOUNTY_SIDE_MODIFIER,
            "委托人很满意你的效率，额外给了赏钱。\n✨ 灵石 +{{spiritStones}}",
            args);
        yield spiritStones + bonus;
      }
      case "SABOTAGE" -> {
        long reduced = (long) (spiritStones * 0.5);
        Map<String, Object> args = Map.of("spiritStones", reduced);
        gameEventService.createEvent(
            userId,
            GameEventCategory.BOUNTY_SIDE_MODIFIER,
            "你在追踪过程中发现目标已被其他猎手抢先一步，损失了一部分战利品。\n✨ 灵石 +{{spiritStones}}",
            args);
        yield reduced;
      }
      case "NOTHING", "INTEL", "SHORT_CUT", "TIME_LOST", "AMBUSH" -> spiritStones;
      default -> spiritStones;
    };
  }

  /** 检查悬赏隐藏事件 (领奖时) */
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
