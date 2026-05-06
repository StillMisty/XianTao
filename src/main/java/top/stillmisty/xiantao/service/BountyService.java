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
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
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
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
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
  private final BountyCombatService bountyCombatService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<List<BountyVO>> listBounties(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(listBounties(userId));
  }

  @Authenticated
  public ServiceResult<BountyStatusVO> getBountyStatus(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getBountyStatus(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> startBounty(PlatformType platform, String openId, String bountyId) {
    Long userId = UserContext.getCurrentUserId();
    Long id;
    try {
      id = Long.parseLong(bountyId);
    } catch (NumberFormatException e) {
      return new ServiceResult.Failure<>("INVALID_INPUT", "请输入有效的悬赏编号");
    }
    return new ServiceResult.Success<>(startBounty(userId, id));
  }

  @Authenticated
  @Transactional
  public ServiceResult<BountyRewardVO> completeBounty(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(completeBounty(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> abandonBounty(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(abandonBounty(userId));
  }

  // ===================== 内部 API =====================

  public List<BountyVO> listBounties(Long userId) {
    User user = userStateService.loadUser(userId);
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
    User user = userStateService.loadUser(userId);

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
    if (!bounty.requiresLevel(user.getLevel())) {
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
    record.setRewards(predeterminedRewards);
    record.setStatus(BountyStatus.ACTIVE);
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
    return bountyCombatService.completeBounty(userId);
  }

  public String abandonBounty(Long userId) {
    User user = userStateService.loadUser(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法放弃悬赏（需要 悬赏 状态）");
    }
    UserBounty record =
        userBountyRepository
            .findActiveByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

    record.setStatus(BountyStatus.ABANDONED);
    userBountyRepository.save(record);

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
        if (!eggs.isEmpty()) {
          int idx = rng.nextInt(eggs.size());
          yield List.of(
              new BountyRewardItem.BeastEggReward(eggs.get(idx).getId(), eggs.get(idx).getName()));
        }
        yield List.of();
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
}
