package top.stillmisty.xiantao.service.combat;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.monster.BeastCombatant;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.beast.BeastSkillService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCombatProcessor {

  private final BeastRepository beastRepository;
  private final BeastSkillService beastSkillService;

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
          beast.setRecoveryUntil(LocalDateTime.now().plusMinutes(recoveryMinutes));
        } else if (beast.getMutationTraits() != null
            && beast.getMutationTraits().contains("SELF_HEAL")) {
          int healAmount = (int) (beast.getMaxHp() * 0.10);
          beast.setHpCurrent(Math.min(beast.getMaxHp(), beast.getHpCurrent() + healAmount));
        }

        if (playerWon) {
          beastSkillService.tryAwakeningSkill(beast);
        }
      }
    }
  }
}
