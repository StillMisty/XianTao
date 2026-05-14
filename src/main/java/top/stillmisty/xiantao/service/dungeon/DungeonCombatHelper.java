package top.stillmisty.xiantao.service.dungeon;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.vo.MonsterPoolEntry;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.CombatService;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.PostCombatProcessor;
import top.stillmisty.xiantao.service.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonCombatHelper {

  private final MonsterTemplateRepository monsterTemplateRepository;
  private final CombatEngine combatEngine;
  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;
  private final BeastRepository beastRepository;
  private final UserStateService userStateService;

  public record CombatOutcome(
      boolean playerWon, long expGained, String monsterName, String summary) {}

  public CombatOutcome executeCombat(
      User user, DungeonInstance instance, DungeonPoiConfig poi, boolean isBoss) {
    MonsterPoolEntry monsterEntry =
        weightedRandom(poi.getMonsterPool(), MonsterPoolEntry::weight, poi.getMonsterWeightTotal());
    if (monsterEntry == null) {
      throw new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND);
    }

    MonsterTemplate monsterTmpl =
        monsterTemplateRepository
            .findById(monsterEntry.monsterTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND));

    Team playerTeam = combatService.buildPlayerTeam(user);

    int monsterLevel = monsterTmpl.getBaseLevel();
    Monster monster = new Monster(monsterTmpl, monsterLevel, List.of());
    Team monsterTeam = new Team(-1L, monster.getName());
    monsterTeam.addMember(monster);

    BattleContext context =
        BattleContext.builder()
            .teamA(playerTeam)
            .teamB(monsterTeam)
            .maxRounds(isBoss ? 30 : 20)
            .scene(BattleContext.BattleScene.DUNGEON)
            .mapId(instance.getDungeonId())
            .playerLevel(user.getLevel())
            .build();
    BattleResultVO battleResult = combatEngine.simulate(context);

    postCombatProcessor.applyHpToUser(user, playerTeam);

    Map<Long, Beast> beastCache = new HashMap<>();
    postCombatProcessor.applyHpToBeasts(playerTeam, user, true, false, beastCache);
    beastCache.values().forEach(beastRepository::save);

    userStateService.save(user);

    boolean playerWon = "Player".equals(battleResult.winner());

    if (!playerWon && user.getHpCurrent() <= 0) {
      instance.markFailed();
      user.setStatus(UserStatus.DYING);
      user.setDyingStartTime(LocalDateTime.now());
      return new CombatOutcome(false, 0, monster.getName(), null);
    }

    long expGained = battleResult.expGained();
    if (playerWon && expGained > 0) {
      user.addExp(expGained);
    }

    String summary = battleResult.summary();
    if (summary != null && summary.length() > 200) {
      summary = summary.substring(0, 200) + "...";
    }

    return new CombatOutcome(
        playerWon,
        expGained,
        monster.getName(),
        playerWon
            ? ("击败了" + monster.getName() + "！\n" + (summary != null ? summary : ""))
            : "被" + monster.getName() + "击败...");
  }

  private <T> T weightedRandom(
      List<T> items, java.util.function.ToIntFunction<T> weightFn, int totalWeight) {
    if (items == null || items.isEmpty() || totalWeight <= 0) return null;
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (T item : items) {
      cumulative += weightFn.applyAsInt(item);
      if (roll < cumulative) return item;
    }
    return items.getLast();
  }
}
