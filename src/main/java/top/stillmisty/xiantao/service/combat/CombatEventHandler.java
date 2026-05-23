package top.stillmisty.xiantao.service.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.SkillProc;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.DropProcessor;
import top.stillmisty.xiantao.service.FortuneService;

/** COMBAT 事件处理器 — 单次遇怪战斗 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CombatEventHandler {

  private static final int DEFAULT_MAX_ROUNDS = 20;

  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;
  private final HighlightBattleDetector highlightBattleDetector;
  private final DropProcessor dropProcessor;
  private final SkillRepository skillRepository;
  private final BeastRepository beastRepository;
  private final EnlightenmentProcessor enlightenmentProcessor;
  private final FortuneService fortuneService;

  @Transactional
  public EncounterResult handle(
      ActivityEvent event,
      Long userId,
      User user,
      Map<Long, MonsterTemplate> templateMap,
      Map<Long, Skill> skillMap,
      int encounterIndex) {
    Map<String, Object> params = event.getParams();
    long templateId = ((Number) params.get("monster_template_id")).longValue();
    MonsterTemplate tmpl = templateMap.get(templateId);
    if (tmpl == null) return EncounterResult.lost();

    int minCount =
        params.containsKey("min_count") ? ((Number) params.get("min_count")).intValue() : 1;
    int maxCount =
        params.containsKey("max_count") ? ((Number) params.get("max_count")).intValue() : 1;
    int count = WeightedRandom.normalInt(minCount, maxCount, ThreadLocalRandom.current());
    count = Math.max(1, count);

    int recoveryAmount = Math.max(1, user.calculateMaxHp() / 20);
    user.setHpCurrent(Math.min(user.calculateMaxHp(), user.getHpCurrent() + recoveryAmount));

    CombatTeam playerTeam = combatService.buildPlayerTeam(user, skillMap);
    CombatTeam monsterTeam = buildMonsterTeam(tmpl, count, skillMap, userId);

    BattleResultVO result = combatService.simulate(playerTeam, monsterTeam, DEFAULT_MAX_ROUNDS);
    boolean playerWon = result.winner().equals("Player");

    List<CombatLogEntry> logs = result.combatLog() != null ? result.combatLog() : List.of();
    List<SkillProc> skillProcs = result.skillProcs() != null ? result.skillProcs() : List.of();
    HighlightBattleDetector.HighlightInfo highlightInfo =
        highlightBattleDetector.detectHighlight(result, encounterIndex);
    boolean isHighlight = highlightInfo != null;

    String monsterName = tmpl.getName() + (count > 1 ? " x" + count : "");
    EncounterResult encounterResult;
    if (playerWon) {
      double levelModifier = calculateCombatExpModifier(user.getLevel(), tmpl.getBaseLevel());
      long expGained = (long) (tmpl.getExpReward() * count * levelModifier);
      List<DropItem> rawDrops = dropProcessor.processMonsterDrops(tmpl, userId);
      List<DropItem> drops = rawDrops != null ? new ArrayList<>(rawDrops) : List.of();
      encounterResult =
          new EncounterResult(
              true,
              expGained,
              count,
              result.rounds(),
              false,
              drops,
              logs,
              skillProcs,
              isHighlight,
              monsterName);
    } else {
      encounterResult =
          new EncounterResult(
              false,
              0,
              0,
              result.rounds(),
              false,
              List.of(),
              logs,
              skillProcs,
              isHighlight,
              monsterName);
    }

    postCombatProcessor.applyHpToUser(user, playerTeam);

    Map<Long, Beast> beastCache = new HashMap<>();
    List<Beast> deployed = beastRepository.findDeployedByUserId(userId);
    for (Beast beast : deployed) {
      beastCache.put(beast.getId(), beast);
    }
    postCombatProcessor.applyHpToBeasts(
        playerTeam, user, playerWon, highlightInfo != null, beastCache);
    for (Beast beast : beastCache.values()) {
      beastRepository.save(beast);
    }

    if (playerWon) {
      boolean enlightenmentTriggered = enlightenmentProcessor.process(userId, user);
      encounterResult =
          new EncounterResult(
              true,
              encounterResult.expGained(),
              encounterResult.kills(),
              encounterResult.rounds(),
              enlightenmentTriggered,
              encounterResult.drops(),
              encounterResult.logs(),
              encounterResult.skillProcs(),
              encounterResult.isHighlight(),
              encounterResult.monsterName());
    }

    return encounterResult;
  }

  private CombatTeam buildMonsterTeam(
      MonsterTemplate tmpl, int count, Map<Long, Skill> skillMap, Long userId) {
    var fortune = fortuneService.calculate(userId);
    int luckOffset = fortuneService.getMonsterLevelOffset(fortune.luck());
    CombatTeam team = new CombatTeam(0L, "Monsters");
    for (int i = 0; i < count; i++) {
      List<Skill> monsterSkills = List.of();
      if (tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()) {
        monsterSkills =
            tmpl.getSkills().stream().map(skillMap::get).filter(Objects::nonNull).toList();
      }
      int monsterLevel =
          tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3) + luckOffset;
      monsterLevel = Math.max(1, monsterLevel);
      team.addMember(new Monster(tmpl, monsterLevel, monsterSkills));
    }
    return team;
  }

  static double calculateCombatExpModifier(int playerLevel, int monsterLevel) {
    double diff = monsterLevel - playerLevel;
    double modifier = 1.0 + diff * 0.05;
    return Math.clamp(modifier, 0.1, 3.0);
  }
}
