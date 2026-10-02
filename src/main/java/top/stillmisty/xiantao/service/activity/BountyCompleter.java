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
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;
import top.stillmisty.xiantao.service.GameEventService;

/** 悬赏完成器 — 悬赏领奖的子事件调节和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BountyCompleter {

  /** 线索键前缀：bounty.hidden.<event_code> */
  private static final String HINT_KEY_PREFIX = "bounty.hidden.";

  private final GameEventService gameEventService;
  private final ActivitySubEventPipeline subEventPipeline;
  private final ActivityEventHelper activityEventHelper;

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
      Long userId, Player user, Long bountyId, String bountyName, EventContext context) {
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

  /**
   * 接取悬赏时预检隐藏事件，命中则生成线索（两阶段隐藏事件的第一阶段）。
   *
   * <p>线索写入 {@code user_bounty.hidden_clues}，形如 {@code {"code": "...", "hint_key":
   * "bounty.hidden.<code>", "hint": "..."}}；未命中返回空 Map。
   */
  @Transactional
  public Map<String, Object> prepareHiddenClue(Long userId, Player user, Long bountyId) {
    ActivityEvent event =
        subEventPipeline.findTriggerableHiddenEvent(
            ActivityType.BOUNTY_SIDE.getCode(), bountyId, userId, user);
    if (event == null) return Map.of();
    return Map.of(
        "code", event.getCode(),
        "hint_key", HINT_KEY_PREFIX + event.getCode(),
        "hint", resolveHint(event));
  }

  /**
   * 领奖时结算隐藏事件：优先按接取时记录的线索二段校验（条件仍满足才发放）。
   *
   * <p>兼容旧数据：历史记录 hidden_clues 为空时，维持领奖时一次性条件检查。
   */
  @Transactional
  public void resolveHiddenEvents(Long userId, Player user, UserBounty record) {
    Map<String, Object> clues = record.getHiddenClues();
    String code = clues != null ? TypeUtils.getString(clues, "code") : null;
    if (code == null || code.isBlank()) {
      checkHiddenEvents(userId, user, record);
      return;
    }
    subEventPipeline.resolveHiddenEvent(
        ActivityType.BOUNTY_SIDE.getCode(),
        record.getBountyId(),
        code,
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

  /** 线索文案：优先取隐藏事件配置中的 hint，缺失则按事件名衍生一句含蓄提示 */
  private String resolveHint(ActivityEvent event) {
    Object configured = event.getParams() != null ? event.getParams().get("hint") : null;
    if (configured instanceof String hint && !hint.isBlank()) return hint;
    String name = activityEventHelper.resolveEventName(event.getCode());
    if (name == null || name.isBlank()) {
      return "你隐约觉得此事另有隐情……";
    }
    return "你隐约觉得此事另有隐情……「" + name + "」的机缘，或许就藏在这一单的收尾处。";
  }

  /** 检查悬赏隐藏事件 */
  @Transactional
  public void checkHiddenEvents(Long userId, Player user, UserBounty record) {
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
