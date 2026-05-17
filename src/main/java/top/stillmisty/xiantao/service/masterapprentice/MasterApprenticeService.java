package top.stillmisty.xiantao.service.masterapprentice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.masterapprentice.entity.MasterApprentice;
import top.stillmisty.xiantao.domain.masterapprentice.enums.MasterApprenticeStatus;
import top.stillmisty.xiantao.domain.masterapprentice.repository.MasterApprenticeRepository;
import top.stillmisty.xiantao.domain.masterapprentice.vo.ApprenticeInfoVO;
import top.stillmisty.xiantao.domain.masterapprentice.vo.MasterApprenticeInfoVO;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.sect.SectMemberService;

@Slf4j
@Service
public class MasterApprenticeService {

  private static final int MAX_APPRENTICE_COUNT = 3;
  private static final int COOLDOWN_HOURS = 24;

  private final MasterApprenticeRepository masterApprenticeRepository;
  private final DaoProtectionRepository daoProtectionRepository;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  @Lazy private final SectMemberService sectMemberService;

  public MasterApprenticeService(
      MasterApprenticeRepository masterApprenticeRepository,
      DaoProtectionRepository daoProtectionRepository,
      UserRepository userRepository,
      UserStateService userStateService,
      @Lazy SectMemberService sectMemberService) {
    this.masterApprenticeRepository = masterApprenticeRepository;
    this.daoProtectionRepository = daoProtectionRepository;
    this.userRepository = userRepository;
    this.userStateService = userStateService;
    this.sectMemberService = sectMemberService;
  }

