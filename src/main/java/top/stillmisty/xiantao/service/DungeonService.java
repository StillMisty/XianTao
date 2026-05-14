package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonFirstClear;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.domain.dungeon.enums.PoiType;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonFirstClearRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonPoiConfigRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry;
import top.stillmisty.xiantao.domain.dungeon.vo.MonsterPoolEntry;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class DungeonService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonPoiConfigRepository poiConfigRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonProgressRepository progressRepository;
  private final DungeonFirstClearRepository firstClearRepository;
  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final UserRepository userRepository;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final CombatEngine combatEngine;
  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;
  private final StackableItemService stackableItemService;
  private final BeastRepository beastRepository;

  // ===================== 公开 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<List<DungeonListVO>> listDungeons(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(listDungeons(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> enterDungeon(
      PlatformType platform, String openId, String dungeonName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(enterDungeon(userId, dungeonName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ExploreResultVO> exploreDungeon(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(exploreDungeon(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> continueDungeon(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(continueDungeon(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> retreatDungeon(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(retreatDungeon(userId));
  }

  // ===================== 内部 API =====================

  public List<DungeonListVO> listDungeons(Long userId) {
    User user = userStateService.loadUser(userId);
    List<DungeonTemplate> templates = dungeonTemplateRepository.findActive();

    List<DungeonListVO> result = new ArrayList<>();
    for (DungeonTemplate tmpl : templates) {
      var progress = progressRepository.findByUserIdAndDungeonId(userId, tmpl.getId());
      var activeInstance =
          instanceRepository.findByLeaderIdAndDungeonIdAndStatus(
              userId, tmpl.getId(), DungeonStatus.ACTIVE);

      result.add(
          new DungeonListVO(
              tmpl.getId(),
              tmpl.getName(),
              tmpl.getElementType(),
              tmpl.getMinLevel(),
              tmpl.getMaxLevel(),
              tmpl.getMaxTeamSize(),
              activeInstance.isPresent(),
              activeInstance.map(DungeonInstance::getStatus).orElse(null),
              activeInstance.map(DungeonInstance::getCurrentArea).orElse(null),
              progress.map(DungeonProgress::getRewardCount).orElse(0),
              progress
                  .map(DungeonProgress::getDailyLimit)
                  .orElse(DungeonProgress.calculateDailyLimit(user.getLevel())),
              progress.map(p -> p.getFirstClear() != null && p.getFirstClear()).orElse(false)));
    }
    return result;
  }

  public String enterDungeon(Long userId, String dungeonName) {
    User user = userStateService.loadUser(userId);
    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findByName(dungeonName)
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, dungeonName));

    if (!dungeon.isAccessibleBy(user.getLevel())) {
      throw new BusinessException(
          ErrorCode.DUNGEON_LEVEL_INSUFFICIENT,
          dungeonName,
          dungeon.getMinLevel(),
          dungeon.getMaxLevel());
    }

    if (user.getLocationId() == null || !user.getLocationId().equals(dungeon.getMapNodeId())) {
      throw new BusinessException(ErrorCode.DUNGEON_NOT_AT_ENTRANCE, dungeonName);
    }

    if (user.getStatus() != UserStatus.IDLE) {
      throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, user.getStatus().getName());
    }

    var existing =
        instanceRepository.findByLeaderIdAndDungeonIdAndStatus(
            userId, dungeon.getId(), DungeonStatus.ACTIVE);
    if (existing.isPresent()) {
      throw new BusinessException(ErrorCode.DUNGEON_ALREADY_IN, dungeonName);
    }

    DungeonInstance instance = new DungeonInstance();
    instance.setDungeonId(dungeon.getId());
    instance.setLeaderId(userId);
    instance.setCurrentArea(DungeonArea.OUTER);
    instance.setPassageUnlocked(false);
    instance.setExploredPois(new ArrayList<>());
    instance.setStatus(DungeonStatus.ACTIVE);
    instance.setExpiresAt(LocalDateTime.now().plusHours(dungeon.getTimeoutHours()));
    instanceRepository.save(instance);

    user.setStatus(UserStatus.DUNGEON);
    userStateService.save(user);

    log.info("玩家 {} 进入秘境 {}", userId, dungeonName);
    StringBuilder sb = new StringBuilder();
    sb.append("你进入了【").append(dungeonName).append("】的外围区域。\n");
    sb.append("紫气弥漫，天地间充斥着锋锐的金行道韵。\n\n");

    List<DungeonPoiConfig> pois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), DungeonArea.OUTER);
    sb.append("可探索的建筑：\n");
    for (DungeonPoiConfig poi : pois) {
      sb.append("  · ")
          .append(poi.getName())
          .append(" [")
          .append(poi.getPoiType().getName())
          .append("]\n");
    }
    sb.append("\n输入「秘境探索」开始探索。");
    return sb.toString();
  }

  public ExploreResultVO exploreDungeon(Long userId) {
    User user = userStateService.loadUser(userId);

    DungeonInstance instance = findActiveInstance(userId);
    checkExpired(instance);

    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

    List<DungeonPoiConfig> areaPois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), instance.getCurrentArea());

    DungeonPoiConfig nextPoi = null;
    for (DungeonPoiConfig poi : areaPois) {
      if (!instance.hasExploredPoi(poi.getId())) {
        nextPoi = poi;
        break;
      }
    }

    if (nextPoi == null) {
      if (Boolean.TRUE.equals(instance.getPassageUnlocked())) {
        throw new BusinessException(ErrorCode.DUNGEON_PASSAGE_LOCKED);
      }
      throw new BusinessException(ErrorCode.DUNGEON_AREA_NOT_FOUND);
    }

    ExploreResultVO result = executePoi(user, instance, nextPoi);
    instance.addExploredPoi(nextPoi.getId());

    // Check if all non-passage POIs are explored
    checkAreaCompletion(instance, dungeon.getId(), areaPois);

    instanceRepository.save(instance);
    userStateService.save(user);

    return result;
  }

  public String continueDungeon(Long userId) {
    User user = userStateService.loadUser(userId);
    DungeonInstance instance = findActiveInstance(userId);
    checkExpired(instance);

    if (!Boolean.TRUE.equals(instance.getPassageUnlocked())) {
      throw new BusinessException(ErrorCode.DUNGEON_PASSAGE_LOCKED);
    }

    DungeonArea nextArea = getNextArea(instance.getCurrentArea());
    if (nextArea == null) {
      // Core completed - finish dungeon
      return completeDungeon(userId, instance);
    }

    instance.advanceArea();
    instanceRepository.save(instance);

    DungeonTemplate dungeon =
        dungeonTemplateRepository.findById(instance.getDungeonId()).orElseThrow();
    List<DungeonPoiConfig> pois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), instance.getCurrentArea());

    StringBuilder sb = new StringBuilder();
    sb.append("你进入了【")
        .append(dungeon.getName())
        .append("】的")
        .append(instance.getCurrentArea().getName())
        .append("区域。\n\n");
    sb.append("可探索的建筑：\n");
    for (DungeonPoiConfig poi : pois) {
      sb.append("  · ")
          .append(poi.getName())
          .append(" [")
          .append(poi.getPoiType().getName())
          .append("]\n");
    }
    sb.append("\n输入「秘境探索」继续探索。");
    return sb.toString();
  }

  public String retreatDungeon(Long userId) {
    User user = userStateService.loadUser(userId);
    DungeonInstance instance = findActiveInstance(userId);

    instance.markAbandoned();
    instanceRepository.save(instance);

    user.setStatus(UserStatus.IDLE);
    userStateService.save(user);

    log.info("玩家 {} 退出秘境 dungeonId={}", userId, instance.getDungeonId());
    return "你主动退出了秘境。已获得的奖励保留。";
  }

  // ===================== 私有方法 =====================

  private DungeonInstance findActiveInstance(Long userId) {
    List<DungeonTemplate> dungeons = dungeonTemplateRepository.findActive();
    for (DungeonTemplate d : dungeons) {
      var inst =
          instanceRepository.findByLeaderIdAndDungeonIdAndStatus(
              userId, d.getId(), DungeonStatus.ACTIVE);
      if (inst.isPresent()) return inst.get();
    }
    throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
  }

  private void checkExpired(DungeonInstance instance) {
    if (instance.isExpired()) {
      instance.markAbandoned();
      instanceRepository.save(instance);
      throw new BusinessException(ErrorCode.DUNGEON_INSTANCE_EXPIRED);
    }
  }

  private DungeonArea getNextArea(DungeonArea current) {
    return switch (current) {
      case OUTER -> DungeonArea.INNER;
      case INNER -> DungeonArea.CORE;
      case CORE -> null;
    };
  }

  private ExploreResultVO executePoi(User user, DungeonInstance instance, DungeonPoiConfig poi) {
    log.info("玩家 {} 探索 POI: {} ({}", user.getId(), poi.getName(), poi.getPoiType().getName());

    return switch (poi.getPoiType()) {
      case GATHER -> executeGather(user, poi);
      case SEARCH -> executeSearch(user, poi);
      case COMBAT -> executeCombat(user, instance, poi, false);
      case BOSS -> executeCombat(user, instance, poi, true);
    };
  }

  private ExploreResultVO executeGather(User user, DungeonPoiConfig poi) {
    List<DropItemVO> drops = rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(10, 31);

    giveDrops(user.getId(), drops, spiritStones);

    return new ExploreResultVO(
        poi.getName(),
        "采集",
        false,
        null,
        drops,
        0,
        spiritStones,
        false,
        "你在" + poi.getName() + "中采集到了一些物资。");
  }

  private ExploreResultVO executeSearch(User user, DungeonPoiConfig poi) {
    List<DropItemVO> drops = rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(20, 81);

    boolean triggerCombat = ThreadLocalRandom.current().nextDouble() < 0.2;
    String combatSummary = null;
    if (triggerCombat) {
      combatSummary = "搜索时遭遇了守护残魂，但被你轻松解决了。";
      spiritStones += ThreadLocalRandom.current().nextInt(20, 61);
    }

    giveDrops(user.getId(), drops, spiritStones);

    return new ExploreResultVO(
        poi.getName(),
        "搜索",
        triggerCombat,
        combatSummary,
        drops,
        0,
        spiritStones,
        false,
        "你在" + poi.getName() + "中仔细搜索了一番。");
  }

  private ExploreResultVO executeCombat(
      User user, DungeonInstance instance, DungeonPoiConfig poi, boolean isBoss) {
    MonsterPoolEntry monsterEntry =
        weightedRandom(poi.getMonsterPool(), MonsterPoolEntry::weight, poi.getMonsterWeightTotal());
    if (monsterEntry == null) {
      throw new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND);
    }

    MonsterTemplate monsterTmpl =
        monsterTemplateRepository
            .findById(monsterEntry.monsterTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND));

    Team playerTeam = combatService.buildPlayerTeam(user);

    int monsterLevel = monsterTmpl.getBaseLevel();
    Monster monster = new Monster(monsterTmpl, monsterLevel, List.of());
    Team monsterTeam = new Team(-1L, monster.getName());
    monsterTeam.addMember(monster);

    BattleContext context =
        BattleContext.builder()
            .teamA(playerTeam)
            .teamB(monsterTeam)
            .maxRounds(isBoss ? 30 : 20)
            .scene(BattleContext.BattleScene.DUNGEON)
            .mapId(instance.getDungeonId())
            .playerLevel(user.getLevel())
            .build();
    BattleResultVO battleResult = combatEngine.simulate(context);

    postCombatProcessor.applyHpToUser(user, playerTeam);

    Map<Long, Beast> beastCache = new HashMap<>();
    postCombatProcessor.applyHpToBeasts(playerTeam, user, true, false, beastCache);
    beastCache.values().forEach(beastRepository::save);

    userStateService.save(user);

    boolean playerWon = "Player".equals(battleResult.winner());

    if (!playerWon) {
      // Check if party wiped (for solo: user HP=0)
      if (user.getHpCurrent() <= 0) {
        instance.markFailed();
        instanceRepository.save(instance);
        user.setStatus(UserStatus.DYING);
        user.setDyingStartTime(LocalDateTime.now());
        userStateService.save(user);
        throw new BusinessException(ErrorCode.DUNGEON_COMBAT_LOST);
      }
      // Player HP > 0 but lost (could be partial survival)
      // For now, similar to failure
    }

    List<DropItemVO> drops = new ArrayList<>();
    long expGained = battleResult.expGained();
    long spiritStones = 0;

    if (playerWon) {
      List<DropItemVO> lootDrops = rollLoot(poi);
      drops.addAll(lootDrops);

      spiritStones = ThreadLocalRandom.current().nextInt(isBoss ? 100 : 30, isBoss ? 500 : 150);

      giveDrops(user.getId(), drops, spiritStones);

      // Give exp to user
      if (expGained > 0) {
        user.addExp(expGained);
      }
    }

    String summary = battleResult.summary();
    if (summary != null && summary.length() > 200) {
      summary = summary.substring(0, 200) + "...";
    }

    return new ExploreResultVO(
        poi.getName(),
        isBoss ? "BOSS战" : "战斗",
        true,
        playerWon
            ? ("击败了" + monster.getName() + "！\n" + (summary != null ? summary : ""))
            : "被" + monster.getName() + "击败...",
        drops,
        expGained,
        spiritStones,
        false,
        playerWon ? "战斗胜利！" : "战斗失败...");
  }

  private void checkAreaCompletion(
      DungeonInstance instance, Long dungeonId, List<DungeonPoiConfig> areaPois) {
    boolean allExplored = areaPois.stream().allMatch(poi -> instance.hasExploredPoi(poi.getId()));
    if (allExplored && !Boolean.TRUE.equals(instance.getPassageUnlocked())) {
      instance.setPassageUnlocked(true);
    }
  }

  private String completeDungeon(Long userId, DungeonInstance instance) {
    DungeonTemplate dungeon =
        dungeonTemplateRepository.findById(instance.getDungeonId()).orElseThrow();
    instance.markCompleted();
    instanceRepository.save(instance);

    User user = userStateService.loadUser(userId);
    user.setStatus(UserStatus.IDLE);
    userStateService.save(user);

    // Update progress
    DungeonProgress progress =
        progressRepository
            .findByUserIdAndDungeonId(userId, dungeon.getId())
            .orElseGet(
                () -> {
                  DungeonProgress p = new DungeonProgress();
                  p.setUserId(userId);
                  p.setDungeonId(dungeon.getId());
                  p.setRewardCount(0);
                  p.setDailyLimit(DungeonProgress.calculateDailyLimit(user.getLevel()));
                  p.setFirstClear(false);
                  p.setLastRewardDate(java.time.LocalDate.now());
                  return p;
                });

    boolean isFirstClear = progress.getFirstClear() == null || !progress.getFirstClear();
    if (isFirstClear) {
      // Check global first clear
      DungeonFirstClear globalFirst =
          firstClearRepository.findByDungeonId(dungeon.getId()).orElse(null);
      if (globalFirst == null) {
        globalFirst = new DungeonFirstClear();
        globalFirst.setDungeonId(dungeon.getId());
        globalFirst.setTeamMembers(List.of(userId));
        globalFirst.setDurationMinutes(
            (int)
                java.time.Duration.between(instance.getCreatedAt(), LocalDateTime.now())
                    .toMinutes());
        firstClearRepository.save(globalFirst);
      }
      progress.setFirstClear(true);
    }

    // Record best area
    if (progress.getBestArea() == null
        || progress.getBestArea().ordinal() < DungeonArea.CORE.ordinal()) {
      progress.setBestArea(DungeonArea.CORE);
    }

    // Give completion reward
    long spiritStonesReward = ThreadLocalRandom.current().nextInt(500, 2001);
    user.setSpiritStones(
        (user.getSpiritStones() != null ? user.getSpiritStones() : 0) + spiritStonesReward);
    userStateService.save(user);

    // Record reward only if within daily limit
    boolean rewardGiven = false;
    if (progress.canGetReward()) {
      progress.recordReward();
      rewardGiven = true;
    }
    progressRepository.save(progress);

    StringBuilder sb = new StringBuilder();
    sb.append("恭喜！你成功通关了【").append(dungeon.getName()).append("】！\n");
    sb.append("获得灵石 ×").append(spiritStonesReward).append("\n");
    if (isFirstClear) {
      sb.append("★ 首次通关记录！\n");
    }
    if (!rewardGiven) {
      sb.append("今日通关奖励次数已达上限。\n");
    }
    return sb.toString();
  }

  // ===================== 掉落逻辑 =====================

  private List<DropItemVO> rollLoot(DungeonPoiConfig poi) {
    List<DropItemVO> drops = new ArrayList<>();
    if (!poi.hasLootPool()) return drops;

    int rollCount =
        1
            + ThreadLocalRandom.current()
                .nextInt(
                    poi.getPoiType() == PoiType.BOSS ? 3 : 1,
                    poi.getPoiType() == PoiType.BOSS ? 5 : 2);

    for (int i = 0; i < rollCount; i++) {
      LootPoolEntry entry =
          weightedRandom(poi.getLootPool(), LootPoolEntry::weight, poi.getLootWeightTotal());
      if (entry == null) continue;

      int qty = ThreadLocalRandom.current().nextInt(entry.minQty(), entry.maxQty() + 1);
      String itemName =
          itemTemplateRepository.findById(entry.templateId()).map(t -> t.getName()).orElse("未知物品");
      drops.add(new DropItemVO(itemName, qty));
    }
    return drops;
  }

  private <T> T weightedRandom(
      List<T> items, java.util.function.ToIntFunction<T> weightFn, int totalWeight) {
    if (items == null || items.isEmpty() || totalWeight <= 0) return null;
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (T item : items) {
      cumulative += weightFn.applyAsInt(item);
      if (roll < cumulative) return item;
    }
    return items.getLast();
  }

  private void giveDrops(Long userId, List<DropItemVO> drops, long spiritStones) {
    for (DropItemVO drop : drops) {
      var template = itemTemplateRepository.findByName(drop.name());
      if (template.isPresent()) {
        stackableItemService.addStackableItem(
            userId,
            template.get().getId(),
            template.get().getType(),
            template.get().getName(),
            drop.quantity());
      }
    }
    if (spiritStones > 0) {
      User user = userStateService.loadUser(userId);
      user.setSpiritStones(
          (user.getSpiritStones() != null ? user.getSpiritStones() : 0) + spiritStones);
      userStateService.save(user);
    }
  }
}
