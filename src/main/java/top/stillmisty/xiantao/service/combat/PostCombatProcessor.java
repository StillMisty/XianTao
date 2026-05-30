package top.stillmisty.xiantao.service.combat;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.user.entity.User;
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

  public void applyHpToUser(User user, CombatTeam team) {
    for (Combatant c : team.members()) {
      if (c instanceof PlayerCombatant pc && pc.getId().equals(user.getId())) {
        if (c.getHp() <= 0) {
          user.setDying();
        } else {
          user.setHpCurrent(c.getHp());
        }
        break;
      }
    }
  }

  public void applyHpToBeasts(
      CombatTeam team,
      User user,
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
  public void applyCombatHpToBeasts(CombatTeam team, User user, boolean playerWon) {
    Map<Long, Beast> beastCache = new HashMap<>();
    applyHpToBeasts(team, user, playerWon, false, beastCache);
    beastCache.values().forEach(beastRepository::save);
  }
}
