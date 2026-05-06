package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.domain.bounty.repository.UserBountyRepository;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.TravelEventType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class BountyCombatService {

  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final UserBountyRepository userBountyRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final ExplorationDescriptionFunction explorationDescriptionFunction;
  private final StackableItemService stackableItemService;
  private final EquipmentService equipmentService;

  @Transactional
  public BountyRewardVO completeBounty(Long userId) {
    User user = userStateService.loadUser(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法完成悬赏（需要 悬赏 状态）");
    }
    UserBounty record =
        userBountyRepository
            .findActiveByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

    long minutesElapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
    if (minutesElapsed < record.getDurationMinutes()) {
      long remaining = record.getDurationMinutes() - minutesElapsed;
      throw new IllegalArgumentException(
          String.format(
              "悬赏「%s」还需 %d 分钟（共需 %d 分）",
              record.getBountyName(), remaining, record.getDurationMinutes()));
    }

    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
    return processBountyCompletion(userId, user, record, mapNode, minutesElapsed);
  }

  private BountyRewardVO processBountyCompletion(
      Long userId, User user, UserBounty record, MapNode mapNode, long minutesElapsed) {
    List<BountyRewardItem> rewardItems = record.getParsedRewardItems();
    RewardStats stats = collectRewardStats(rewardItems);
    List<BountyRewardItem> items = filterNonCurrencyRewards(rewardItems);

    addRewardsToInventory(userId, items);
    if (stats.spiritStones > 0) {
      user.setSpiritStones(user.getSpiritStones() + stats.spiritStones);
    }

    String eventDescription = resolveEvent(user, mapNode);
    String rewardDescription =
        buildRewardDescription(stats.spiritStones, items, stats.hasBeastEgg, stats.hasEquipment);
    String beautified =
        beautifyBountyCompletion(
            mapNode, record.getBountyName(), rewardDescription, eventDescription, items);

    record.setStatus(BountyStatus.COMPLETED);
    userBountyRepository.save(record);

    user.setStatus(UserStatus.IDLE);
    userStateService.save(user);

    log.info(
        "用户 {} 完成悬赏: {} (耗时{}分, 物品数={}, 灵石={})",
        userId,
        record.getBountyName(),
        minutesElapsed,
        items.size(),
        stats.spiritStones);

    return new BountyRewardVO(
        userId,
        record.getBountyId(),
        record.getBountyName(),
        mapNode.getName(),
        minutesElapsed,
        beautified != null ? beautified : rewardDescription,
        eventDescription,
        items,
        stats.spiritStones,
        stats.hasBeastEgg,
        stats.hasEquipment);
  }

  private record RewardStats(long spiritStones, boolean hasBeastEgg, boolean hasEquipment) {}

  private RewardStats collectRewardStats(List<BountyRewardItem> rewardItems) {
    long spiritStones = 0;
    boolean hasBeastEgg = false;
    boolean hasEquipment = false;
    for (BountyRewardItem item : rewardItems) {
      switch (item) {
        case BountyRewardItem.SpiritStonesReward(var amount) -> spiritStones = amount;
        case BountyRewardItem.BeastEggReward _ -> hasBeastEgg = true;
        case BountyRewardItem.EquipmentRewardItem _ -> hasEquipment = true;
        default -> {}
      }
    }
    return new RewardStats(spiritStones, hasBeastEgg, hasEquipment);
  }

  private List<BountyRewardItem> filterNonCurrencyRewards(List<BountyRewardItem> rewardItems) {
    return rewardItems.stream()
        .filter(
            i ->
                !(i instanceof BountyRewardItem.SpiritStonesReward)
                    && !(i instanceof BountyRewardItem.EquipmentRewardItem))
        .toList();
  }

  private void addRewardsToInventory(Long userId, List<BountyRewardItem> items) {
    var itemRewards =
        items.stream()
            .filter(
                i ->
                    i instanceof BountyRewardItem.ItemReward
                        || i instanceof BountyRewardItem.BeastEggReward
                        || i instanceof BountyRewardItem.EquipmentRewardItem)
            .toList();
    if (itemRewards.isEmpty()) return;

    Set<Long> templateIds = new HashSet<>();
    for (BountyRewardItem item : itemRewards) {
      switch (item) {
        case BountyRewardItem.ItemReward(var templateId, _, _) -> templateIds.add(templateId);
        case BountyRewardItem.BeastEggReward(var templateId, _) -> templateIds.add(templateId);
        case BountyRewardItem.EquipmentRewardItem(var templateId, _) -> templateIds.add(templateId);
        default -> {}
      }
    }

    if (templateIds.isEmpty()) return;

    Map<Long, ItemType> typeMap =
        itemTemplateRepository.findByIds(new ArrayList<>(templateIds)).stream()
            .collect(Collectors.toMap(ItemTemplate::getId, ItemTemplate::getType));

    for (BountyRewardItem item : itemRewards) {
      switch (item) {
        case BountyRewardItem.ItemReward(var templateId, var name, var quantity) -> {
          ItemType itemType = typeMap.getOrDefault(templateId, ItemType.MATERIAL);
          stackableItemService.addStackableItem(userId, templateId, itemType, name, quantity);
        }
        case BountyRewardItem.BeastEggReward(var templateId, var name) -> {
          stackableItemService.addStackableItem(userId, templateId, ItemType.BEAST_EGG, name, 1);
        }
        case BountyRewardItem.EquipmentRewardItem(var templateId, var name) -> {
          equipmentService.createEquipment(userId, templateId);
        }
        default -> {}
      }
    }
  }

  private String buildRewardDescription(
      long spiritStones, List<BountyRewardItem> items, boolean hasBeastEgg, boolean hasEquipment) {
    StringBuilder sb = new StringBuilder();
    if (spiritStones > 0) {
      sb.append("获得 ").append(spiritStones).append(" 灵石。");
    }
    if (!items.isEmpty()) {
      if (!sb.isEmpty()) sb.append(" ");
      sb.append("获得物品：");
      sb.append(
          items.stream()
              .map(
                  i ->
                      switch (i) {
                        case BountyRewardItem.ItemReward(_, var name, var quantity) ->
                            String.format("%s x%d", name, quantity);
                        case BountyRewardItem.BeastEggReward(_, var name) ->
                            String.format("%s x1", name);
                        default -> "";
                      })
              .filter(s -> !s.isEmpty())
              .collect(Collectors.joining("、")));
      sb.append("。");
    }
    if (hasBeastEgg) {
      if (!sb.isEmpty()) sb.append(" ");
      sb.append("获得灵兽卵。");
    }
    if (hasEquipment) {
      if (!sb.isEmpty()) sb.append(" ");
      sb.append("获得装备。");
    }
    return sb.toString();
  }

  private String resolveEvent(User user, MapNode mapNode) {
    if (mapNode.getTravelEvents() == null || mapNode.getTravelEvents().isEmpty()) return null;

    int d20Roll = (int) (Math.random() * 20) + 1;
    if (d20Roll > 10) return null;

    TravelEventType eventType = TravelEventType.randomEvent(mapNode.getTravelEvents());
    if (eventType == TravelEventType.SAFE_PASSAGE) return null;

    String description = processEvent(user, eventType);
    log.info("用户 {} 悬赏途中触发事件: {}", user.getId(), description);
    return description;
  }

  private String processEvent(User user, TravelEventType eventType) {
    return switch (eventType) {
      case AMBUSH -> {
        int damage = 10 + (int) (Math.random() * 20);
        user.takeDamage(damage);
        yield String.format("途中遭遇敌人袭击，受到 %d 点伤害（剩余 HP: %d）", damage, user.getHpCurrent());
      }
      case FIND_TREASURE -> {
        long reward = 5 + (long) (Math.random() * 15);
        user.setSpiritStones(user.getSpiritStones() + reward);
        yield String.format("途中发现了一个隐藏宝箱，获得 %d 灵石", reward);
      }
      case WEATHER -> "遭遇毒雾天气，艰难穿过才得以继续前行";
      case SAFE_PASSAGE -> null;
    };
  }

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
            eventDescription);

    try {
      var response = explorationDescriptionFunction.beautify(request);
      return response != null ? response.description() : null;
    } catch (Exception e) {
      log.warn("LLM 美化悬赏描述失败", e);
      return null;
    }
  }
}
