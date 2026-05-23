package top.stillmisty.xiantao.service.cultivation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TribulationService {

  private static final int TRIBULATION_MAX_ROUNDS = 40;
  private static final int TRIBULATION_COOLDOWN_HOURS = 1;

  private final FudiCellRepository fudiCellRepository;
  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritStoneService spiritStoneService;
  private final CombatService combatService;
  private final UserStateService userStateService;

  /**
   * 触发天劫 — 使用战斗引擎进行回合制战斗（玩家于福地手动触发）
   *
   * @param fudi 福地
   * @param user 玩家
   * @return 天劫结果文本
   */
  @Transactional
  public String resolveTribulation(Fudi fudi, User user) {
    if (fudi.getLastTribulationTime() != null) {
      long hoursSinceLast =
          Duration.between(fudi.getLastTribulationTime(), LocalDateTime.now()).toHours();
      if (hoursSinceLast < TRIBULATION_COOLDOWN_HOURS) {
        long remaining = TRIBULATION_COOLDOWN_HOURS - hoursSinceLast;
        return String.format("天劫刚过不久，灵气尚在激荡。请%d小时后再次引动。", remaining);
      }
    }

    fudi.setLastTribulationTime(LocalDateTime.now());
    if (fudi.getTribulationStage() == null) {
      fudi.setTribulationStage(0);
    }
    if (fudi.getTribulationWinStreak() == null) {
      fudi.setTribulationWinStreak(0);
    }

    // 先持久化天劫状态，防止并发重复触发
    fudiRepository.save(fudi);

    // 构建防守方队伍（玩家 + 出战灵兽）
    CombatTeam defendingTeam = combatService.buildPlayerTeam(user);

    // 检查是否有存活成员
    if (defendingTeam.aliveMembers().isEmpty()) {
      return "⚠️ 没有可出战的单位，天劫无法降临";
    }

    // 计算防守方队伍总属性（用于 Boss 缩放，天然支持未来多人组队）
    CombatService.TeamStats teamStats = combatService.calculateTeamStats(defendingTeam);

    // 检查是否触发怜悯
    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    boolean compassionTriggered =
        spirit != null && spirit.getAffection() >= 800 && !defendingTeam.aliveMembers().isEmpty();

    // 生成天劫化身
    TribulationBoss boss =
        new TribulationBoss(
            teamStats.totalMaxHp(),
            teamStats.avgAttack(),
            teamStats.avgDef(),
            teamStats.avgSpeed(),
            fudi.getTribulationStage(),
            compassionTriggered);

    // Boss 队伍
    CombatTeam bossTeam = new CombatTeam(0L, "天劫");
    bossTeam.addMember(boss);

    // 执行战斗
    BattleResultVO result = combatService.simulate(defendingTeam, bossTeam, TRIBULATION_MAX_ROUNDS);

    boolean playerWon = "Player".equals(result.winner());
    boolean compassionUsed = compassionTriggered && !playerWon;

    // 应用 HP 变化到玩家和灵兽
    applyHpToUser(user, defendingTeam);
    combatService.applyCombatHpToBeasts(defendingTeam);

    String tribulationResult;
    if (playerWon) {
      tribulationResult = applyTribulationWin(fudi, spirit, boss);
    } else if (compassionUsed) {
      tribulationResult = applyTribulationCompassion(fudi, spirit, boss);
    } else {
      tribulationResult = applyTribulationLoss(fudi, spirit, boss);
    }

    fudiRepository.save(fudi);
    return tribulationResult;
  }

  // ===================== 战斗后 HP 应用 =====================

  private void applyHpToUser(User user, CombatTeam team) {
    team.members().stream()
        .filter(c -> c instanceof top.stillmisty.xiantao.domain.monster.PlayerCombatant)
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

  // ===================== 天劫结果处理 =====================

  private record TribulationProgress(
      int oldStage, int newWinStreak, int newStage, int stoneReward) {}

  private TribulationProgress advanceTribulation(Fudi fudi) {
    int oldStage = fudi.getTribulationStage();
    int newWinStreak = fudi.getTribulationWinStreak() + 1;
    int newStage = oldStage + 1;

    fudi.setTribulationWinStreak(newWinStreak);
    fudi.setTribulationStage(newStage);

    int stoneReward = newWinStreak * 100;
    spiritStoneService.deposit(fudi.getUserId(), stoneReward);

    return new TribulationProgress(oldStage, newWinStreak, newStage, stoneReward);
  }

  /** 胜利：正常进阶，好感+5 */
  private String applyTribulationWin(
      Fudi fudi, top.stillmisty.xiantao.domain.fudi.entity.Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    int oldAffection = spirit != null ? spirit.getAffection() : 0;
    if (spirit != null) {
      spirit.addAffection(5);
      spiritRepository.save(spirit);
    }

    return String.format(
        """
            ⚡ 天劫降临！成功击退天劫化身！
               劫数：%d → %d ｜ 连胜×%d
               灵石奖励：+%d ｜ 好感度：%d → %d""",
        p.oldStage(),
        p.newStage(),
        p.newWinStreak(),
        p.stoneReward(),
        oldAffection,
        spirit != null ? spirit.getAffection() : 0);
  }

  /** 怜悯：地灵挡劫，进阶但精力归零 */
  private String applyTribulationCompassion(
      Fudi fudi, top.stillmisty.xiantao.domain.fudi.entity.Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    spiritRepository.save(spirit);

    return """
        🪽⚡ 天劫降临！地灵燃烧灵体为你扛过天雷……
           劫数：%d → %d ｜ 连胜×%d
           灵石奖励：+%d
           精力归零，地灵陷入疲惫…"""
        .formatted(p.oldStage(), p.newStage(), p.newWinStreak(), p.stoneReward());
  }

  /** 失败：地块摧毁，好感下降，连胜中断 摧毁数量根据 Boss 剩余HP比例决定（剩得越少输得越体面） */
  private String applyTribulationLoss(
      Fudi fudi, top.stillmisty.xiantao.domain.fudi.entity.Spirit spirit, TribulationBoss boss) {
    int oldWinStreak = fudi.getTribulationWinStreak();
    fudi.setTribulationWinStreak(0);

    // 根据 Boss 剩余血量比例决定摧毁力度
    double bossHpRatio = (double) boss.getHp() / boss.getMaxHp();
    int occupiedCount =
        (int)
            fudiCellRepository.findByFudiId(fudi.getId()).stream()
                .filter(cell -> cell.getCellType() != CellType.EMPTY)
                .count();

    int clearCount;
    if (bossHpRatio >= 0.5) {
      clearCount = Math.clamp((int) Math.ceil(0.6 * occupiedCount), 1, occupiedCount);
    } else if (bossHpRatio >= 0.2) {
      clearCount = Math.clamp((int) Math.ceil(0.3 * occupiedCount), 1, occupiedCount);
    } else {
      clearCount = 1;
    }

    List<FudiCell> occupiedCells =
        fudiCellRepository.findByFudiId(fudi.getId()).stream()
            .filter(cell -> cell.getCellType() != CellType.EMPTY)
            .toList();
    List<FudiCell> cellsToDestroy = new ArrayList<>(occupiedCells);
    Collections.shuffle(cellsToDestroy);
    cellsToDestroy = cellsToDestroy.subList(0, Math.min(clearCount, cellsToDestroy.size()));

    for (FudiCell cell : cellsToDestroy) {
      cell.setCellType(CellType.EMPTY);
      cell.clearConfig();
      fudiCellRepository.save(cell);
    }

    int oldAffection = spirit != null ? spirit.getAffection() : 0;
    if (spirit != null) {
      spirit.addAffection(-clearCount);
      spiritRepository.save(spirit);
    }

    return String.format(
        """
            ⚡ 天劫降临！未能抵挡天劫化身……
               连胜×%d → 中断 ｜ 被毁地块：%d 个
               Boss剩余HP：%.0f%%
               好感度：%d → %d""",
        oldWinStreak,
        clearCount,
        bossHpRatio * 100,
        oldAffection,
        spirit != null ? spirit.getAffection() : 0);
  }
}
