package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.repository.BountyRepository;
import top.stillmisty.xiantao.domain.bounty.repository.UserBountyRepository;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyStatusVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.map.enums.TravelEventType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 悬赏服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BountyService {

  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final BountyRepository bountyRepository;
  private final UserBountyRepository userBountyRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final ExplorationDescriptionFunction explorationDescriptionFunction;
  private final StackableItemService stackableItemService;
  private final EquipmentService equipmentService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<List<BountyVO>> listBounties(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(listBounties(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  public ServiceResult<BountyStatusVO> getBountyStatus(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(getBountyStatus(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  public ServiceResult<String> startBounty(PlatformType platform, String openId, Long bountyId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(startBounty(userId, bountyId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<BountyRewardVO> completeBounty(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(completeBounty(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  public ServiceResult<String> abandonBounty(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(abandonBounty(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  // ===================== 内部 API =====================

  public List<BountyVO> listBounties(Long userId) {
    User user = userStateService.getUser(userId);
    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

    return bountyRepository.findByMapId(mapNode.getId()).stream()
        .filter(b -> user.getLevel() >= b.getRequireLevel())
        .map(
            b ->
                new BountyVO(
                    b.getId(),
                    b.getName(),
                    b.getDescription(),
                    b.getDurationMinutes(),
                    b.getRewards(),
                    b.getRequireLevel(),
                    b.getEventWeight()))
        .toList();
  }

  public BountyStatusVO getBountyStatus(Long userId) {
    UserBounty record =
        userBountyRepository
            .findActiveByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("当前没有进行中的悬赏"));

    long minutesElapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
    long minutesRemaining = Math.max(0, record.getDurationMinutes() - minutesElapsed);

    Bounty bounty = bountyRepository.findById(record.getBountyId()).orElseThrow();

    return new BountyStatusVO(
        record.getBountyId(),
        record.getBountyName(),
        bounty.getDescription(),
        record.getStartTime(),
        record.getDurationMinutes(),
        minutesElapsed,
        minutesRemaining,
        record.getParsedRewardItems());
  }

  public String startBounty(Long userId, Long bountyId) {
    User user = userStateService.getUser(userId);

    if (user.getStatus() != UserStatus.IDLE) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法接取悬赏（需要 空闲 状态）");
    }

    Bounty bounty =
        bountyRepository
            .findById(bountyId)
            .orElseThrow(() -> new IllegalArgumentException("悬赏不存在"));

    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
    if (!bounty.getMapId().equals(mapNode.getId())) {
      throw new IllegalArgumentException("该悬赏不属于当前地图");
    }
    if (user.getLevel() < bounty.getRequireLevel()) {
      throw new IllegalArgumentException("等级不足，无法接取该悬赏");
    }

    // 种子随机预确定奖励物品（userId + 当日日期为种子）
    long seed = userId * 31 + LocalDate.now().toEpochDay();
    Random rng = new Random(seed);
    List<BountyRewardItem> predeterminedRewards = determineRewards(bounty, mapNode, rng);

    UserBounty record = new UserBounty();
    record.setUserId(userId);
    record.setBountyId(bountyId);
    record.setBountyName(bounty.getName());
    record.setStartTime(LocalDateTime.now());
    record.setDurationMinutes(bounty.getDurationMinutes());
    record.setRewards(predeterminedRewards.stream().map(BountyRewardItem::toMap).toList());
    record.setStatus("active");
    userBountyRepository.save(record);

    user.setStatus(UserStatus.BOUNTY);
    userStateService.save(user);

    log.info(
        "用户 {} 接取悬赏: {} (ID={}, 耗时{}分, 预存物品数={})",
        userId,
        bounty.getName(),
        bountyId,
        bounty.getDurationMinutes(),
        predeterminedRewards.size());

    return String.format("已接取悬赏「%s」，预计 %d 分钟后完成。", bounty.getName(), bounty.getDurationMinutes());
  }

  @Transactional
  public BountyRewardVO completeBounty(Long userId) {
    User user = userStateService.getUser(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法完成悬赏（需要 悬赏 状态）");
    }
    UserBounty record =
        userBountyRepository
            .findActiveByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

    // 检查时间
    long minutesElapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
    if (minutesElapsed < record.getDurationMinutes()) {
      long remaining = record.getDurationMinutes() - minutesElapsed;
      throw new IllegalArgumentException(
          String.format(
              "悬赏「%s」还需 %d 分钟（共需 %d 分）",
              record.getBountyName(), remaining, record.getDurationMinutes()));
    }

    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
    Bounty bounty = bountyRepository.findById(record.getBountyId()).orElseThrow();

    // 从预存记录中读取奖励物品
    List<BountyRewardItem> rewardItems = record.getParsedRewardItems();
    long spiritStones = 0;
    boolean hasBeastEgg = false;
    boolean hasEquipment = false;

    // 统计奖励类型
    for (BountyRewardItem item : rewardItems) {
      switch (item) {
        case BountyRewardItem.SpiritStonesReward(var amount) -> spiritStones = amount;
        case BountyRewardItem.BeastEggReward _ -> hasBeastEgg = true;
        case BountyRewardItem.EquipmentRewardItem _ -> hasEquipment = true;
        default -> {}
      }
    }

    // 分离物品 (非灵石、非兽卵、非装备)
    List<BountyRewardItem> items =
        rewardItems.stream()
            .filter(
                i ->
                    !(i instanceof BountyRewardItem.SpiritStonesReward)
                        && !(i instanceof BountyRewardItem.EquipmentRewardItem))
            .toList();

    // 发放物品
    addRewardsToInventory(userId, items);

    // 发放灵石
    if (spiritStones > 0) {
      user.setSpiritStones(user.getSpiritStones() + spiritStones);
    }

    // 旅行事件
    String eventDescription = resolveEvent(user, mapNode);

    // 奖励描述
    String rewardDescription =
        buildRewardDescription(spiritStones, items, hasBeastEgg, hasEquipment);

    // LLM 美化
    String beautified =
        beautifyBountyCompletion(
            mapNode, record.getBountyName(), rewardDescription, eventDescription, items);

    // 标记完成
    record.setStatus("completed");
    userBountyRepository.update(record);

    user.setStatus(UserStatus.IDLE);
    userStateService.save(user);

    log.info(
        "用户 {} 完成悬赏: {} (耗时{}分, 物品数={}, 灵石={})",
        userId,
        record.getBountyName(),
        minutesElapsed,
        items.size(),
        spiritStones);

    return new BountyRewardVO(
        userId,
        record.getBountyId(),
        record.getBountyName(),
        mapNode.getName(),
        minutesElapsed,
        beautified != null ? beautified : rewardDescription,
        eventDescription,
        items,
        spiritStones,
        hasBeastEgg,
        hasEquipment);
  }

  public String abandonBounty(Long userId) {
    User user = userStateService.getUser(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法放弃悬赏（需要 悬赏 状态）");
    }
    UserBounty record =
        userBountyRepository
            .findActiveByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

    record.setStatus("abandoned");
    userBountyRepository.update(record);

    user.setStatus(UserStatus.IDLE);
    userStateService.save(user);

    log.info("用户 {} 放弃悬赏: {}", userId, record.getBountyName());
    return String.format("已放弃悬赏「%s」，无任何产出。", record.getBountyName());
  }

  // ===================== 奖励预计算 =====================

  /** 接取时预确定奖励（种子随机） */
  private List<BountyRewardItem> determineRewards(Bounty bounty, MapNode mapNode, Random rng) {
    List<BountyRewardPool> pool = bounty.getRewards();
    if (pool.isEmpty()) return List.of();

    BountyRewardPool selected = WeightedRandom.select(pool, BountyRewardPool::weight, rng);
    if (selected == null) return List.of();

    return switch (selected) {
      case BountyRewardPool.RareItem(_, var minCount, var maxCount, _) -> {
        int count = minCount + rng.nextInt(maxCount - minCount + 1);
        yield findRareItems(mapNode, count, rng);
      }
      case BountyRewardPool.SpiritStones(_, var minAmount, var maxAmount, _) -> {
        long amount =
            minAmount + (minAmount == maxAmount ? 0 : (rng.nextLong(maxAmount - minAmount + 1)));
        yield List.of(new BountyRewardItem.SpiritStonesReward(amount));
      }
      case BountyRewardPool.BeastEgg _ -> {
        List<ItemTemplate> eggs = itemTemplateRepository.findByType(ItemType.BEAST_EGG);
        yield !eggs.isEmpty()
            ? List.of(
                new BountyRewardItem.BeastEggReward(
                    eggs.get(rng.nextInt(eggs.size())).getId(),
                    eggs.get(rng.nextInt(eggs.size())).getName()))
            : List.of();
      }
      case BountyRewardPool.EquipmentReward(_, var templateId, var name) -> {
        EquipmentTemplate equipmentTemplate =
            equipmentTemplateRepository.findById(templateId).orElse(null);
        yield equipmentTemplate != null
            ? List.of(new BountyRewardItem.EquipmentRewardItem(templateId, name))
            : List.of();
      }
    };
  }

  private List<BountyRewardItem> findRareItems(MapNode mapNode, int count, Random rng) {
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return List.of();

    // Batch lookup item templates
    Map<Long, ItemTemplate> templateMap =
        itemTemplateRepository
            .findByIds(
                specialties.stream()
                    .map(top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry::templateId)
                    .toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

    List<BountyRewardItem> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      SpecialtyEntry entry = WeightedRandom.select(specialties, SpecialtyEntry::weight, rng);
      if (entry == null) continue;
      Long templateId = entry.templateId();
      ItemTemplate template = templateMap.get(templateId);
      String name = template != null ? template.getName() : "未知物品";
      items.add(new BountyRewardItem.ItemReward(templateId, name, 1));
    }
    return items;
  }

  // ===================== 奖励发放 =====================

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

  // ===================== 事件 =====================

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

  // ===================== LLM 美化 =====================

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
