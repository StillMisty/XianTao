package top.stillmisty.xiantao.service.masterapprentice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.masterapprentice.entity.MasterApprentice;
import top.stillmisty.xiantao.domain.masterapprentice.enums.MasterApprenticeStatus;
import top.stillmisty.xiantao.domain.masterapprentice.vo.ApprenticeInfoVO;
import top.stillmisty.xiantao.domain.masterapprentice.vo.MasterApprenticeInfoVO;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.infrastructure.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.infrastructure.repository.MasterApprenticeRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Slf4j
@Service
public class MasterApprenticeService {

  private static final int MAX_APPRENTICE_COUNT = 3;
  private static final int COOLDOWN_HOURS = 24;

  private final MasterApprenticeRepository masterApprenticeRepository;
  private final DaoProtectionRepository daoProtectionRepository;
  private final UserRepository userRepository;
  private final PlayerLoader playerLoader;

  public MasterApprenticeService(
      MasterApprenticeRepository masterApprenticeRepository,
      DaoProtectionRepository daoProtectionRepository,
      UserRepository userRepository,
      PlayerLoader playerLoader) {
    this.masterApprenticeRepository = masterApprenticeRepository;
    this.daoProtectionRepository = daoProtectionRepository;
    this.userRepository = userRepository;
    this.playerLoader = playerLoader;
  }

  // ===================== 公开 API =====================

  @Transactional
  public ServiceResult<String> requestMentor(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(requestMentorInternal(userId, targetNickname));
  }

  @Transactional
  public ServiceResult<String> requestApprentice(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(requestApprenticeInternal(userId, targetNickname));
  }

  public ServiceResult<MasterApprenticeInfoVO> getStatus(Long userId) {
    return new ServiceResult.Success<>(getStatusInternal(userId));
  }

  @Transactional
  public ServiceResult<String> dismissApprentice(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(dismissApprenticeInternal(userId, targetNickname));
  }

  @Transactional
  public ServiceResult<String> renounceMaster(Long userId) {
    return new ServiceResult.Success<>(renounceMasterInternal(userId));
  }

  // ===================== 内部 API =====================

  public String requestMentorInternal(Long userId, String targetNickname) {
    Player apprentice = playerLoader.load(userId);
    Player master = playerLoader.findByNickname(targetNickname);
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
    if (isLevelGapSufficient(master.getLevel(), apprentice.getLevel())) {
      throw new BusinessException(ErrorCode.MASTER_LEVEL_INSUFFICIENT);
    }
    if (masterApprenticeRepository.countActiveByMasterId(master.getId()) >= MAX_APPRENTICE_COUNT) {
      throw new BusinessException(ErrorCode.MASTER_FULL, MAX_APPRENTICE_COUNT);
    }
    if (isOnCooldown(userId)) {
      throw new BusinessException(ErrorCode.MASTER_COOLDOWN, COOLDOWN_HOURS);
    }

    establishMasterApprentice(master.getId(), userId);

    log.info("玩家 {} 拜师 {}", userId, master.getId());
    return "已拜【" + targetNickname + "】为师！护道关系已自动建立。";
  }

  public String requestApprenticeInternal(Long userId, String targetNickname) {
    Player master = playerLoader.load(userId);
    Player apprentice = playerLoader.findByNickname(targetNickname);
    if (apprentice == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }
    if (apprentice.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.MASTER_APPRENTICE_CANNOT_SELF);
    }
    if (masterApprenticeRepository
        .findByApprenticeId(apprentice.getId())
        .map(MasterApprentice::isActive)
        .orElse(false)) {
      throw new BusinessException(ErrorCode.MASTER_APPRENTICE_HAS_MASTER);
    }
    if (isLevelGapSufficient(master.getLevel(), apprentice.getLevel())) {
      throw new BusinessException(ErrorCode.MASTER_LEVEL_INSUFFICIENT);
    }
    if (masterApprenticeRepository.countActiveByMasterId(userId) >= MAX_APPRENTICE_COUNT) {
      throw new BusinessException(ErrorCode.MASTER_FULL, MAX_APPRENTICE_COUNT);
    }
    if (isOnCooldown(apprentice.getId())) {
      throw new BusinessException(ErrorCode.MASTER_COOLDOWN, COOLDOWN_HOURS);
    }

    establishMasterApprentice(userId, apprentice.getId());