  // ===================== 公开 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<String> requestMentor(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(requestMentor(userId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> requestApprentice(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(requestApprentice(userId, targetNickname));
  }

  @Authenticated
  public ServiceResult<MasterApprenticeInfoVO> getStatus(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getStatus(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> dismissApprentice(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(dismissApprentice(userId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> renounceMaster(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(renounceMaster(userId));
  }

  // ===================== 内部 API =====================

  public String requestMentor(Long userId, String targetNickname) {
    User apprentice = userStateService.loadUser(userId);
    User master = userStateService.loadUserByNickname(targetNickname);
    if (master == null) {
      throw new BusinessException(ErrorCode.MASTER_NOT_FOUND);
    }
    if (master.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.MASTER_CANNOT_SELF);
    }
    if (masterApprenticeRepository
        .findByApprenticeId(userId)
        .map(MasterApprentice::isActive)
        .orElse(false)) {
      throw new BusinessException(ErrorCode.MASTER_ALREADY_HAS);
    }
    if (!isLevelGapSufficient(master.getLevel(), apprentice.getLevel())) {
      throw new BusinessException(ErrorCode.MASTER_LEVEL_INSUFFICIENT);
    }
    if (masterApprenticeRepository.countActiveByMasterId(master.getId()) >= MAX_APPRENTICE_COUNT) {
      throw new BusinessException(ErrorCode.MASTER_FULL, MAX_APPRENTICE_COUNT);
    }
    if (isOnCooldown(userId)) {
      throw new BusinessException(ErrorCode.MASTER_COOLDOWN, COOLDOWN_HOURS);
    }
    if (!sectMemberService.isInSameSect(userId, master.getId())) {
      throw new BusinessException(ErrorCode.MASTER_NOT_SAME_SECT);
    }

    establishMasterApprentice(master.getId(), userId);

    log.info("玩家 {} 拜师 {}", userId, master.getId());
    return "已拜【" + targetNickname + "】为师！护道关系已自动建立。";
  }

  public String requestApprentice(Long userId, String targetNickname) {
    User master = userStateService.loadUser(userId);
    User apprentice = userStateService.loadUserByNickname(targetNickname);
    if (apprentice == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }
    if (apprentice.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.MASTER_CANNOT_SELF);
    }
    if (masterApprenticeRepository
        .findByApprenticeId(apprentice.getId())
        .map(MasterApprentice::isActive)
        .orElse(false)) {
      throw new BusinessException(ErrorCode.MASTER_APPRENTICE_HAS_MASTER);
    }
    if (!isLevelGapSufficient(master.getLevel(), apprentice.getLevel())) {
      throw new BusinessException(ErrorCode.MASTER_LEVEL_INSUFFICIENT);
    }
    if (masterApprenticeRepository.countActiveByMasterId(userId) >= MAX_APPRENTICE_COUNT) {
      throw new BusinessException(ErrorCode.MASTER_FULL, MAX_APPRENTICE_COUNT);
    }
    if (isOnCooldown(apprentice.getId())) {
      throw new BusinessException(ErrorCode.MASTER_COOLDOWN, COOLDOWN_HOURS);
    }
    if (!sectMemberService.isInSameSect(userId, apprentice.getId())) {
      throw new BusinessException(ErrorCode.MASTER_NOT_SAME_SECT);
    }

    establishMasterApprentice(userId, apprentice.getId());

    log.info("玩家 {} 收徒 {}", userId, apprentice.getId());
    return "已收【" + targetNickname + "】为徒！护道关系已自动建立。";
  }

  public MasterApprenticeInfoVO getStatus(Long userId) {
    User user = userStateService.loadUser(userId);

    Optional<MasterApprentice> asApprenticeOpt =
        masterApprenticeRepository.findByApprenticeId(userId);
    List<MasterApprentice> asMasterList = masterApprenticeRepository.findByMasterId(userId);
    List<MasterApprentice> activeApprentices =
        asMasterList.stream().filter(MasterApprentice::isActive).toList();

    Long masterId = null;
    String masterName = null;
    Integer masterLevel = null;
    String masterRealmDisplay = null;
    Long relationshipId = null;
    String status = "散修";

    if (asApprenticeOpt.isPresent()) {
      MasterApprentice ma = asApprenticeOpt.get();
      if (ma.isActive()) {
        User master = userStateService.loadUser(ma.getMasterId());
        masterId = master.getId();
        masterName = master.getNickname();
        masterLevel = master.getLevel();
        masterRealmDisplay = CultivationRealm.realmDisplay(master.getLevel());
        relationshipId = ma.getId();
        status = "在师";
      } else {
        status = ma.getStatus().getName();
      }
    }

    List<ApprenticeInfoVO> apprentices = new ArrayList<>();
    for (MasterApprentice ma : activeApprentices) {
      User apprentice = userStateService.loadUser(ma.getApprenticeId());
      apprentices.add(
          new ApprenticeInfoVO(
              apprentice.getId(),
              apprentice.getNickname(),
              apprentice.getLevel(),
              CultivationRealm.realmDisplay(apprentice.getLevel()),
              ma.getStatus().getName()));
    }

    return new MasterApprenticeInfoVO(
        masterId != null,
        masterId,
        masterName,
        masterLevel,
        masterRealmDisplay,
        relationshipId,
        status,
        apprentices.size(),
        apprentices);
  }

  public String dismissApprentice(Long userId, String targetNickname) {
    User target = userStateService.loadUserByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    Optional<MasterApprentice> relationOpt =
        masterApprenticeRepository.findByApprenticeId(target.getId());
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      throw new BusinessException(ErrorCode.MASTER_APPRENTICE_NOT_FOUND);
    }

    MasterApprentice relation = relationOpt.get();
    if (!relation.getMasterId().equals(userId)) {
      throw new BusinessException(ErrorCode.MASTER_APPRENTICE_NOT_FOUND);
    }

    relation.setStatus(MasterApprenticeStatus.DISMISSED);
    relation.setCooldownUntil(LocalDateTime.now().plusHours(COOLDOWN_HOURS));
    masterApprenticeRepository.save(relation);

    clearDaoProtection(userId, target.getId());

    if (sectMemberService.isInSect(target.getId())) {
      sectMemberService.leaveSectInternal(target.getId());
    }

    log.info("玩家 {} 被师傅 {} 逐出师门", target.getId(), userId);
    return "已将【" + targetNickname + "】逐出师门。";
  }

  public String renounceMaster(Long userId) {
    Optional<MasterApprentice> relationOpt = masterApprenticeRepository.findByApprenticeId(userId);
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      throw new BusinessException(ErrorCode.MASTER_NO_MASTER);
    }

    MasterApprentice relation = relationOpt.get();
    relation.setStatus(MasterApprenticeStatus.RENEGED);
    relation.setCooldownUntil(LocalDateTime.now().plusHours(COOLDOWN_HOURS));
    masterApprenticeRepository.save(relation);

    clearDaoProtection(relation.getMasterId(), userId);

    if (sectMemberService.isInSect(userId)) {
      sectMemberService.leaveSectInternal(userId);
    }

    log.info("玩家 {} 叛师", userId);
    return "你已叛离师门，" + COOLDOWN_HOURS + " 小时内无法拜新师。";
  }

  // ===================== 内部工具方法 =====================

  /** 建立师徒关系（含宗门验证 + 护道同步） */
  @Transactional
  public void establishMasterApprentice(Long masterId, Long apprenticeId) {
    Optional<MasterApprentice> existing =
        masterApprenticeRepository.findByApprenticeId(apprenticeId);
    MasterApprentice relation;
    if (existing.isPresent()) {
      relation = existing.get();
      relation.setMasterId(masterId);
      relation.setStatus(MasterApprenticeStatus.ACTIVE);
      relation.setCooldownUntil(null);
      relation.setGraduatedAt(null);
    } else {
      relation = MasterApprentice.create(masterId, apprenticeId);
    }
    masterApprenticeRepository.save(relation);

    Optional<DaoProtection> existingProtection =
        daoProtectionRepository.findByProtectorAndProtege(masterId, apprenticeId);
    if (existingProtection.isEmpty()) {
      DaoProtection protection = DaoProtection.create(masterId, apprenticeId);
      daoProtectionRepository.save(protection);
    }
  }

  /** 计算修炼速度加成 */
  public double calculateTrainingBonus(Long userId) {
    Optional<MasterApprentice> relationOpt = masterApprenticeRepository.findByApprenticeId(userId);
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      return 1.0;
    }

    User master = userRepository.findById(relationOpt.get().getMasterId()).orElse(null);
    if (master == null) {
      return 1.0;
    }

    User apprentice = userRepository.findById(userId).orElse(null);
    if (apprentice == null) {
      return 1.0;
    }

    int levelDiff = master.getLevel() - apprentice.getLevel();
    double bonus = 1.0 + (levelDiff * 0.002);
    return Math.min(1.5, Math.max(1.0, bonus));
  }

  /** 检测并执行自动出师（由升级/突破时调用） */
  @Transactional
  public void checkAndGraduate(Long userId) {
    Optional<MasterApprentice> relationOpt = masterApprenticeRepository.findByApprenticeId(userId);
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      return;
    }

    MasterApprentice relation = relationOpt.get();
    User apprentice = userRepository.findById(userId).orElse(null);
    User master = userRepository.findById(relation.getMasterId()).orElse(null);
    if (apprentice == null || master == null) {
      return;
    }

    CultivationRealm apprenticeRealm = CultivationRealm.fromLevel(apprentice.getLevel());
    CultivationRealm masterRealm = CultivationRealm.fromLevel(master.getLevel());

    if (apprenticeRealm.ordinal() >= masterRealm.ordinal()) {
      graduate(userId, relation);
      log.info("玩家 {} 自动出师，达到师傅境界 {}", userId, apprenticeRealm.getRealmName());
    }
  }

  private void graduate(Long apprenticeId, MasterApprentice relation) {
    relation.setStatus(MasterApprenticeStatus.GRADUATED);
    relation.setGraduatedAt(LocalDateTime.now());
    masterApprenticeRepository.save(relation);

    clearDaoProtection(relation.getMasterId(), apprenticeId);
  }

  /** 师傅宗门变更时同步所有徒弟 */
  @Transactional
  public void syncSectForMaster(Long masterId, Long targetSectId) {
    List<MasterApprentice> relations = masterApprenticeRepository.findByMasterId(masterId);
    for (MasterApprentice relation : relations) {
      if (relation.isActive()) {
        sectMemberService.joinSectInternal(relation.getApprenticeId(), targetSectId);
        log.info("徒弟 {} 跟随师傅 {} 加入宗门 {}", relation.getApprenticeId(), masterId, targetSectId);
      }
    }
  }

  /** 师傅退出/被踢出宗门时处理所有徒弟叛师 */
  @Transactional
  public void handleMasterSectLeave(Long masterId) {
    List<MasterApprentice> relations = masterApprenticeRepository.findByMasterId(masterId);
    for (MasterApprentice relation : relations) {
      if (relation.isActive()) {
        relation.setStatus(MasterApprenticeStatus.RENEGED);
        masterApprenticeRepository.save(relation);
        clearDaoProtection(masterId, relation.getApprenticeId());
        if (sectMemberService.isInSect(relation.getApprenticeId())) {
          sectMemberService.leaveSectInternal(relation.getApprenticeId());
        }
        log.info("徒弟 {} 因师傅 {} 退出宗门而叛师", relation.getApprenticeId(), masterId);
      }
    }
  }

  // ===================== 私有工具方法 =====================

  private boolean isLevelGapSufficient(int higherLevel, int lowerLevel) {
    CultivationRealm higherRealm = CultivationRealm.fromLevel(higherLevel);
    CultivationRealm lowerRealm = CultivationRealm.fromLevel(lowerLevel);
    return higherRealm.ordinal() > lowerRealm.ordinal();
  }

  private boolean isOnCooldown(Long userId) {
    Optional<MasterApprentice> relation = masterApprenticeRepository.findByApprenticeId(userId);
    return relation
        .map(
            r -> {
              if (r.isActive()) return false;
              LocalDateTime cooldown = r.getCooldownUntil();
              return cooldown != null && cooldown.isAfter(LocalDateTime.now());
            })
        .orElse(false);
  }

  private void clearDaoProtection(Long protectorId, Long protegeId) {
    Optional<DaoProtection> protection =
        daoProtectionRepository.findByProtectorAndProtege(protectorId, protegeId);
    protection.ifPresent(p -> daoProtectionRepository.deleteById(p.getId()));
  }
}
