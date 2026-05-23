package top.stillmisty.xiantao.service.bounty;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
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
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

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
    long id;
    try {
      id = Long.parseLong(bountyId);
    } catch (NumberFormatException e) {
      return new ServiceResult.Failure<>(BOUNTY_ID_INVALID, "请输入有效的悬赏编号");
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

  @Cacheable(cacheNames = "bounties", key = "#userId")
  public List<BountyVO> listBounties(Long userId) {
    User user = userStateService.loadUser(userId);
    MapNode mapNode =
        mapNodeRepository
            .findById(user.getLocationId())
            .orElseThrow(() -> new BusinessException(MAP_CURRENT_NOT_FOUND));

    // 先查出全量，再找出唯一悬赏的 completed IDs，交给 SQL 层排除
    List<Bounty> allBounties = bountyRepository.findByMapId(mapNode.getId());
    List<Long> uniqueBountyIds =
        allBounties.stream()
            .filter(b -> b.getIsUnique() != null && b.getIsUnique())
            .map(Bounty::getId)
            .toList();
    Set<Long> excludeIds =
        uniqueBountyIds.isEmpty()
            ? Set.of()
            : new HashSet<>(userBountyRepository.findCompletedBountyIds(userId, uniqueBountyIds));

    List<Bounty> eligible =
        (excludeIds.isEmpty()
                ? allBounties
                : bountyRepository.findByMapIdExcluding(mapNode.getId(), excludeIds))
            .stream().filter(b -> b.requiresLevel(user.getLevel())).toList();

    if (eligible.isEmpty()) return List.of();

    // Deterministic seed: per user, per map, per day → same list within same day
    long seed = userId * 31 + mapNode.getId() * 17 + LocalDate.now().toEpochDay();
    Random rng = new Random(seed);

    int count = rng.nextInt(2) + 3; // 3–4 bounties
    count = Math.min(count, eligible.size());

    List<Bounty> selected =
        WeightedRandom.selectN(
            eligible, b -> b.getEventWeight() != null ? b.getEventWeight() : 0, count, rng);

    return selected.stream()
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

  @Cacheable(cacheNames = "bounties", key = "'status:' + #userId")
  public BountyStatusVO getBountyStatus(Long userId) {
    UserBounty record = userBountyRepository.findActiveByUserId(userId).orElse(null);
    if (record == null) {
      return new BountyStatusVO(null, "无进行中的悬赏", "", null, 0, 0, 0, List.of());
    }

    long minutesElapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
    long minutesRemaining = Math.max(0, record.getDurationMinutes() - minutesElapsed);

    Bounty bounty =
        bountyRepository
            .findById(record.getBountyId())
            .orElseThrow(() -> new BusinessException(BOUNTY_NOT_FOUND));

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

  @CacheEvict(cacheNames = "bounties", key = "'status:' + #userId")
  public String startBounty(Long userId, Long bountyId) {
    User user = userStateService.loadUserForUpdate(userId);

    if (user.getStatus() != UserStatus.IDLE) {
      throw new BusinessException(STATUS_BLOCKED, user.getStatus().getName(), "空闲");
    }

    Bounty bounty =
        bountyRepository
            .findById(bountyId)
            .orElseThrow(() -> new BusinessException(BOUNTY_NOT_FOUND));

    MapNode mapNode =
        mapNodeRepository
            .findById(user.getLocationId())
            .orElseThrow(() -> new BusinessException(MAP_CURRENT_NOT_FOUND));
    if (!bounty.getMapId().equals(mapNode.getId())) {
      throw new BusinessException(BOUNTY_WRONG_MAP);
    }
    if (!bounty.requiresLevel(user.getLevel())) {
      throw new BusinessException(BOUNTY_LEVEL_INSUFFICIENT);
    }

    if (bounty.getIsUnique() != null
        && bounty.getIsUnique()
        && userBountyRepository.findCompletedByUserIdAndBountyId(userId, bountyId).isPresent()) {
      throw new BusinessException(BOUNTY_ALREADY_COMPLETED);
    }

    long seed = userId * 31 + LocalDate.now().toEpochDay() + ThreadLocalRandom.current().nextLong();
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
    record.setHiddenClues(Map.of()); // hidden clues checked at start
    userBountyRepository.save(record);

    user.setStatus(UserStatus.BOUNTY);
    user.setActivityType(ActivityType.BOUNTY);
    user.setActivityStartTime(LocalDateTime.now());
    user.setActivityTargetId(record.getId());
    userStateService.saveActivity(user);

    log.info(
        "玩家 {} 接取悬赏: {} (ID={}, 耗时{}分, 预存物品数={})",
        userId,
        bounty.getName(),
        bountyId,
        bounty.getDurationMinutes(),
        predeterminedRewards.size());

    return String.format("已接取悬赏「%s」，预计 %d 分钟后完成。", bounty.getName(), bounty.getDurationMinutes());
  }

  @Caching(
      evict = {
        @CacheEvict(cacheNames = "bounties", key = "#userId"),
        @CacheEvict(cacheNames = "bounties", key = "'status:' + #userId")
      })
  public BountyRewardVO completeBounty(Long userId) {
    return bountyCombatService.completeBounty(userId);
  }

  @Transactional
  @CacheEvict(cacheNames = "bounties", key = "'status:' + #userId")
  public String abandonBounty(Long userId) {
    User user = userStateService.loadUserForUpdate(userId);
    if (user.getStatus() != UserStatus.BOUNTY) {
      throw new BusinessException(STATUS_BLOCKED, user.getStatus().getName(), "悬赏");
    }
    UserBounty record =
        userBountyRepository
            .findActiveByUserIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(BOUNTY_NO_ACTIVE));

    record.setStatus(BountyStatus.ABANDONED);
    userBountyRepository.save(record);

    user.setStatus(UserStatus.IDLE);
    user.clearActivity();
    userStateService.saveActivity(user);

    log.info("玩家 {} 放弃悬赏: {}", userId, record.getBountyName());
    return String.format("已放弃悬赏「%s」，无任何产出。", record.getBountyName());
  }

  // ===================== 奖励预计算 =====================

  private List<BountyRewardItem> determineRewards(Bounty bounty, MapNode mapNode, Random rng) {
    List<BountyRewardPool> pool = bounty.getRewards();
    if (pool.isEmpty()) return List.of();

    List<BountyRewardItem> allRewards = new ArrayList<>();
    for (BountyRewardPool entry : pool) {
      allRewards.addAll(
          switch (entry) {
            case BountyRewardPool.RareItem(var minCount, var maxCount, _) -> {
              int count = minCount + rng.nextInt(maxCount - minCount + 1);
              yield findRareItems(mapNode, count, rng);
            }
            case BountyRewardPool.SpiritStones(var minAmount, var maxAmount) -> {
              long amount =
                  minAmount
                      + (minAmount == maxAmount ? 0 : (rng.nextLong(maxAmount - minAmount + 1)));
              yield List.of(new BountyRewardItem.SpiritStonesReward(amount));
            }
            case BountyRewardPool.BeastEgg(_, var templateId) -> {
              if (templateId != null) {
                ItemTemplate egg = itemTemplateRepository.findById(templateId).orElse(null);
                yield egg != null
                    ? List.of(new BountyRewardItem.BeastEggReward(templateId, egg.getName()))
                    : List.of();
              }
              List<ItemTemplate> eggs = itemTemplateRepository.findByType(ItemType.BEAST_EGG);
              if (!eggs.isEmpty()) {
                int idx = rng.nextInt(eggs.size());
                yield List.of(
                    new BountyRewardItem.BeastEggReward(
                        eggs.get(idx).getId(), eggs.get(idx).getName()));
              }
              yield List.of();
            }
            case BountyRewardPool.EquipmentReward(var templateId) -> {
              EquipmentTemplate equipmentTemplate =
                  equipmentTemplateRepository.findById(templateId).orElse(null);
              yield equipmentTemplate != null
                  ? List.of(
                      new BountyRewardItem.EquipmentRewardItem(
                          templateId, equipmentTemplate.getName()))
                  : List.of();
            }
            case BountyRewardPool.SkillJade(var templateId) -> {
              ItemTemplate skillJade = itemTemplateRepository.findById(templateId).orElse(null);
              yield skillJade != null
                  ? List.of(
                      new BountyRewardItem.SkillJadeRewardItem(templateId, skillJade.getName()))
                  : List.of();
            }
            case BountyRewardPool.Potion(var templateId) -> {
              ItemTemplate potion = itemTemplateRepository.findById(templateId).orElse(null);
              yield potion != null
                  ? List.of(new BountyRewardItem.ItemReward(templateId, potion.getName(), 1))
                  : List.of();
            }
            case BountyRewardPool.RecipeScroll(var templateId) -> {
              ItemTemplate scroll = itemTemplateRepository.findById(templateId).orElse(null);
              yield scroll != null
                  ? List.of(new BountyRewardItem.ItemReward(templateId, scroll.getName(), 1))
                  : List.of();
            }
            case BountyRewardPool.ForgingBlueprint(var templateId) -> {
              ItemTemplate bp = itemTemplateRepository.findById(templateId).orElse(null);
              yield bp != null
                  ? List.of(new BountyRewardItem.ItemReward(templateId, bp.getName(), 1))
                  : List.of();
            }
          });
    }
    return allRewards;
  }

  private List<BountyRewardItem> findRareItems(MapNode mapNode, int count, Random rng) {
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return List.of();

    Map<Long, ItemTemplate> templateMap =
        itemTemplateRepository
            .findByIds(specialties.stream().map(SpecialtyEntry::templateId).toList())
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
