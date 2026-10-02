package top.stillmisty.xiantao.service.cultivation;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.TribulationType;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.service.ProtectionHelper;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.combat.PostCombatProcessor;
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
  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;

  // ===================== 公开 API =====================

  public ServiceResult<BreakthroughResult> attemptBreakthrough(Long userId) {
    return new ServiceResult.Success<>(attemptBreakthroughInternal(userId));
  }

  /** 大境界雷劫预报 修为足以尝试跨大境界/渡劫期突破时返回情报面板，否则返回 null。仅列候选天劫与削助手段， 不透露概率数值——天数难测，唯备战可恃。 */
  @Nullable
  public TribulationForecast buildTribulationForecast(Player user) {
    long expNeeded = user.calculateExpToNextLevel();
    if (user.getExp() < expNeeded) {
      return null;
    }
    int newLevel = user.getLevel() + 1;
    CultivationRealm newRealm = CultivationRealm.fromLevel(newLevel);
    boolean isMajor = CultivationRealm.isMajorBreakthrough(user.getLevel(), newLevel);
    boolean isTribulationRealm = newRealm == CultivationRealm.TRIBULATION;
    if (!isMajor && !isTribulationRealm) {
      return null;
    }

    List<String> tribulationNames =
        Arrays.stream(TribulationType.values())
            .filter(t -> isTribulationRealm || t.getMinRealmOrdinal() <= newRealm.getRank())
            .map(TribulationType::getDisplayName)
            .toList();

    double protectionBonus = protectionHelper.calculateProtectionBonus(user);
    List<PlayerBuff> breakthroughBuffs =
        playerBuffRepository.findActiveByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);
    double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();

    double resistPercent = 0;
    List<PlayerBuff> resistBuffs =
        playerBuffRepository.findActiveByUserIdAndType(
            user.getId(), PlayerBuffType.TRIBULATION_RESIST);
    if (!resistBuffs.isEmpty()) {
      resistPercent = Math.min(90, resistBuffs.stream().mapToInt(PlayerBuff::getValue).sum());
    }

    return new TribulationForecast(
        newRealm.getRealmName(),
        tribulationNames,
        pillBonus,
        protectionBonus,
        resistPercent,
        user.getBreakthroughFailCount());
  }

  // ===================== 内部 API =====================

  /**
   * 突破境界
   *
   * @param userId 用户ID
   * @return 突破结果
   */
  public BreakthroughResult attemptBreakthroughInternal(Long userId) {
    Player user = userStateService.loadUser(userId);

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
      Player user,
      int newLevel,
      CultivationRealm newRealm,
      boolean isMajor,
      boolean isTribulationRealm,
      long expNeeded) {
    int oldLevel = user.getLevel();
    int targetRealmOrdinal = newRealm.getRank();
    int tribulationLevel = isTribulationRealm ? newLevel - newRealm.getStartLevel() + 1 : 0;

    // 读取丹药+护道加成，转为 Boss 削弱
    double protectionBonus = protectionHelper.calculateProtectionBonus(user);
    List<PlayerBuff> breakthroughBuffs =
        playerBuffRepository.findActiveByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);
    double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();
    // 下限 0：护道/丹药加成为负时不得反向增强雷劫
    double bossReduction = Math.clamp((pillBonus + protectionBonus) / 100.0, 0.0, 0.5);

    // 保底削弱
    double pityReduction = Math.min(0.5, user.getBreakthroughFailCount() * 0.05);

    // 雷劫抗性 buff（招雷散等负值为刻意设计：雷劫增强，但渡过可获得额外修为补偿）
    int resistSum =
        playerBuffRepository
            .findActiveByUserIdAndType(user.getId(), PlayerBuffType.TRIBULATION_RESIST)
            .stream()
            .mapToInt(PlayerBuff::getValue)
            .sum();
    double tribulationResist = 0;
    if (resistSum != 0) {
      tribulationResist = Math.min(0.9, resistSum / 100.0);
    }
    boolean thunderLureActive = resistSum < 0;

    // 随机选择雷劫类型
    TribulationType tribulationType =
        TribulationType.randomForBreakthrough(targetRealmOrdinal, isTribulationRealm);

    // 构建队伍
    CombatTeam defendingTeam = combatService.buildPlayerTeam(user);
    if (defendingTeam.aliveMembers().isEmpty()) {
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

    CombatService.TeamStats teamStats = combatService.calculateTeamStats(defendingTeam);

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

    // 执行渡劫战斗
    CombatTeam bossTeam = new CombatTeam(0L, "天劫");
    bossTeam.addMember(boss);
    BattleResultVO battleResult = combatService.simulate(defendingTeam, bossTeam, 40);
    boolean playerWon = "Player".equals(battleResult.winner());

    // 战后气血写回：战败写回残血/濒死（胜利按设计回满，在成功结算中处理）；
    // 灵兽按战斗剩余气血写回，阵亡者卸下出战并进入休养，与历练战斗一致
    if (!playerWon) {
      postCombatProcessor.applyHpToUser(user, defendingTeam);
    }
    postCombatProcessor.applyCombatHpToBeasts(defendingTeam, user, playerWon);

    // 清除 buff 和护道关系
    daoProtectionService.clearProtegeRelations(user.getId());
    playerBuffRepository.deleteByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);

    if (playerWon) {
      return handleCombatBreakthroughSuccess(
          user,
          newLevel,
          newRealm,
          isMajor,
          isTribulationRealm,
          tribulationType,
          battleResult,
          expNeeded,
          thunderLureActive);
    } else {
      return handleCombatBreakthroughFailure(
          user, oldLevel, isMajor, tribulationType, battleResult);
    }
  }

  private BreakthroughResult handleCombatBreakthroughSuccess(
      Player user,
      int newLevel,
      CultivationRealm newRealm,
      boolean isMajor,
      boolean isTribulationRealm,
      TribulationType tribulationType,
      BattleResultVO result,
      long expNeeded,
      boolean thunderLureActive) {
    user.setLevel(newLevel);
    user.setBreakthroughFailCount(0);
    user.setHpCurrent(user.calculateMaxHp());

    // 招雷散补偿：负抗性令雷劫更强，渡过则回馈本次突破消耗修为的 50%（经存储上限截断）
    long thunderLureExp = 0;
    if (thunderLureActive) {
      long beforeExp = user.getExp();
      user.addExp(expNeeded / 2);
      thunderLureExp = user.getExp() - beforeExp;
    }

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

    String thunderLureText = thunderLureExp > 0 ? " | 招雷淬体：修为 +" + thunderLureExp : "";
    String message;
    if (isMajor) {
      message =
          narrative
              + "\n\n"
              + "全属性 +"
              + CultivationRealm.MAJOR_BREAKTHROUGH_STAT_PERCENT
              + "%"
              + " | 灵石 +"
              + CultivationRealm.breakthroughSpiritStonesReward(newRealm)
              + thunderLureText;
    } else {
      message = narrative + "\n\n" + "全属性 +5%" + thunderLureText;
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
      Player user,
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

  private double calculateFinalBreakthroughRate(Player user) {
    double protectionBonus = protectionHelper.calculateProtectionBonus(user);
    List<PlayerBuff> breakthroughBuffs =
        playerBuffRepository.findActiveByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);
    double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();
    double baseSuccessRate = user.calculateBreakthroughSuccessRate();
    return Math.clamp(baseSuccessRate + protectionBonus + pillBonus, 0.0, 100.0);
  }

  private BreakthroughResult handleBreakthroughSuccess(
      Long userId, Player user, long expNeeded, double finalSuccessRate) {
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
      Long userId, Player user, long expNeeded, double finalSuccessRate) {
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
  private void applyMajorBreakthroughBonuses(Player user) {
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
