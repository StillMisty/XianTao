package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 修仙核心服务 处理突破等核心修仙机制 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CultivationService {

  private final UserStateService userStateService;
  private final PlayerBuffRepository playerBuffRepository;
  private final ProtectionHelper protectionHelper;
  private final DaoProtectionService daoProtectionService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<BreakthroughResult> attemptBreakthrough(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(attemptBreakthrough(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<DaoProtectionResult> establishProtection(
      PlatformType platform, String openId, String protegeNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(
        daoProtectionService.establishProtection(userId, protegeNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<DaoProtectionResult> removeProtection(
      PlatformType platform, String openId, String protegeNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(
        daoProtectionService.removeProtection(userId, protegeNickname));
  }

  @Authenticated
  public ServiceResult<DaoProtectionQueryResult> queryProtectionInfo(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(daoProtectionService.queryProtectionInfo(userId));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  public DaoProtectionResult establishProtection(Long userId, String protegeNickname) {
    return daoProtectionService.establishProtection(userId, protegeNickname);
  }

  public DaoProtectionResult removeProtection(Long userId, String protegeNickname) {
    return daoProtectionService.removeProtection(userId, protegeNickname);
  }

  public DaoProtectionQueryResult queryProtectionInfo(Long userId) {
    return daoProtectionService.queryProtectionInfo(userId);
  }

  /**
   * 突破境界
   *
   * @param userId 用户ID
   * @return 突破结果
   */
  @Transactional
  public BreakthroughResult attemptBreakthrough(Long userId) {
    User user = userStateService.loadUser(userId);

    long expNeeded = user.calculateExpToNextLevel();
    if (user.getExp() < expNeeded) {
      return new BreakthroughResult(
          false,
          String.format("修为不足，突破需要 %d 修为，当前仅有 %d 修为", expNeeded, user.getExp()),
          user.calculateBreakthroughSuccessRate(),
          user.getLevel(),
          user.getBreakthroughFailCount(),
          user.calculateBreakthroughSuccessRate(),
          null);
    }

    double finalSuccessRate = calculateFinalBreakthroughRate(user);
    boolean breakthroughSuccess = Math.random() * 100 < finalSuccessRate;

    if (breakthroughSuccess) {
      return handleBreakthroughSuccess(userId, user, expNeeded, finalSuccessRate);
    } else {
      return handleBreakthroughFailure(userId, user, expNeeded, finalSuccessRate);
    }
  }

  private double calculateFinalBreakthroughRate(User user) {
    double protectionBonus = protectionHelper.calculateProtectionBonus(user);
    List<PlayerBuff> breakthroughBuffs =
        playerBuffRepository.findActiveByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);
    double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();
    double baseSuccessRate = user.calculateBreakthroughSuccessRate();
    return Math.min(100.0, baseSuccessRate + protectionBonus + pillBonus);
  }

  private BreakthroughResult handleBreakthroughSuccess(
      Long userId, User user, long expNeeded, double finalSuccessRate) {
    int oldLevel = user.getLevel();
    user.setLevel(oldLevel + 1);
    user.setExp(user.getExp() - expNeeded);
    user.setBreakthroughFailCount(0);
    user.setHpCurrent(user.calculateMaxHp());

    daoProtectionService.clearProtegeRelations(userId);
    playerBuffRepository.deleteByUserIdAndType(userId, PlayerBuffType.BREAKTHROUGH);

    userStateService.save(user);

    return new BreakthroughResult(
        true,
        String.format("恭喜！突破成功！晋升至第%d层！", user.getLevel()),
        finalSuccessRate,
        user.getLevel(),
        0,
        user.calculateBreakthroughSuccessRate(),
        null);
  }

  private BreakthroughResult handleBreakthroughFailure(
      Long userId, User user, long expNeeded, double finalSuccessRate) {
    long newExp = Math.max(0, user.getExp() - expNeeded);
    user.setExp(newExp);
    user.setBreakthroughFailCount(user.getBreakthroughFailCount() + 1);

    daoProtectionService.clearProtegeRelations(userId);
    playerBuffRepository.deleteByUserIdAndType(userId, PlayerBuffType.BREAKTHROUGH);

    userStateService.save(user);

    return new BreakthroughResult(
        false,
        String.format("突破失败！道基反噬，损失 %d 修为，当前修为 %d", expNeeded, newExp),
        finalSuccessRate,
        user.getLevel(),
        user.getBreakthroughFailCount(),
        user.calculateBreakthroughSuccessRate(),
        null);
  }
}
