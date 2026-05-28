package top.stillmisty.xiantao.service.dungeon;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.vo.MonsterPoolEntry;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
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
  private final BeastRepository beastRepository;
  private final UserStateService userStateService;

  public record CombatOutcome(
      boolean playerWon, boolean memberAlive, long expGained, String monsterName, String summary) {}

  @Transactional
  public CombatOutcome executeCombatForTeam(
      User leader,
      DungeonInstance instance,
      DungeonPoiConfig poi,
      boolean isBoss,
      List<Long> memberIds) {

    MonsterPoolEntry monsterEntry =
        WeightedRandom.select(
            poi.getMonsterPool(), MonsterPoolEntry::weight, ThreadLocalRandom.current());
    if (monsterEntry == null) {
      throw new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND);
    }

    MonsterTemplate monsterTmpl =
        monsterTemplateRepository
            .findById(monsterEntry.monsterTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND));

    int areaMinLevel =
        instance.getCurrentArea() == top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea.OUTER
            ? 1
            : instance.getCurrentArea()
                    == top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea.INNER
                ? 10
                : 20;

    Monster monster = new Monster(monsterTmpl, areaMinLevel, List.of());
    CombatTeam monsterTeam = new CombatTeam(-1L, monster.getName());
    monsterTeam.addMember(monster);

    CombatTeam playerTeam = combatService.buildPlayerTeam(leader);
    Map<Long, User> memberUsers = new HashMap<>();
    Map<Long, CombatTeam> memberTeams = new HashMap<>();

    for (Long memberId : memberIds) {
      if (memberId.equals(leader.getId())) continue;
      User member = userStateService.loadUser(memberId);
      if (member.getHpCurrent() == null || member.getHpCurrent() <= 0) continue;
      memberUsers.put(memberId, member);
      memberTeams.put(memberId, combatService.buildPlayerTeam(member));
    }

    for (CombatTeam mt : memberTeams.values()) {
      for (var combatant : mt.members()) {
        playerTeam.addMember(combatant);
      }
    }

    BattleContext context =
        BattleContext.builder()
            .teamA(playerTeam)
            .teamB(monsterTeam)
            .maxRounds(isBoss ? 30 : 20)
            .scene(BattleContext.BattleScene.DUNGEON)
            .mapId(instance.getDungeonId())
            .playerLevel(leader.getLevel())
            .build();
    BattleResultVO battleResult = combatEngine.simulate(context);

    postCombatProcessor.applyHpToUser(leader, playerTeam);
    userStateService.saveHpStatus(leader);

    for (var entry : memberUsers.entrySet()) {
      postCombatProcessor.applyHpToUser(entry.getValue(), playerTeam);
      userStateService.saveHpStatus(entry.getValue());
    }

    Map<Long, Beast> beastCache = new HashMap<>();
    postCombatProcessor.applyHpToBeasts(playerTeam, leader, true, false, beastCache);
    beastCache.values().forEach(beastRepository::save);

    boolean playerWon = "Player".equals(battleResult.winner());
    boolean anyAlive = !playerTeam.aliveMembers().isEmpty();

    if (!playerWon && !anyAlive) {
      leader.setStatus(UserStatus.DYING);
      leader.setDyingStartTime(LocalDateTime.now());
      userStateService.saveHpStatus(leader);
      for (User m : memberUsers.values()) {
        if (m.getHpCurrent() != null && m.getHpCurrent() <= 0) {
          m.setStatus(UserStatus.DYING);
          m.setDyingStartTime(LocalDateTime.now());
          userStateService.saveHpStatus(m);
        }
      }
      return new CombatOutcome(false, false, 0, monster.getName(), null);
    }

    long expGained = battleResult.expGained();
    if (playerWon && expGained > 0) {
      leader.addExp(expGained);
      for (User m : memberUsers.values()) {
        m.addExp(expGained);
      }
    }

    String summary = battleResult.summary();
    if (summary != null && summary.length() > 200) {
      summary = summary.substring(0, 200) + "...";
    }

    return new CombatOutcome(
        playerWon,
        anyAlive,
        expGained,
        monster.getName(),
        playerWon
            ? ("击败了" + monster.getName() + "！\n" + (summary != null ? summary : ""))
            : "被" + monster.getName() + "击败...");
  }
}
