package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DungeonService {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonQueryService dungeonQueryService;
  private final DungeonAccessChecker accessChecker;
  private final UserStateService userStateService;
  private final DungeonInstanceManager instanceManager;
  private final DungeonStateBuilder stateBuilder;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public ServiceResult<List<top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO>> listDungeons(
      Long userId) {
    return dungeonQueryService.listDungeons(userId);
  }

  @Transactional
  @CacheEvict(cacheNames = "dungeon_list", key = "#userId")
  public ServiceResult<String> enterDungeon(Long userId, String dungeonName) {
    return new ServiceResult.Success<>(enterDungeonInternal(userId, dungeonName));
  }

  @Transactional(readOnly = true)
  public ServiceResult<String> statusInDungeon(Long userId) {
    return new ServiceResult.Success<>(getStatusInternal(userId));
  }

  public String enterDungeonInternal(Long userId, String dungeonName) {
    User user = userStateService.loadUser(userId);
    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findByName(dungeonName)
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, dungeonName));

    if (!dungeon.getIsActive()) {
      throw new BusinessException(ErrorCode.DUNGEON_NOT_ACTIVE, dungeonName);
    }

    checkIdleStatus(user);
    accessChecker.checkAccess(user, dungeon);

    DungeonInstance existing =
        instanceRepository
            .findByLeaderIdAndDungeonIdAndStatus(userId, dungeon.getId(), DungeonStatus.ACTIVE)
            .orElse(null);
    if (existing != null) {
      throw new BusinessException(ErrorCode.DUNGEON_ALREADY_IN, dungeonName);
    }

    DungeonInstance instance = new DungeonInstance();
    instance.setDungeonId(dungeon.getId());
    instance.setLeaderId(userId);
    instance.setCurrentAreaKey(dungeon.getAreaConfigs().get(0).key());
    instance.setPassageUnlocked(false);
    instance.setExploredPois(new ArrayList<>());
    instance.setStatus(DungeonStatus.ACTIVE);
    instance.setExpiresAt(TimeUtil.now().plusHours(dungeon.getTimeoutHours()));
    instanceRepository.save(instance);

    user.setStatus(UserStatus.DUNGEON);
    user.setActivityType(ActivityType.DUNGEON);
    user.setActivityStartTime(TimeUtil.now());
    user.setActivityTargetId(instance.getId());
    userStateService.saveActivity(user);

    log.info("玩家 {} 进入了秘境 {}", userId, dungeonName);

    var area = dungeon.getAreaConfigs().get(0);
    StringBuilder sb = new StringBuilder();
    sb.append("你踏入了【").append(dungeon.getName()).append("】—").append(area.name()).append("\n");
    sb.append(area.description()).append("\n");

    if (dungeon.hasSpirit()) {
      var sc = dungeon.getSpiritConfig();
      if (sc != null) {
        sb.append(sc.spiritAppearance()).append("\n\n");
        if (sc.greeting() != null) {
          sb.append("「").append(sc.spiritName()).append("」: ").append(sc.greeting());
        }
      }
    } else {
      sb.append("\n可探索的地点：");
      for (var poi : area.mainPois()) {
        sb.append("\n  · ").append(poi.name()).append(" [").append(poi.type()).append("]");
      }
      sb.append("\n\n输入「秘灵 内容」与秘境之灵/叙事者对话");
    }

    return sb.toString();
  }

  public String getStatusInternal(Long userId) {
    User user = userStateService.loadUser(userId);
    if (user.getActivityTargetId() == null || user.getStatus() != UserStatus.DUNGEON) {
      return "你当前不在任何秘境中。输入「秘境」查看可进入的秘境。";
    }

    DungeonInstance instance =
        instanceRepository
            .findById(user.getActivityTargetId())
            .filter(DungeonInstance::isActive)
            .orElse(null);
    if (instance == null) {
      return "你当前不在任何秘境中。";
    }

    DungeonTemplate dungeon =
        dungeonTemplateRepository.findById(instance.getDungeonId()).orElse(null);
    if (dungeon == null) {
      return "秘境数据异常。";
    }

    var area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
    String areaName = area != null ? area.name() : instance.getCurrentAreaKey();
    int totalMain = area != null ? area.mainPois().size() : 0;
    int explored = instance.exploredCount();
    long elapsedMinutes =
        java.time.Duration.between(instance.getCreatedAt(), TimeUtil.now()).toMinutes();

    return areaName
        + " | 探索 "
        + explored
        + "/"
        + totalMain
        + " | 用时 "
        + elapsedMinutes
        + "min"
        + "\n输入「秘灵 内容」探索秘境";
  }

  private void checkIdleStatus(User user) {
    if (user.getStatus() != UserStatus.IDLE) {
      throw new BusinessException(
          ErrorCode.DUNGEON_STATUS_BLOCKED,
          user.getStatus() != null ? user.getStatus().getName() : "未知");
    }
  }
}
