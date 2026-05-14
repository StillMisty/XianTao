package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonPoiConfigRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.dungeon.DungeonCombatHelper;
import top.stillmisty.xiantao.service.dungeon.DungeonLootHelper;
import top.stillmisty.xiantao.service.dungeon.DungeonProgressHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DungeonService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonPoiConfigRepository poiConfigRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonProgressRepository progressRepository;
  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final DungeonCombatHelper combatHelper;
  private final DungeonLootHelper lootHelper;
  private final DungeonProgressHelper progressHelper;

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
        return new ExploreResultVO(
            "", "区域完成", true, "当前区域已探索完毕，输入「秘境深入」继续推进。", null, 0, 0, false, "已完成");
      }
      throw new BusinessException(ErrorCode.DUNGEON_AREA_NOT_FOUND);
    }

    ExploreResultVO exploreResult = executePoi(user, instance, nextPoi);
    instance.addExploredPoi(nextPoi.getId());

    checkAreaCompletion(instance, dungeon.getId(), areaPois);

    instanceRepository.save(instance);
    userStateService.save(user);

    return exploreResult;
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
      return progressHelper.completeDungeon(userId, instance);
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
      instance.setStatus(DungeonStatus.ABANDONED);
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
      case GATHER -> lootHelper.executeGather(user, poi);
      case SEARCH -> lootHelper.executeSearch(user, poi);
      case COMBAT -> executePoiCombat(user, instance, poi, false);
      case BOSS -> executePoiCombat(user, instance, poi, true);
    };
  }

  private ExploreResultVO executePoiCombat(
      User user, DungeonInstance instance, DungeonPoiConfig poi, boolean isBoss) {
    DungeonCombatHelper.CombatOutcome outcome =
        combatHelper.executeCombat(user, instance, poi, isBoss);

    if (!outcome.playerWon()) {
      instance.markFailed();
      instanceRepository.save(instance);
      userStateService.save(user);
      throw new BusinessException(ErrorCode.DUNGEON_COMBAT_LOST);
    }

    List<DropItemVO> drops = new ArrayList<>();
    long spiritStones = 0;

    if (outcome.playerWon()) {
      List<DropItemVO> lootDrops = lootHelper.rollLoot(poi);
      drops.addAll(lootDrops);

      spiritStones = ThreadLocalRandom.current().nextInt(isBoss ? 100 : 30, isBoss ? 500 : 150);

      lootHelper.giveDrops(user.getId(), drops, spiritStones);
    }

    return new ExploreResultVO(
        poi.getName(),
        isBoss ? "BOSS战" : "战斗",
        true,
        outcome.summary(),
        drops,
        outcome.expGained(),
        spiritStones,
        false,
        outcome.playerWon() ? "战斗胜利！" : "战斗失败...");
  }

  private void checkAreaCompletion(
      DungeonInstance instance, Long dungeonId, List<DungeonPoiConfig> areaPois) {
    boolean allExplored = areaPois.stream().allMatch(poi -> instance.hasExploredPoi(poi.getId()));
    if (allExplored && !Boolean.TRUE.equals(instance.getPassageUnlocked())) {
      instance.setPassageUnlocked(true);
    }
  }
}
