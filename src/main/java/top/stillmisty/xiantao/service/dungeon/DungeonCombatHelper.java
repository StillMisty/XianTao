package top.stillmisty.xiantao.service.dungeon;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.combat.PostCombatProcessor;
import top.stillmisty.xiantao.service.player.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonCombatHelper {

  private final MonsterTemplateRepository monsterTemplateRepository;
  private final CombatEngine combatEngine;
  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;
  private final UserStateService userStateService;

  public record SimpleCombatOutcome(
      boolean playerWon,
      long expGained,
      String monsterName,
      String summary,
      List<String> lootDescriptions) {}

  @Transactional
  public SimpleCombatOutcome executeCombat(
      Long userId, User user, DungeonTemplate.Poi poi, DungeonTemplate.MonsterEntry monsterEntry) {

    MonsterTemplate monsterTmpl =
        monsterTemplateRepository
            .findById(monsterEntry.templateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND));

    Monster monster = new Monster(monsterTmpl, 1, List.of());
    CombatTeam monsterTeam = new CombatTeam(-1L, monster.getName());
    monsterTeam.addMember(monster);

    CombatTeam playerTeam = combatService.buildPlayerTeam(user);

    BattleContext context =
        BattleContext.builder()
            .teamA(playerTeam)
            .teamB(monsterTeam)
            .maxRounds(20)
            .scene(BattleContext.BattleScene.DUNGEON)
            .playerLevel(user.getLevel())
            .build();
    BattleResultVO battleResult = combatEngine.simulate(context);

    postCombatProcessor.applyHpToUser(user, playerTeam);
    userStateService.saveHpStatus(user);

    boolean playerWon = "Player".equals(battleResult.winner());

    if (!playerWon && playerTeam.aliveMembers().isEmpty()) {
      user.setStatus(UserStatus.DYING);
      user.setDyingStartTime(TimeUtil.now());
      userStateService.saveHpStatus(user);
      return new SimpleCombatOutcome(false, 0, monster.getName(), "你被击败了，陷入了濒死状态。", List.of());
    }

    long expGained = battleResult.expGained();
    if (playerWon && expGained > 0) {
      user.addExp(expGained);
    }

    String summary = battleResult.summary();
    if (summary != null && summary.length() > 200) {
      summary = summary.substring(0, 200) + "...";
    }

    return new SimpleCombatOutcome(
        playerWon,
        expGained,
        monster.getName(),
        playerWon
            ? ("击败了" + monster.getName() + "！\n" + (summary != null ? summary : ""))
            : "被" + monster.getName() + "击退了...",
        List.of());
  }

  @SuppressWarnings("NullAway")
  public DungeonTemplate.MonsterEntry selectMonster(DungeonTemplate.Poi poi) {
    if (poi.monsterPool() == null || poi.monsterPool().isEmpty()) return null;
    return WeightedRandom.select(
        poi.monsterPool(), DungeonTemplate.MonsterEntry::weight, ThreadLocalRandom.current());
  }
}
