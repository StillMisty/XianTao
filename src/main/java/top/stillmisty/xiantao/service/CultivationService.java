package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
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
  private final ChatClient chatClient;

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
          CultivationRealm.realmDisplay(user.getLevel()),
          false,
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
    int newLevel = oldLevel + 1;
    boolean isMajor = CultivationRealm.isMajorBreakthrough(oldLevel, newLevel);

    user.setLevel(newLevel);
    user.setExp(user.getExp() - expNeeded);
    user.setBreakthroughFailCount(0);
    user.setHpCurrent(user.calculateMaxHp());

    daoProtectionService.clearProtegeRelations(userId);
    playerBuffRepository.deleteByUserIdAndType(userId, PlayerBuffType.BREAKTHROUGH);

    String message;
    if (isMajor) {
      CultivationRealm newRealm = CultivationRealm.fromLevel(newLevel);
      applyMajorBreakthroughBonuses(user);
      String llmMessage = generateBreakthroughMessage(newRealm, user.getNickname());
      message =
          "*** "
              + llmMessage
              + " ***\n"
              + "全属性 +"
              + CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT
              + "%"
              + " | 灵石 +"
              + CultivationRealm.breakthroughSpiritStonesReward(newRealm);
    } else {
      message = "恭喜！突破成功！";
    }

    userStateService.save(user);

    return new BreakthroughResult(
        true,
        message,
        finalSuccessRate,
        newLevel,
        CultivationRealm.realmDisplay(newLevel),
        isMajor,
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
        CultivationRealm.realmDisplay(user.getLevel()),
        false,
        user.getBreakthroughFailCount(),
        user.calculateBreakthroughSuccessRate(),
        null);
  }

  /** 跨大境界突破时应用属性加成和灵石奖励 */
  private void applyMajorBreakthroughBonuses(User user) {
    int bonusStr =
        user.getEffectiveStatStr() * CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT / 100;
    int bonusCon =
        user.getEffectiveStatCon() * CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT / 100;
    int bonusAgi =
        user.getEffectiveStatAgi() * CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT / 100;
    int bonusWis =
        user.getEffectiveStatWis() * CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT / 100;

    user.addStatStr(bonusStr);
    user.addStatCon(bonusCon);
    user.addStatAgi(bonusAgi);
    user.addStatWis(bonusWis);

    CultivationRealm realm = CultivationRealm.fromLevel(user.getLevel());
    long spiritStones = CultivationRealm.breakthroughSpiritStonesReward(realm);
    user.setSpiritStones(user.getSpiritStones() + spiritStones);
  }

  /** 使用 LLM 生成跨大境界突破贺词 */
  private String generateBreakthroughMessage(CultivationRealm realm, String nickname) {
    try {
      String prompt =
          """
              你是一位仙侠世界的说书人，擅长渲染境界突破的宏大场面。
              现在有一位修士突破了境界桎梏，请撰写一段突破贺词。

              【修士信息】
              道号：%s
              新境界：%s

              【新境界介绍】
              %s

              【写作要求】
              字数 40-80 字，仙侠古风，气势磅礴。
              描写突破时的天地异象、修士感悟或命运转折。
              结合该境界的独特意境，让玩家感受到实力暴涨的爽感。
              只需要输出贺词正文，不要任何前缀、后缀或解释。"""
              .formatted(nickname, realm.getRealmName(), realm.getDescription());

      String result = chatClient.prompt().user(prompt).call().content();

      if (result != null && !result.isBlank()) {
        return result.strip();
      }
    } catch (Exception e) {
      log.warn("LLM生成突破贺词失败，使用默认文案", e);
    }
    return realm.getBreakthroughMessage();
  }
}
