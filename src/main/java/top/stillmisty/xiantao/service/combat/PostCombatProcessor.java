package top.stillmisty.xiantao.service.combat;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.beast.BeastSkillService;
import top.stillmisty.xiantao.service.beast.MutationEffectResolver;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCombatProcessor {

  private final BeastRepository beastRepository;
  private final BeastSkillService beastSkillService;
  private final MutationEffectResolver effectResolver;
  private final CombatService combatService;

  /** 一场战斗的结果 — 原始战报与按队伍名判定的胜负 */
  public record BattleOutcome(BattleResultVO result, boolean playerWon) {}

  /**
   * 执行一场战斗并落地后果：按队伍名判定胜负，写回角色气血/濒死与灵兽气血/休养/觉醒。
   *
   * @param beastCache 预加载的灵兽缓存；传入时复用其中实体并保存，为 {@code null} 时自行查询
   */
  @Transactional
  public BattleOutcome resolve(
      Player user,
      CombatTeam playerTeam,
      CombatTeam opponentTeam,
      int maxRounds,
      @Nullable Map<Long, Beast> beastCache) {
    BattleResultVO result = combatService.simulate(playerTeam, opponentTeam, maxRounds);
    boolean playerWon = result.winnerIs(playerTeam.name());

    applyHpToUser(user, playerTeam);
    if (beastCache == null) {
      applyCombatHpToBeasts(playerTeam, user, playerWon);
    } else {
      applyHpToBeasts(playerTeam, user, playerWon, false, beastCache);
      beastCache.values().forEach(beastRepository::save);
    }
    return new BattleOutcome(result, playerWon);
  }

  public void applyHpToUser(Player user, CombatTeam team) {
    for (Combatant c : team.members()) {
      if (c instanceof PlayerCombatant pc && pc.getId().equals(user.getId())) {
        if (c.getHp() <= 0) {
          user.setDying(TimeUtil.now());
        } else {
          // 被动法决可能提高战斗内气血上限，写回时以基础上限截断
          user.setHpCurrent(Math.min(user.calculateMaxHp(), c.getHp()));
        }
        break;
      }
    }
  }

  public void applyHpToBeasts(
      CombatTeam team,
      Player user,
      boolean playerWon,
      boolean isHighlightBattle,
      Map<Long, Beast> beastCache) {
    for (Combatant c : team.members()) {
      if (c instanceof BeastCombatant) {
        Beast beast =
            beastCache.computeIfAbsent(c.getId(), id -> beastRepository.findById(id).orElse(null));
        if (beast == null) continue;

        beast.setHpCurrent(c.getHp());
        if (!c.isAlive()) {
          beast.setIsDeployed(false);
          int recoveryMinutes = beast.getQuality().getRecoveryMinutes();
          beast.setRecoveryUntil(TimeUtil.now().plusMinutes(recoveryMinutes));
        } else {
          double healPercent =
              effectResolver.sumEffectValue(beast, MutationEffectType.ON_BATTLE_END_HEAL);
          if (healPercent > 0) {
            int healAmount = (int) (beast.getMaxHp() * healPercent / 100);
            beast.setHpCurrent(Math.min(beast.getMaxHp(), beast.getHpCurrent() + healAmount));
          }
        }

        if (playerWon) {
          beastSkillService.tryAwakeningSkill(beast);
        }
      }
    }
  }

  @Transactional
  public void applyCombatHpToBeasts(CombatTeam team, Player user, boolean playerWon) {
    Map<Long, Beast> beastCache = new HashMap<>();
    applyHpToBeasts(team, user, playerWon, false, beastCache);
    beastCache.values().forEach(beastRepository::save);
  }
}
