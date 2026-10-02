package top.stillmisty.xiantao.service.bounty;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserBountyRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.RewardGrant;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.activity.BountyCompleter;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;
import top.stillmisty.xiantao.service.player.UserStateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BountyCombatService {

  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final UserBountyRepository userBountyRepository;
  private final ExplorationDescriptionFunction explorationDescriptionFunction;
  private final RewardGrant rewardGrant;
  private final BountyCompleter bountyCompleter;
  private final SpiritStoneService spiritStoneService;
  private final FortuneService fortuneService;

  /** 悬赏完成结果：VO 附带美化所需元数据（LLM 调用须在事务提交后进行） */
  public record CompletedBounty(
      BountyRewardVO vo,
      MapNode mapNode,
      String bountyName,
      String rewardDescription,
      List<BountyRewardItem> items) {}

  @Transactional
  public CompletedBounty completeBounty(Long userId) {
    Player user = userStateService.loadUser(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new BusinessException(STATUS_BLOCKED, user.getStatus().getName(), "悬赏");
    }
    // 超时后 BountyReadyHandler 可能已把记录标记为 COMPLETED，领奖仍按同一记录进行
    UserBounty record =
        userBountyRepository
            .findCurrentForUser(userId, user.getActivityTargetId())
            .orElseThrow(() -> new BusinessException(BOUNTY_NO_ACTIVE));

    long minutesElapsed = Duration.between(record.getStartTime(), TimeUtil.now()).toMinutes();
    if (record.getStatus() == BountyStatus.ACTIVE && minutesElapsed < record.getDurationMinutes()) {
      long remaining = record.getDurationMinutes() - minutesElapsed;
      throw new BusinessException(
          BOUNTY_TIME_REMAINING, record.getBountyName(), remaining, record.getDurationMinutes());
    }

    MapNode mapNode =
        mapNodeRepository
            .findById(user.getLocationId())
            .orElseThrow(() -> new BusinessException(MAP_CURRENT_NOT_FOUND));
    return processBountyCompletion(userId, user, record, mapNode, minutesElapsed);
  }

  private CompletedBounty processBountyCompletion(
      Long userId, Player user, UserBounty record, MapNode mapNode, long minutesElapsed) {
    List<BountyRewardItem> rewardItems = record.getParsedRewardItems();
    RewardStats stats = collectRewardStats(rewardItems);
    List<BountyRewardItem> items = filterNonCurrencyRewards(rewardItems);

    addRewardsToInventory(userId, items);

    // Apply bounty side modifier (子事件调节主奖励)
    EventContext sideContext = EventContext.empty();
    EventContextKeys.BOUNTY_NAME.put(sideContext, record.getBountyName());
    long[] rewardHolder = new long[] {stats.spiritStones};
    EventContextKeys.BOUNTY_REWARD.put(sideContext, rewardHolder);
    bountyCompleter.rollBountySideEvent(
        userId, user, record.getBountyId(), record.getBountyName(), sideContext);
    long finalSpiritStones = rewardHolder[0];

    var fortune = fortuneService.calculate(userId);
    finalSpiritStones =
        (long) (finalSpiritStones * fortuneService.getWealthMultiplier(fortune.wealth()));

    if (finalSpiritStones > 0) {
      spiritStoneService.deposit(userId, finalSpiritStones);
    }

    // Bounty completion event
    bountyCompleter.produceCompletionEvent(
        userId, record.getBountyName(), items, finalSpiritStones);

    // Check hidden events
    bountyCompleter.checkHiddenEvents(userId, user, record);

    String rewardDescription =
        buildRewardDescription(finalSpiritStones, items, stats.hasBeastEgg, stats.hasEquipment);

    record.setStatus(BountyStatus.COMPLETED);
    userBountyRepository.save(record);

    user.clearActivity();
    userStateService.saveActivity(user);

    log.info(
        "玩家 {} 完成悬赏: {} (耗时{}分, 物品数={}, 灵石={})",
        userId,
        record.getBountyName(),
        minutesElapsed,
        items.size(),
        finalSpiritStones);

    BountyRewardVO vo =
        new BountyRewardVO(
            userId,
            record.getBountyId(),
            record.getBountyName(),
            mapNode.getName(),
            minutesElapsed,
            rewardDescription,
            null,
            items,
            finalSpiritStones,
            stats.hasBeastEgg,
            stats.hasEquipment);
    return new CompletedBounty(
        vo, mapNode, record.getBountyName(), rewardDescription, List.copyOf(items));
  }

  /** LLM 美化悬赏结算描述。必须在 {@link #completeBounty} 的事务提交之后调用， 避免 LLM 往返期间持有 user_bounty 行锁。 */
  @Nullable
  public String beautify(CompletedBounty completed) {
    return beautifyBountyCompletion(
        completed.mapNode(),
        completed.bountyName(),
        completed.rewardDescription(),
        "",
        completed.items());
  }

  private record RewardStats(long spiritStones, boolean hasBeastEgg, boolean hasEquipment) {}

  private RewardStats collectRewardStats(List<BountyRewardItem> rewardItems) {
    long spiritStones = 0;
    boolean hasBeastEgg = false;
    boolean hasEquipment = false;
    for (BountyRewardItem item : rewardItems) {
      switch (item) {
        case BountyRewardItem.SpiritStonesReward(var amount) -> spiritStones += amount;
        case BountyRewardItem.BeastEggReward _ -> hasBeastEgg = true;
        case BountyRewardItem.EquipmentRewardItem _ -> hasEquipment = true;
        case BountyRewardItem.SkillJadeRewardItem _ -> {}
        default -> {}
      }
    }
    return new RewardStats(spiritStones, hasBeastEgg, hasEquipment);
  }

  private List<BountyRewardItem> filterNonCurrencyRewards(List<BountyRewardItem> rewardItems) {
    return rewardItems.stream()
        .filter(i -> !(i instanceof BountyRewardItem.SpiritStonesReward))
        .toList();
  }

  private void addRewardsToInventory(Long userId, List<BountyRewardItem> items) {
    rewardGrant.grant(userId, toDropItems(items));
  }

  /** 悬赏奖励 → 统一发放模型：灵石奖励不入包（走 SpiritStoneService），此处只转换物品侧。 */
  private static List<DropItem> toDropItems(List<BountyRewardItem> items) {
    List<DropItem> drops = new ArrayList<>();
    for (BountyRewardItem item : items) {
      switch (item) {
        case BountyRewardItem.ItemReward(var templateId, var name, var quantity) ->
            drops.add(new DropItem(DropItem.DropType.ITEM, templateId, name, quantity));
        case BountyRewardItem.BeastEggReward(var templateId, var name) ->
            drops.add(new DropItem(DropItem.DropType.ITEM, templateId, name, 1));
        case BountyRewardItem.EquipmentRewardItem(var templateId, var name) ->
            drops.add(new DropItem(DropItem.DropType.EQUIPMENT, templateId, name, 1));
        case BountyRewardItem.SkillJadeRewardItem(var templateId, var name) ->
            drops.add(new DropItem(DropItem.DropType.ITEM, templateId, name, 1));
        default -> {}
      }
    }
    return drops;
  }

  private String buildRewardDescription(
      long spiritStones, List<BountyRewardItem> items, boolean hasBeastEgg, boolean hasEquipment) {
    StringBuilder sb = new StringBuilder();
    if (spiritStones > 0) {
      sb.append("获得 ").append(spiritStones).append(" 灵石。");
    }
    if (!items.isEmpty()) {
      if (!sb.isEmpty()) sb.append(" ");
      sb.append("获得：");
      sb.append(
          items.stream()
              .map(
                  i ->
                      switch (i) {
                        case BountyRewardItem.ItemReward(_, var name, var quantity) ->
                            String.format("%s x%d", name, quantity);
                        case BountyRewardItem.BeastEggReward(_, var name) ->
                            String.format("%s x1", name);
                        case BountyRewardItem.EquipmentRewardItem(_, var name) ->
                            String.format("%s x1", name);
                        case BountyRewardItem.SkillJadeRewardItem(_, var name) ->
                            String.format("%s x1", name);
                        default -> "";
                      })
              .filter(s -> !s.isEmpty())
              .collect(Collectors.joining("、")));
      sb.append("。");
    }
    return sb.toString();
  }

  @Nullable
  private String beautifyBountyCompletion(
      MapNode mapNode,
      String bountyName,
      String rewardDescription,
      String eventDescription,
      List<BountyRewardItem> items) {
    List<String> itemNames = null;
    if (items != null && !items.isEmpty()) {
      itemNames =
          items.stream()
              .map(
                  i ->
                      switch (i) {
                        case BountyRewardItem.ItemReward(_, var name, _) -> name;
                        case BountyRewardItem.BeastEggReward(_, var name) -> name;
                        case BountyRewardItem.EquipmentRewardItem(_, var name) -> name;
                        case BountyRewardItem.SkillJadeRewardItem(_, var name) -> name;
                        default -> null;
                      })
              .filter(Objects::nonNull)
              .toList();
    }

    var request =
        new ExplorationDescriptionFunction.Request(
            mapNode.getName(),
            mapNode.getDescription(),
            "完成悬赏「" + bountyName + "」",
            itemNames,
            null,
            null,
            eventDescription,
            null,
            null,
            null);

    // 叙述模块内部已兜底失败场景，此处不再吞异常
    return explorationDescriptionFunction.beautify(request).description();
  }
}
