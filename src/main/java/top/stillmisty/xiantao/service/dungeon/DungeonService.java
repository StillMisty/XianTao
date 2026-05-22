package top.stillmisty.xiantao.service.dungeon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonContinueResult;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonEnterResult;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonEnterResult.DungeonPoiEntry;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;
import top.stillmisty.xiantao.domain.team.repository.TeamMemberRepository;
import top.stillmisty.xiantao.domain.team.repository.TeamRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DungeonService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonPoiConfigRepository poiConfigRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonProgressRepository progressRepository;
  private final UserStateService userStateService;
  private final DungeonCombatHelper combatHelper;
  private final DungeonLootHelper lootHelper;
  private final DungeonProgressHelper progressHelper;
  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;

  @Lazy @Autowired private DungeonService self;

  // ===================== 公开 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<List<DungeonListVO>> listDungeons(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(listDungeons(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<DungeonEnterResult> enterDungeon(
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
  public ServiceResult<DungeonContinueResult> continueDungeon(
      PlatformType platform, String openId) {
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
      DungeonInstance activeInstance = findActiveInstanceRaw(userId, tmpl.getId());

      result.add(
          new DungeonListVO(
              tmpl.getId(),
              tmpl.getName(),
              tmpl.getMinLevel(),
              tmpl.getMaxLevel(),
              tmpl.getMaxTeamSize(),
              activeInstance != null,
              activeInstance != null ? activeInstance.getStatus() : null,
              activeInstance != null ? activeInstance.getCurrentArea() : null,
              progress.map(DungeonProgress::getRewardCount).orElse(0),
              progress
                  .map(DungeonProgress::getDailyLimit)
                  .orElse(DungeonProgress.calculateDailyLimit(user.getLevel())),
              progress.map(p -> p.getFirstClear() != null && p.getFirstClear()).orElse(false)));
    }
    return result;
  }

  public DungeonEnterResult enterDungeon(Long userId, String dungeonName) {
    User user = userStateService.loadUserForUpdate(userId);
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

    checkIdleStatus(user);

    DungeonInstance existing = findActiveInstanceRaw(userId, dungeon.getId());
    if (existing != null) {
      throw new BusinessException(ErrorCode.DUNGEON_ALREADY_IN, dungeonName);
    }

    Long teamId = null;
    List<Long> memberIds = List.of(userId);

    var teamMemberOpt = teamMemberRepository.findByUserId(userId);
    if (teamMemberOpt.isPresent()) {
      TeamMember tm = teamMemberOpt.get();
      var team =
          teamRepository
              .findById(tm.getTeamId())
              .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_IN));
      if (!team.getLeaderId().equals(userId)) {
        throw new BusinessException(ErrorCode.DUNGEON_NOT_LEADER);
      }
      teamId = team.getId();
      List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
      memberIds = members.stream().map(TeamMember::getUserId).toList();

      if (memberIds.size() > dungeon.getMaxTeamSize()) {
        throw new BusinessException(ErrorCode.DUNGEON_TEAM_SIZE_EXCEED, dungeon.getMaxTeamSize());
      }

      var sortedMemberIds = memberIds.stream().sorted().toList();
      for (Long memberId : sortedMemberIds) {
        User memberUser = userStateService.loadUserForUpdate(memberId);
        checkIdleStatus(memberUser);
        DungeonInstance memberExisting = findActiveInstanceRaw(memberId, dungeon.getId());
        if (memberExisting != null) {
          throw new BusinessException(ErrorCode.DUNGEON_ALREADY_IN, dungeonName);
        }
      }
    }

    List<DungeonPoiConfig> outerPois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), DungeonArea.OUTER);
    Long passagePoiId = pickPassagePoi(outerPois);

    DungeonInstance instance = new DungeonInstance();
    instance.setDungeonId(dungeon.getId());
    instance.setLeaderId(userId);
    instance.setTeamId(teamId);
    instance.setCurrentArea(DungeonArea.OUTER);
    instance.setPassageUnlocked(false);
    instance.setPassagePoiId(passagePoiId);
    instance.setHasCoreToken(false);
    instance.setExploredPois(new ArrayList<>());
    instance.setStatus(DungeonStatus.ACTIVE);
    instance.setExpiresAt(LocalDateTime.now().plusHours(dungeon.getTimeoutHours()));
    instanceRepository.save(instance);

    for (Long memberId : memberIds) {
      User memberUser =
          memberId.equals(userId) ? user : userStateService.loadUserForUpdate(memberId);
      memberUser.setStatus(UserStatus.DUNGEON);
      memberUser.setActivityType(ActivityType.DUNGEON);
      memberUser.setActivityStartTime(LocalDateTime.now());
      memberUser.setActivityTargetId(instance.getId());
      userStateService.saveActivity(memberUser);
    }

    log.info("玩家 {} 率队 {} 人进入秘境 {}", userId, memberIds.size(), dungeonName);
    return new DungeonEnterResult(
        dungeonName,
        DungeonArea.OUTER.getName(),
        memberIds.size(),
        outerPois.stream()
            .map(
                poi ->
                    new DungeonPoiEntry(
                        poi.getName(),
                        poi.getPoiType().getName(),
                        poi.getUnlockCondition() != null && !poi.getUnlockCondition().isBlank()))
            .toList());
  }

  public ExploreResultVO exploreDungeon(Long userId) {
    User user = userStateService.loadUser(userId);
    DungeonInstance instance = findActiveInstance(userId);

    if (!instance.getLeaderId().equals(userId)) {
      throw new BusinessException(ErrorCode.DUNGEON_NOT_LEADER);
    }

    checkExpired(instance);

    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

    List<DungeonPoiConfig> areaPois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), instance.getCurrentArea());

    DungeonPoiConfig nextPoi = null;
    for (DungeonPoiConfig poi : areaPois) {
      if (!instance.hasExploredPoi(poi.getId()) && isPoiUnlocked(poi, instance)) {
        nextPoi = poi;
        break;
      }
    }

    if (nextPoi == null) {
      if (Boolean.TRUE.equals(instance.getPassageUnlocked())) {
        return new ExploreResultVO(
            "", "区域完成", false, "当前区域已探索完毕，输入「秘境继续」继续推进。", null, 0, 0, false, "已完成");
      }
      throw new BusinessException(ErrorCode.DUNGEON_AREA_NOT_FOUND);
    }

    ExploreResultVO exploreResult = executePoi(user, instance, nextPoi);
    instance.addExploredPoi(nextPoi.getId());

    if (nextPoi.getId().equals(instance.getPassagePoiId())) {
      instance.setPassageUnlocked(true);
    }

    if (nextPoi.getUnlockCondition() != null && nextPoi.getUnlockCondition().equals("CORE_TOKEN")) {
      instance.setHasCoreToken(true);
    }

    instanceRepository.save(instance);
    userStateService.save(user);

    return exploreResult;
  }

  public DungeonContinueResult continueDungeon(Long userId) {
    User user = userStateService.loadUser(userId);
    DungeonInstance instance = findActiveInstance(userId);

    if (!instance.getLeaderId().equals(userId)) {
      throw new BusinessException(ErrorCode.DUNGEON_NOT_LEADER);
    }

    checkExpired(instance);

    if (!Boolean.TRUE.equals(instance.getPassageUnlocked())) {
      throw new BusinessException(ErrorCode.DUNGEON_PASSAGE_LOCKED);
    }

    if (instance.getCurrentArea() == DungeonArea.INNER
        && !Boolean.TRUE.equals(instance.getHasCoreToken())) {
      throw new BusinessException(ErrorCode.DUNGEON_PASSAGE_LOCKED);
    }

    DungeonArea nextArea = getNextArea(instance.getCurrentArea());
    if (nextArea == null) {
      return new DungeonContinueResult.Completed(settleAllMembers(instance));
    }

    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));
    List<DungeonPoiConfig> pois =
        poiConfigRepository.findByDungeonIdAndArea(dungeon.getId(), nextArea);
    Long passagePoiId = pickPassagePoi(pois);

    instance.advanceArea();
    instance.setPassagePoiId(passagePoiId);
    instanceRepository.save(instance);

    DungeonEnterResult areaView =
        new DungeonEnterResult(
            dungeon.getName(),
            instance.getCurrentArea().getName(),
            getTeamMemberIds(instance).size(),
            pois.stream()
                .map(
                    poi ->
                        new DungeonPoiEntry(
                            poi.getName(),
                            poi.getPoiType().getName(),
                            poi.getUnlockCondition() != null
                                && !poi.getUnlockCondition().isBlank()))
                .toList());
    return new DungeonContinueResult.AreaView(areaView);
  }

  public String retreatDungeon(Long userId) {
    User user = userStateService.loadUser(userId);
    DungeonInstance instance = findActiveInstance(userId);

    if (instance.getLeaderId().equals(userId)) {
      return retreatCaptain(instance);
    }

    return retreatMember(userId, instance);
  }

  // ===================== 私有方法 =====================

  private void checkIdleStatus(User user) {
    if (user.getStatus() != UserStatus.IDLE) {
      throw new BusinessException(ErrorCode.DUNGEON_STATUS_BLOCKED, user.getStatus().getName());
    }
  }

  private void appendBuildingPrompt(StringBuilder sb, List<DungeonPoiConfig> pois) {
    sb.append("可探索的建筑：\n");
    for (DungeonPoiConfig poi : pois) {
      sb.append("  · ")
          .append(poi.getName())
          .append(" [")
          .append(poi.getPoiType().getName())
          .append("]");
      if (poi.getUnlockCondition() != null && !poi.getUnlockCondition().isBlank()) {
        sb.append(" 🔒");
      }
      sb.append("\n");
    }
  }

  private DungeonInstance findActiveInstance(Long userId) {
    User user = userStateService.loadUser(userId);
    if (user.getActivityTargetId() == null) {
      throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
    }
    return instanceRepository
        .findByIdForUpdate(user.getActivityTargetId())
        .filter(DungeonInstance::isActive)
        .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE));
  }

  private DungeonInstance findActiveInstanceRaw(Long userId, Long dungeonId) {
    return instanceRepository
        .findByLeaderIdAndDungeonIdAndStatus(userId, dungeonId, DungeonStatus.ACTIVE)
        .orElse(null);
  }

  private void checkExpired(DungeonInstance instance) {
    if (instance.isExpired()) {
      self.markInstanceAbandoned(instance);
      throw new BusinessException(ErrorCode.DUNGEON_INSTANCE_EXPIRED);
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void markInstanceFailed(DungeonInstance instance) {
    instance.markFailed();
    instanceRepository.save(instance);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void markInstanceAbandoned(DungeonInstance instance) {
    instance.setStatus(DungeonStatus.ABANDONED);
    instanceRepository.save(instance);
  }

  private DungeonArea getNextArea(DungeonArea current) {
    return switch (current) {
      case OUTER -> DungeonArea.INNER;
      case INNER -> DungeonArea.CORE;
      case CORE -> null;
    };
  }

  private Long pickPassagePoi(List<DungeonPoiConfig> pois) {
    List<DungeonPoiConfig> candidates =
        pois.stream().filter(p -> Boolean.TRUE.equals(p.getIsPassage())).toList();
    if (candidates.isEmpty()) {
      return pois.isEmpty() ? null : pois.getFirst().getId();
    }
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).getId();
  }

  private boolean isPoiUnlocked(DungeonPoiConfig poi, DungeonInstance instance) {
    if (poi.getUnlockCondition() == null || poi.getUnlockCondition().isBlank()) {
      return true;
    }
    return switch (poi.getUnlockCondition()) {
      case "CORE_TOKEN", "BOSS_CLEARED" -> Boolean.TRUE.equals(instance.getHasCoreToken());
      default -> true;
    };
  }

  private List<Long> getTeamMemberIds(DungeonInstance instance) {
    if (instance.getTeamId() == null) {
      return List.of(instance.getLeaderId());
    }
    return teamMemberRepository.findByTeamId(instance.getTeamId()).stream()
        .map(TeamMember::getUserId)
        .toList();
  }

  private ExploreResultVO executePoi(User user, DungeonInstance instance, DungeonPoiConfig poi) {
    log.debug("探索 POI: {} (type={})", poi.getName(), poi.getPoiType().getName());

    return switch (poi.getPoiType()) {
      case GATHER -> executeGatherForTeam(instance, poi);
      case SEARCH -> executeSearchForTeam(instance, poi);
      case COMBAT -> executePoiCombat(user, instance, poi, false);
      case BOSS -> executePoiCombat(user, instance, poi, true);
    };
  }

  private ExploreResultVO executeGatherForTeam(DungeonInstance instance, DungeonPoiConfig poi) {
    List<DropItemVO> drops = lootHelper.rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(10, 31);
    List<Long> memberIds = getTeamMemberIds(instance);

    for (Long memberId : memberIds) {
      lootHelper.giveDrops(memberId, drops, spiritStones);
    }

    StringBuilder msg = new StringBuilder("队伍在" + poi.getName() + "中采集到了一些物资。");
    if (memberIds.size() > 1) {
      msg.append("（全员 ").append(memberIds.size()).append(" 人获得奖励）");
    }
    return new ExploreResultVO(
        poi.getName(), "采集", false, null, drops, 0, spiritStones, false, msg.toString());
  }

  private ExploreResultVO executeSearchForTeam(DungeonInstance instance, DungeonPoiConfig poi) {
    List<DropItemVO> drops = lootHelper.rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(20, 81);

    boolean triggerCombat = ThreadLocalRandom.current().nextDouble() < 0.2;
    String combatSummary = null;
    if (triggerCombat) {
      combatSummary = "搜索时遭遇了守护残魂，但被轻松解决了。";
      spiritStones += ThreadLocalRandom.current().nextInt(20, 61);
    }

    List<Long> memberIds = getTeamMemberIds(instance);
    for (Long memberId : memberIds) {
      lootHelper.giveDrops(memberId, drops, spiritStones);
    }

    StringBuilder msg = new StringBuilder("队伍在" + poi.getName() + "中仔细搜索了一番。");
    if (memberIds.size() > 1) {
      msg.append("（全员 ").append(memberIds.size()).append(" 人获得奖励）");
    }
    return new ExploreResultVO(
        poi.getName(),
        "搜索",
        triggerCombat,
        combatSummary,
        drops,
        0,
        spiritStones,
        false,
        msg.toString());
  }

  private ExploreResultVO executePoiCombat(
      User user, DungeonInstance instance, DungeonPoiConfig poi, boolean isBoss) {
    List<Long> memberIds = getTeamMemberIds(instance);

    DungeonCombatHelper.CombatOutcome outcome =
        combatHelper.executeCombatForTeam(user, instance, poi, isBoss, memberIds);

    if (!outcome.playerWon()) {
      boolean anyAlive = outcome.memberAlive();
      if (!anyAlive) {
        self.markInstanceFailed(instance);
        throw new BusinessException(ErrorCode.DUNGEON_COMBAT_LOST);
      }
      return new ExploreResultVO(
          poi.getName(),
          isBoss ? "BOSS战" : "战斗",
          true,
          outcome.summary(),
          null,
          0,
          0,
          false,
          "部分成员阵亡，队伍继续前进...");
    }

    List<DropItemVO> drops = lootHelper.rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(isBoss ? 100 : 30, isBoss ? 500 : 150);

    for (Long memberId : memberIds) {
      lootHelper.giveDrops(memberId, drops, spiritStones);
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
        "战斗胜利！");
  }

  private String retreatCaptain(DungeonInstance instance) {
    instance.markAbandoned();
    instanceRepository.save(instance);

    List<Long> memberIds = getTeamMemberIds(instance);
    for (Long memberId : memberIds) {
      User memberUser = userStateService.loadUserForUpdate(memberId);
      memberUser.clearActivity();
      userStateService.saveActivity(memberUser);
    }

    log.info(
        "队长 {} 撤退，秘境 {} 结束，共 {} 人退出",
        instance.getLeaderId(),
        instance.getDungeonId(),
        memberIds.size());
    return "你带领队伍撤退了。所有成员已退出秘境，已获奖励保留。";
  }

  private String retreatMember(Long userId, DungeonInstance instance) {
    User member = userStateService.loadUserForUpdate(userId);
    member.clearActivity();
    userStateService.saveActivity(member);

    log.info("队员 {} 退出秘境 {}", userId, instance.getDungeonId());
    return "你退出了秘境。已获得的奖励保留。";
  }

  private String settleAllMembers(DungeonInstance instance) {
    instance.markCompleted();
    instanceRepository.save(instance);

    List<Long> memberIds = getTeamMemberIds(instance);
    StringBuilder sb = new StringBuilder();

    for (Long memberId : memberIds) {
      User member = userStateService.loadUser(memberId);
      member.clearActivity();
      userStateService.saveActivity(member);

      String result = progressHelper.completeDungeon(memberId, instance);
      if (memberIds.size() > 1) {
        sb.append("【").append(member.getNickname()).append("】: ").append(result).append("\n");
      } else {
        sb.append(result);
      }
    }

    log.info("秘境 {} 通关，{} 人结算", instance.getDungeonId(), memberIds.size());
    return sb.toString();
  }
}
