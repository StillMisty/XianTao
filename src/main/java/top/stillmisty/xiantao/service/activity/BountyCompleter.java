package top.stillmisty.xiantao.service.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

/** 悬赏完成器 — 悬赏领奖的子事件调节和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyCompleter {

  private final GameEventService gameEventService;
  private final ActivitySubEventPipeline subEventPipeline;

  /** 悬赏完成叙事 */
  @Transactional
  public void produceCompletionEvent(
      Long userId, String bountyName, List<BountyRewardItem> items, long spiritStones) {
    List<String> parts = new ArrayList<>();
    if (spiritStones > 0) {
      parts.add("✨ 灵石 +" + spiritStones);
    }
    if (!items.isEmpty()) {
      String itemsStr =
          items.stream()
              .map(
                  i ->
                      switch (i) {
                        case BountyRewardItem.ItemReward(_, var name, var quantity) ->
                            name + " x" + quantity;
                        case BountyRewardItem.BeastEggReward(_, var name) -> name + " x1";
                        case BountyRewardItem.EquipmentRewardItem(_, var name) -> name + " x1";
                        case BountyRewardItem.SkillJadeRewardItem(_, var name) -> name + " x1";
                        default -> "";
                      })
              .filter(s -> !s.isEmpty())
              .collect(Collectors.joining("、"));
      parts.add(itemsStr);
    }
    String rewardsText = String.join("、", parts);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.BOUNTY_COMPLETE)
            .withNarrative(
                "委托「" + bountyName + "」已完成。\n你从发布人处领取了约定的报酬：\n" + rewardsText, Map.of()));
  }

  /** 悬赏已可领取提示 */
  @Transactional
  public void produceReadyEvent(Long userId, String bountyName) {
    Map<String, Object> args = Map.of("bountyName", bountyName);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.BOUNTY_READY)
            .withNarrative("悬赏「{{bountyName}}」已完成，请使用「悬赏结算」领取奖励。", args));
  }

  /** 悬赏子事件调节主奖励 — 通过 context 传出修改后的灵石数 */
  @Transactional
  public void rollBountySideEvent(
      Long userId, User user, Long bountyId, String bountyName, EventContext context) {
    EventContextKeys.BOUNTY_NAME.put(context, bountyName);
    subEventPipeline.rollSubEvent(
        ActivityType.BOUNTY_SIDE.getCode(),
        bountyId,
        1.0,
        userId,
        user,
        GameEventCategory.BOUNTY_SIDE_MODIFIER,
        fortune -> {
          EventContextKeys.FORTUNE.put(context, fortune);
          return context;
        });
  }

  /** 检查悬赏隐藏事件 */
  @Transactional
  public void checkHiddenEvents(Long userId, User user, UserBounty record) {
    subEventPipeline.checkHiddenEvents(
        ActivityType.BOUNTY_SIDE.getCode(),
        record.getBountyId(),
        userId,
        user,
        GameEventCategory.BOUNTY_HIDDEN,
        fortune -> {
          EventContext ctx = EventContext.empty();
          EventContextKeys.BOUNTY_NAME.put(ctx, record.getBountyName());
          EventContextKeys.FORTUNE.put(ctx, fortune);
          return ctx;
        });
  }
}
