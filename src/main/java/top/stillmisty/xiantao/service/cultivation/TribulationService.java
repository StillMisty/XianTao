package top.stillmisty.xiantao.service.cultivation;

import java.time.Duration;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TribulationService {

  private static final int TRIBULATION_COOLDOWN_HOURS = 1;

  private final FudiCellRepository fudiCellRepository;
  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritStoneService spiritStoneService;
  private final UserStateService userStateService;
  private final CombatService combatService;

  /**
   * 触发天劫 — 使用战斗引擎进行回合制战斗（玩家于福地手动触发）
   *
   * @param fudi 福地
   * @param user 玩家
   * @return 天劫结果文本
   */
  @Transactional
  public String resolveTribulation(Fudi fudi, Player user) {
    if (fudi.getLastTribulationTime() != null) {
      long hoursSinceLast =
          Duration.between(fudi.getLastTribulationTime(), TimeUtil.now()).toHours();
      if (hoursSinceLast < TRIBULATION_COOLDOWN_HOURS) {
        long remaining = TRIBULATION_COOLDOWN_HOURS - hoursSinceLast;
        return String.format("天劫刚过不久，灵气尚在激荡。请%d小时后再次引动。", remaining);
      }
    }

    // 构建防守方队伍（玩家 + 出战灵兽）
    CombatTeam defendingTeam = combatService.buildPlayerTeam(user);
    if (defendingTeam.aliveMembers().isEmpty()) {
      // 无出战单位不消耗冷却
      return "⚠️ 没有可出战的单位，天劫无法降临";
    }

    fudi.setLastTribulationTime(TimeUtil.now());

    // 先持久化天劫状态，防止并发重复触发
    fudiRepository.save(fudi);

    // 计算防守方队伍总属性（用于 Boss 缩放）
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

    // 执行战斗
    CombatTeam bossTeam = new CombatTeam(0L, "天劫");
    bossTeam.addMember(boss);
    BattleResultVO battleResult = combatService.simulate(defendingTeam, bossTeam, 40);
    boolean playerWon = battleResult.winner().equals("Player");
    boolean compassionUsed = compassionTriggered && !playerWon;

    // 应用 HP 变化到玩家和灵兽
    userStateService.saveHpStatus(user);

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
  private String applyTribulationWin(Fudi fudi, @Nullable Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    int oldAffection = spirit != null ? spirit.getAffection() : 0;
    if (spirit != null) {
      spirit.addAffection(5);
      // 事件情绪：击退天劫后地灵兴奋难平（覆盖好感自动档位）
      spirit.setEmotionState(EmotionState.EXCITED);
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

  /** 怜悯：地灵挡劫，玩家照常进阶（不消耗精力，精力机制未实现） */
  private String applyTribulationCompassion(
      Fudi fudi, @Nullable Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    if (spirit != null) {
      // 事件情绪：燃烧灵体挡劫后陷入虚弱
      spirit.setEmotionState(EmotionState.EXHAUSTED);
      spiritRepository.save(spirit);
    }

    return """
        🪽⚡ 天劫降临！地灵燃烧灵体为你扛过天雷……
           劫数：%d → %d ｜ 连胜×%d
           灵石奖励：+%d
           地灵以身挡劫，护你周全。"""
        .formatted(p.oldStage(), p.newStage(), p.newWinStreak(), p.stoneReward());
  }

  /** 失败：地块摧毁，好感下降，连胜中断 摧毁数量根据 Boss 剩余HP比例决定（剩得越少输得越体面） */
  private String applyTribulationLoss(Fudi fudi, @Nullable Spirit spirit, TribulationBoss boss) {
    int oldWinStreak = fudi.getTribulationWinStreak();
    fudi.setTribulationWinStreak(0);

    // 根据 Boss 剩余血量比例决定摧毁力度
    double bossHpRatio = (double) boss.getHp() / boss.getMaxHp();

    List<FudiCell> occupiedCells =
        fudiCellRepository.findByFudiId(fudi.getId()).stream()
            .filter(cell -> cell.getCellType() != CellType.EMPTY)
            .toList();
    int occupiedCount = occupiedCells.size();

    int clearCount;
    if (occupiedCount == 0) {
      // 没有可摧毁的地块（如地块全空或全枯萎已清），仅中断连胜，避免 clamp(min>max) 抛异常
      clearCount = 0;
    } else if (bossHpRatio >= 0.5) {
      clearCount = Math.clamp((int) Math.ceil(0.6 * occupiedCount), 1, occupiedCount);
    } else if (bossHpRatio >= 0.2) {
      clearCount = Math.clamp((int) Math.ceil(0.3 * occupiedCount), 1, occupiedCount);
    } else {
      clearCount = 1;
    }

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
      // 事件情绪：渡劫失败后地灵余怒难平
      spirit.setEmotionState(EmotionState.ANGRY);
      spiritRepository.save(spirit);
    }

    return String.format(
        """
            ⚡ 天劫降临！未能抵挡天劫化身……
               连胜×%d → 中断 ｜ 被毁地块：%d 个
               天劫化身剩余气血：%.0f%%
               好感度：%d → %d""",
        oldWinStreak,
        clearCount,
        bossHpRatio * 100,
        oldAffection,
        spirit != null ? spirit.getAffection() : 0);
  }
}
