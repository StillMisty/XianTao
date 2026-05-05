package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.TribulationBoss;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TribulationService {

  private static final int TRIBULATION_COOLDOWN_DAYS = 7;
  private static final int TRIBULATION_MAX_ROUNDS = 40;

  private final FudiCellRepository fudiCellRepository;
  private final SpiritRepository spiritRepository;
  private final BeastRepository beastRepository;
  private final UserRepository userRepository;
  private final FudiHelper fudiHelper;
  private final CombatService combatService;

  /**
   * 触发天劫 — 使用战斗引擎进行回合制战斗
   *
   * @param fudi 福地
   * @param user 玩家
   * @param forceTrigger 是否强制触发（忽略冷却）
   * @return 天劫结果文本，未触发返回 null
   */
  public String resolveTribulation(Fudi fudi, User user, boolean forceTrigger) {
    LocalDateTime referenceTime =
        fudi.getLastTribulationTime() != null
            ? fudi.getLastTribulationTime()
            : fudi.getCreateTime();

    if (!forceTrigger
        && java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays()
            < TRIBULATION_COOLDOWN_DAYS) {
      return null;
    }

    fudi.setLastTribulationTime(LocalDateTime.now());

    // 构建防守方队伍（玩家 + 出战灵兽）
    Team defendingTeam = combatService.buildPlayerTeam(user);

    // 检查是否有存活成员
    if (defendingTeam.aliveMembers().isEmpty()) {
      return "⚠️ 没有可出战的单位，天劫无法降临";
    }

    // 计算防守方队伍总属性（用于 Boss 缩放，天然支持未来多人组队）
    TeamStats teamStats = calculateTeamStats(defendingTeam);

    // 检查是否触发怜悯
    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    boolean compassionTriggered =
        spirit != null && spirit.getAffection() >= 800 && defendingTeam.aliveMembers().size() >= 1;

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
    Team bossTeam = new Team(0L, "天劫");
    bossTeam.addMember(boss);

    // 执行战斗
    BattleResultVO result = combatService.simulate(defendingTeam, bossTeam, TRIBULATION_MAX_ROUNDS);

    boolean playerWon = "Player".equals(result.winner());
    boolean compassionUsed = compassionTriggered && !playerWon;

    // 应用 HP 变化到玩家和灵兽
    applyHpToUser(user, defendingTeam);
    applyHpToBeasts(defendingTeam, user, playerWon);

    if (playerWon) {
      return applyTribulationWin(fudi, spirit, boss);
    } else if (compassionUsed) {
      return applyTribulationCompassion(fudi, spirit, boss);
    } else {
      return applyTribulationLoss(fudi, spirit, boss);
    }
  }

  // ===================== 防守方队伍属性统计 =====================

  private record TeamStats(int totalMaxHp, int avgAttack, int avgDef, int avgSpeed) {}

  private TeamStats calculateTeamStats(Team team) {
    List<Combatant> members = team.members();
    int totalMaxHp = 0, totalAtk = 0, totalDef = 0, totalSpd = 0;
    int count = 0;
    for (Combatant c : members) {
      if (c.isAlive()) {
        totalMaxHp += c.getMaxHp();
        totalAtk += c.getAttack();
        totalDef += c.getDefense();
        totalSpd += c.getSpeed();
        count++;
      }
    }
    count = Math.max(1, count);
    return new TeamStats(totalMaxHp, totalAtk / count, totalDef / count, totalSpd / count);
  }

  // ===================== 战斗后 HP 应用 =====================

  private void applyHpToUser(User user, Team team) {
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
    userRepository.save(user);
  }

  private void applyHpToBeasts(Team team, User user, boolean playerWon) {
    for (Combatant c : team.members()) {
      if (c instanceof top.stillmisty.xiantao.domain.monster.BeastCombatant bc) {
        Beast beast = beastRepository.findById(c.getId()).orElse(null);
        if (beast != null) {
          beast.setHpCurrent(Math.max(0, c.getHp()));
          if (c.getHp() <= 0 && !playerWon) {
            beast.setIsDeployed(false);
          }
          beastRepository.save(beast);
        }
      }
    }
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
    fudiHelper.addSpiritStones(fudi.getUserId(), stoneReward);

    return new TribulationProgress(oldStage, newWinStreak, newStage, stoneReward);
  }

  /** 胜利：正常进阶，好感+5 */
  private String applyTribulationWin(
      Fudi fudi, top.stillmisty.xiantao.domain.fudi.entity.Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    int oldAffection = spirit != null ? spirit.getAffection() : 0;
    if (spirit != null) {
      spirit.addAffection(5);
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

  /** 怜悯：地灵挡劫，进阶但精力归零 */
  private String applyTribulationCompassion(
      Fudi fudi, top.stillmisty.xiantao.domain.fudi.entity.Spirit spirit, TribulationBoss boss) {
    TribulationProgress p = advanceTribulation(fudi);

    spirit.setEmotionState(EmotionState.EXHAUSTED);
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
      spirit.setEmotionState(EmotionState.ANGRY);
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
