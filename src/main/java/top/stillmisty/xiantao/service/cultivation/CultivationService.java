package top.stillmisty.xiantao.service.cultivation;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.TribulationType;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.service.ProtectionHelper;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 修仙核心服务 处理突破等核心修仙机制 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CultivationService {

  private static final int TRIBULATION_MAX_ROUNDS = 40;

  private final UserStateService userStateService;
  private final PlayerBuffRepository playerBuffRepository;
  private final ProtectionHelper protectionHelper;
  private final DaoProtectionService daoProtectionService;
  private final SpiritStoneService spiritStoneService;
  private final ChatClient chatClient;
  private final MasterApprenticeService masterApprenticeService;
  private final CombatService combatService;

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
  public BreakthroughResult attemptBreakthrough(Long userId) {
    User user = userStateService.loadUserForUpdate(userId);

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

    // 扣除经验（无论胜败）
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

    CombatTeam bossTeam = new CombatTeam(0L, "天劫");
    bossTeam.addMember(boss);

    BattleResultVO result = combatService.simulate(defendingTeam, bossTeam, TRIBULATION_MAX_ROUNDS);

    boolean playerWon = "Player".equals(result.winner());

    // 应用 HP 变化
    applyHpToUser(user, defendingTeam);
    combatService.applyCombatHpToBeasts(defendingTeam);

    // 清除 buff 和护道关系
    daoProtectionService.clearProtegeRelations(user.getId());
    playerBuffRepository.deleteByUserIdAndType(user.getId(), PlayerBuffType.BREAKTHROUGH);

    if (playerWon) {
      return handleCombatBreakthroughSuccess(
          user, newLevel, newRealm, isMajor, isTribulationRealm, tribulationType, result);
    } else {
      return handleCombatBreakthroughFailure(user, oldLevel, isMajor, tribulationType, result);
    }
  }

  private void applyHpToUser(User user, CombatTeam team) {
    team.members().stream()
        .filter(c -> c instanceof PlayerCombatant)
        .findFirst()
        .ifPresent(
            c -> {
              user.setHpCurrent(Math.max(0, c.getHp()));
              if (c.getHp() <= 0) {
                user.setDying();
              }
            });
    userStateService.saveHpStatus(user);
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

    String narrative = generateCombatNarrative(tribulationType, user.getNickname(), result, true);

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

    String narrative = generateCombatNarrative(tribulationType, user.getNickname(), result, false);

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

  // ===================== LLM 战斗叙事生成 =====================

  private String generateCombatNarrative(
      TribulationType tribulationType, String nickname, BattleResultVO result, boolean playerWon) {
    // 构建战斗摘要
    String combatSummary = buildCombatSummary(result);

    try {
      String prompt =
          """
              你是一位仙侠世界的说书人，擅长渲染雷劫的宏大战斗场面。
              请根据战斗记录撰写一段雷劫战斗过程的描述。

              【修士信息】
              道号：%s
              雷劫类型：%s
              结果：%s

              【战斗记录】
              %s

              【写作要求】
              字数 50-100 字，仙侠古风，气势磅礴，有画面感。
              描写雷劫的威力展现、修士的奋力抵抗、灵兽的助阵牺牲、关键转折点。
              突出"%s"雷劫的独特特征和压迫感。
              %s
              只需要输出战斗描述正文，不要任何前缀、后缀或解释。"""
              .formatted(
                  nickname,
                  tribulationType.getDisplayName(),
                  playerWon ? "修士成功渡过雷劫" : "修士未能抵挡雷劫",
                  combatSummary,
                  tribulationType.getDisplayName(),
                  playerWon ? "结尾要有劫后余生的爽感和境界突破的喜悦。" : "结尾要有雷劫的残酷和道基反噬的悲壮感。");

      String llmResult = chatClient.prompt().user(prompt).call().content();

      if (llmResult != null && !llmResult.isBlank()) {
        return llmResult.strip();
      }
    } catch (Exception e) {
      log.warn("LLM生成战斗叙事失败，使用默认文案", e);
    }

    // 回退文案
    return playerWon ? "雷劫已渡，道行精进！" : "雷劫降临，未能抵挡天劫化身……境界突破失败！";
  }

  private String buildCombatSummary(BattleResultVO result) {
    StringBuilder sb = new StringBuilder();
    sb.append("回合数：").append(result.rounds()).append("\n");

    if (result.playerHpChange() != null) {
      result
          .playerHpChange()
          .forEach(
              (name, hp) ->
                  sb.append(String.format("%s HP：%d → %d\n", name, hp.before(), hp.after())));
    }

    // 提取技能使用统计
    if (result.skillProcs() != null && !result.skillProcs().isEmpty()) {
      sb.append("技能：");
      sb.append(
          result.skillProcs().stream()
              .map(sp -> sp.key() + "×" + sp.count())
              .collect(Collectors.joining("、")));
      sb.append("\n");
    }

    // 提取关键战斗事件（技能攻击 + 击杀 + 重创）
    if (result.combatLog() != null) {
      sb.append("关键事件：\n");
      result.combatLog().stream()
          .filter(e -> e.damageDealt() > 0 || e.isKill())
          .forEach(
              e -> {
                String action =
                    e.attackType() == CombatLogEntry.AttackType.SKILL && e.skillName() != null
                        ? "施展「" + e.skillName() + "」"
                        : "攻击";
                String line =
                    String.format(
                        "  第%d回合：%s%s，对%s造成%d伤害 (HP: %d→%d)",
                        e.round(),
                        e.attackerName(),
                        action,
                        e.defenderName(),
                        e.damageDealt(),
                        e.defenderHpBefore(),
                        e.defenderHpAfter());
                if (e.isKill()) line += " ← 击杀！";
                if (e.defenderHpBefore() > 0
                    && e.defenderHpAfter() > 0
                    && (double) e.defenderHpAfter() / e.defenderHpBefore() < 0.3) {
                  line += " ← 重创！";
                }
                sb.append(line).append("\n");
              });
    }

    return sb.toString();
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