    log.info("玩家 {} 收徒 {}", userId, apprentice.getId());
    return "已收【" + targetNickname + "】为徒！护道关系已自动建立。";
  }

  public MasterApprenticeInfoVO getStatusInternal(Long userId) {
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
        Player master = playerLoader.load(ma.getMasterId());
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

    // 展示路径批量读取，无需行锁
    var apprenticeIds =
        activeApprentices.stream().map(MasterApprentice::getApprenticeId).distinct().toList();
    Map<Long, Player> apprenticeMap =
        userRepository.findByIds(apprenticeIds).stream()
            .collect(java.util.stream.Collectors.toMap(Player::getId, u -> u));

    List<ApprenticeInfoVO> apprentices = new ArrayList<>();
    for (MasterApprentice ma : activeApprentices) {
      Player apprentice = apprenticeMap.get(ma.getApprenticeId());
      if (apprentice == null) continue;
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

  @CacheEvict(cacheNames = "dao_protection", key = "#userId")
  public String dismissApprenticeInternal(Long userId, String targetNickname) {
    Player target = playerLoader.findByNickname(targetNickname);
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
    relation.setCooldownUntil(TimeUtil.now().plusHours(COOLDOWN_HOURS));
    masterApprenticeRepository.save(relation);

    clearDaoProtection(userId, target.getId());

    log.info("玩家 {} 被师傅 {} 逐出师门", target.getId(), userId);
    return "已将【" + targetNickname + "】逐出师门。";
  }

  @CacheEvict(cacheNames = "dao_protection", key = "#userId")
  public String renounceMasterInternal(Long userId) {
    Optional<MasterApprentice> relationOpt = masterApprenticeRepository.findByApprenticeId(userId);
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      throw new BusinessException(ErrorCode.MASTER_NO_MASTER);
    }

    MasterApprentice relation = relationOpt.get();
    relation.setStatus(MasterApprenticeStatus.RENEGED);
    relation.setCooldownUntil(TimeUtil.now().plusHours(COOLDOWN_HOURS));
    masterApprenticeRepository.save(relation);

    clearDaoProtection(relation.getMasterId(), userId);

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

    Player master = userRepository.findById(relationOpt.get().getMasterId()).orElse(null);
    if (master == null) {
      return 1.0;
    }

    Player apprentice = userRepository.findById(userId).orElse(null);
    if (apprentice == null) {
      return 1.0;
    }

    int levelDiff = master.getLevel() - apprentice.getLevel();
    double bonus = 1.0 + (levelDiff * 0.002);
    return Math.clamp(bonus, 1.0, 1.5);
  }

  /** 检测并执行自动出师（由升级/突破时调用） */
  @Transactional
  @CacheEvict(cacheNames = "dao_protection", key = "#userId")
  public void checkAndGraduate(Long userId) {
    Optional<MasterApprentice> relationOpt = masterApprenticeRepository.findByApprenticeId(userId);
    if (relationOpt.isEmpty() || !relationOpt.get().isActive()) {
      return;
    }

    MasterApprentice relation = relationOpt.get();
    Player apprentice = userRepository.findById(userId).orElse(null);
    Player master = userRepository.findById(relation.getMasterId()).orElse(null);
    if (apprentice == null || master == null) {
      return;
    }

    CultivationRealm apprenticeRealm = CultivationRealm.fromLevel(apprentice.getLevel());
    CultivationRealm masterRealm = CultivationRealm.fromLevel(master.getLevel());

    if (apprenticeRealm.getRank() >= masterRealm.getRank()) {
      graduate(userId, relation);
      log.info("玩家 {} 自动出师，达到师傅境界 {}", userId, apprenticeRealm.getRealmName());
    }
  }

  private void graduate(Long apprenticeId, MasterApprentice relation) {
    relation.setStatus(MasterApprenticeStatus.GRADUATED);
    relation.setGraduatedAt(TimeUtil.now());
    masterApprenticeRepository.save(relation);

    clearDaoProtection(relation.getMasterId(), apprenticeId);
  }

  // ===================== 私有工具方法 =====================

  private boolean isLevelGapSufficient(int higherLevel, int lowerLevel) {
    CultivationRealm higherRealm = CultivationRealm.fromLevel(higherLevel);
    CultivationRealm lowerRealm = CultivationRealm.fromLevel(lowerLevel);
    return higherRealm.getRank() <= lowerRealm.getRank();
  }

  private boolean isOnCooldown(Long userId) {
    Optional<MasterApprentice> relation = masterApprenticeRepository.findByApprenticeId(userId);
    return relation
        .map(
            r -> {
              if (r.isActive()) return false;
              LocalDateTime cooldown = r.getCooldownUntil();
              return (cooldown != null && cooldown.isAfter(TimeUtil.now()));
            })
        .orElse(false);
  }

  private void clearDaoProtection(Long protectorId, Long protegeId) {
    Optional<DaoProtection> protection =
        daoProtectionRepository.findByProtectorAndProtege(protectorId, protegeId);
    protection.ifPresent(p -> daoProtectionRepository.deleteById(p.getId()));
  }
}
