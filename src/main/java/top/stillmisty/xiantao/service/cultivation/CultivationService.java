package top.stillmisty.xiantao.service.cultivation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.TribulationType;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.service.ProtectionHelper;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.combat.TribulationCombatExecutor;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 修仙核心服务 处理突破等核心修仙机制 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CultivationService {

  private final UserStateService userStateService;
  private final PlayerBuffRepository playerBuffRepository;
  private final ProtectionHelper protectionHelper;
  private final DaoProtectionService daoProtectionService;
  private final SpiritStoneService spiritStoneService;
  private final MasterApprenticeService masterApprenticeService;
  private final TribulationNarrativeGenerator narrativeGenerator;
  private final TribulationCombatExecutor tribulationCombatExecutor;

  // ===================== 公开 API =====================

  public ServiceResult<BreakthroughResult> attemptBreakthrough(Long userId) {
    return new ServiceResult.Success<>(attemptBreakthroughInternal(userId));
  }

  // ===================== 内部 API =====================

  /**
   * 突破境界
   *
   * @param userId 用户ID
   * @return 突破结果
   */
  public BreakthroughResult attemptBreakthroughInternal(Long userId) {
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

    int newLevel = user.getLevel() + 1;
    CultivationRealm newRealm = CultivationRealm.fromLevel(newLevel);
    boolean isMajor = CultivationRealm.isMajorBreakthrough(user.getLevel(), newLevel);
    boolean isTribulationRealm = newRealm == CultivationRealm.TRIBULATION;

    if (isMajor || isTribulationRealm) {
      return combatBreakthrough(user, newLevel, newRealm, isMajor, isTribulationRealm, expNeeded);
    }

    // 小境界突破：原有 RNG 逻辑
    double finalSuccessRate = calculateFinalBreakthroughRate(user);
    boolean breakthroughSuccess = Math.random() * 100 < finalSuccessRate;

    if (breakthroughSuccess) {
      return handleBreakthroughSuccess(userId, user, expNeeded, finalSuccessRate);
    } else {
      return handleBreakthroughFailure(userId, user, expNeeded, finalSuccessRate);
    }
  }

  // ===================== 战斗突破（跨大境界 + 渡劫期） =====================

  private BreakthroughResult combatBreakthrough(
      User user,
      int newLevel,
      CultivationRealm newRealm,
      boolean isMajor,
      boolean isTribulationRealm,
      long expNeeded) {
    int oldLevel = user.getLevel();
    int targetRealmOrdinal = newRealm.ordinal();
    int tribulationLevel = isTribulationRealm ? newLevel - newRealm.getStartLevel() + 1 : 0;

    // 读取丹药+护道加成，转为 Boss 削弱
    double protectionBonus = protectionHelper.calculateProtectionBonus(user);
    List<PlayerBuff> breakthroughBuffs =
        playerBuffRepository.findActiveByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);
    double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();
    double bossReduction = Math.min(0.5, (pillBonus + protectionBonus) / 100.0);

    // 保底削弱
    double pityReduction = Math.min(0.5, user.getBreakthroughFailCount() * 0.05);

    // 雷劫抗性 buff
    double tribulationResist = 0;
    List<PlayerBuff> resistBuffs =
        playerBuffRepository.findActiveByUserIdAndType(
            user.getId(), PlayerBuffType.TRIBULATION_RESIST);
    if (!resistBuffs.isEmpty()) {
      tribulationResist =
          Math.min(0.9, resistBuffs.stream().mapToInt(PlayerBuff::getValue).sum() / 100.0);
    }

    // 随机选择雷劫类型
    TribulationType tribulationType =
        TribulationType.randomForBreakthrough(targetRealmOrdinal, isTribulationRealm);

    // 构建队伍
    CombatTeam defendingTeam = tribulationCombatExecutor.buildTeamOrReturnNull(user);
    if (defendingTeam == null) {
      return new BreakthroughResult(
          false,
          "⚠️ 没有可出战的单位，雷劫无法降临",
          null,
          user.getLevel(),
          CultivationRealm.realmDisplay(user.getLevel()),
          isMajor,
          user.getBreakthroughFailCount(),
          null,
          null,
          null,
          tribulationType.getDisplayName());
    }

    // 扣除修为（无论胜败）
    user.setExp(user.getExp() - expNeeded);

    CombatService.TeamStats teamStats = tribulationCombatExecutor.calculateTeamStats(defendingTeam);

    TribulationBoss boss =
        TribulationBoss.forPlayerBreakthrough(
            teamStats.totalMaxHp(),
            teamStats.avgAttack(),
            teamStats.avgDef(),
            teamStats.avgSpeed(),
            targetRealmOrdinal,
            tribulationType,
            bossReduction,
            pityReduction,
            tribulationResist,
            tribulationLevel);

    var battleResult = tribulationCombatExecutor.execute(defendingTeam, boss);

    userStateService.saveHpStatus(user);

    // 清除 buff 和护道关系
    daoProtectionService.clearProtegeRelations(user.getId());
    playerBuffRepository.deleteByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);

    if (battleResult.playerWon()) {
      return handleCombatBreakthroughSuccess(
          user,
          newLevel,
          newRealm,
          isMajor,
          isTribulationRealm,
          tribulationType,
          battleResult.battleResult());
    } else {
      return handleCombatBreakthroughFailure(
          user, oldLevel, isMajor, tribulationType, battleResult.battleResult());
    }
  }

  private BreakthroughResult handleCombatBreakthroughSuccess(
      User user,
      int newLevel,
      CultivationRealm newRealm,
      boolean isMajor,
      boolean isTribulationRealm,
      TribulationType tribulationType,
      BattleResultVO result) {
    user.setLevel(newLevel);
    user.setBreakthroughFailCount(0);
    user.setHpCurrent(user.calculateMaxHp());

    if (isMajor) {
      applyMajorBreakthroughBonuses(user);
    } else if (isTribulationRealm) {
      // 渡劫期每级 +5% 全属性
      int bonusStr = user.getEffectiveStatStr() * 5 / 100;
      int bonusCon = user.getEffectiveStatCon() * 5 / 100;
      int bonusAgi = user.getEffectiveStatAgi() * 5 / 100;
      int bonusWis = user.getEffectiveStatWis() * 5 / 100;
      user.addStatStr(bonusStr);
      user.addStatCon(bonusCon);
      user.addStatAgi(bonusAgi);
      user.addStatWis(bonusWis);
    }

    userStateService.save(user);
    masterApprenticeService.checkAndGraduate(user.getId());

    String narrative =
        narrativeGenerator.generateCombatNarrative(
            tribulationType, user.getNickname(), result, true);

    String message;
    if (isMajor) {
      message =
          narrative
              + "\n\n"
              + "全属性 +"
              + CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT
              + "%"
              + " | 灵石 +"
              + CultivationRealm.breakthroughSpiritStonesReward(newRealm);
    } else {
      message = narrative + "\n\n" + "全属性 +5%";
    }

    return new BreakthroughResult(
        true,
        message,
        null,
        newLevel,
        CultivationRealm.realmDisplay(newLevel),
        isMajor,
        0,
        null,
        null,
        result,
        tribulationType.getDisplayName());
  }

  private BreakthroughResult handleCombatBreakthroughFailure(
      User user,
      int oldLevel,
      boolean isMajor,
      TribulationType tribulationType,
      BattleResultVO result) {
    user.setBreakthroughFailCount(user.getBreakthroughFailCount() + 1);
    userStateService.save(user);

    String narrative =
        narrativeGenerator.generateCombatNarrative(
            tribulationType, user.getNickname(), result, false);

    return new BreakthroughResult(
        false,
        narrative,
        null,
        oldLevel,
        CultivationRealm.realmDisplay(oldLevel),
        isMajor,
        user.getBreakthroughFailCount(),
        null,
        null,
        result,
        tribulationType.getDisplayName());
  }

  // ===================== 原有 RNG 突破逻辑 =====================

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

    if (isMajor) {
      applyMajorBreakthroughBonuses(user);
    }

    userStateService.save(user);

    masterApprenticeService.checkAndGraduate(userId);

    String message;
    if (isMajor) {
      CultivationRealm newRealm = CultivationRealm.fromLevel(newLevel);
      String llmMessage =
          narrativeGenerator.generateBreakthroughMessage(newRealm, user.getNickname());
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
    spiritStoneService.deposit(user.getId(), spiritStones);
  }
}
