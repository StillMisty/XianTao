package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.monster.BeastCombatant;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.user.entity.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCombatProcessor {

  private final BeastRepository beastRepository;
  private final BeastService beastService;

  public void applyHpToUser(User user, Team team) {
    for (Combatant c : team.members()) {
      if (c instanceof PlayerCombatant) {
        user.setHpCurrent(c.getHp());
      }
    }
  }

  public void applyHpToBeasts(
      Team team,
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

          if (playerWon) {
            beastService.tryAwakeningSkill(beast);
          }
        } else {
          if (isHighlightBattle) {
            beastService.tryAwakeningSkill(beast);
          }
        }
      }
    }
  }
}
