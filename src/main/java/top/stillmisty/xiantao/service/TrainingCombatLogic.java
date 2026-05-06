package top.stillmisty.xiantao.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterEncounterEntry;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.combat.EncounterCalculator;
import top.stillmisty.xiantao.service.combat.HighlightBattleDetector;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingCombatLogic {

  private static final int DEFAULT_MAX_ROUNDS = 20;
  private final UserStateService userStateService;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final EquipmentRepository equipmentRepository;
  private final BeastRepository beastRepository;
  private final CombatService combatService;
  private final EncounterCalculator encounterCalculator;
  private final HighlightBattleDetector highlightBattleDetector;
  private final PostCombatProcessor postCombatProcessor;
  private final DropProcessor dropProcessor;
  private final SkillRepository skillRepository;

  public BattleResultVO simulateTraining(
      Long userId, User user, int durationMinutes, MapNode mapNode) {
    List<MonsterEncounterEntry> monsterEncounters = mapNode.getMonsterEncounters();
    if (monsterEncounters == null || monsterEncounters.isEmpty()) {
      return buildEmptyBattleResult(mapNode);
    }

    var encounterParams = computeEncounterParams(userId, user, mapNode, durationMinutes);
    var preloadedData = preloadMonsterData(monsterEncounters);

    CombatContext ctx = new CombatContext(userId, user, mapNode, monsterEncounters);

    for (int i = 0; i < encounterParams.encounterChances(); i++) {
      if (ThreadLocalRandom.current().nextDouble() >= encounterParams.encounterChance()) continue;

      MonsterEncounterEntry entry =
          WeightedRandom.select(
              monsterEncounters, MonsterEncounterEntry::weight, ThreadLocalRandom.current());
      if (entry == null) continue;
      MonsterTemplate tmpl = preloadedData.templateMap.get(entry.templateId());
      if (tmpl == null) continue;

      ctx.totalEncounters++;
      int count = WeightedRandom.normalInt(entry.min(), entry.max(), ThreadLocalRandom.current());

      boolean shouldStop = processSingleEncounter(ctx, entry, tmpl, count, preloadedData.skillMap);
      if (shouldStop) {
        return buildSummaryFromContext(ctx);
      }
    }

    finalizeTraining(ctx);
    return buildSummaryFromContext(ctx);
  }

  private PreloadedMonsterData preloadMonsterData(List<MonsterEncounterEntry> monsterEncounters) {
    var templateIds = monsterEncounters.stream().map(MonsterEncounterEntry::templateId).toList();
    Map<Long, MonsterTemplate> templateMap =
        monsterTemplateRepository.findByIds(templateIds).stream()
            .collect(Collectors.toMap(MonsterTemplate::getId, t -> t));

    var skillIds =
        templateMap.values().stream()
            .flatMap(
                t -> t.getSkills() != null ? t.getSkills().stream() : java.util.stream.Stream.of())
            .distinct()
            .toList();
    Map<Long, Skill> skillMap;
    if (!skillIds.isEmpty()) {
      skillMap =
          skillRepository.findByIds(skillIds).stream()
              .collect(Collectors.toMap(Skill::getId, s -> s));
    } else {
      skillMap = Map.of();
    }
    return new PreloadedMonsterData(templateMap, skillMap);
  }

  private boolean processSingleEncounter(
      CombatContext ctx,
      MonsterEncounterEntry entry,
      MonsterTemplate tmpl,
      int count,
      Map<Long, Skill> skillMap) {
    Team playerTeam = combatService.buildPlayerTeam(ctx.user);
    Team monsterTeam = buildMonsterTeamWithSkills(tmpl, count, skillMap);

    BattleResultVO result = combatService.simulate(playerTeam, monsterTeam, DEFAULT_MAX_ROUNDS);
    ctx.totalRounds += result.rounds();
    if (result.combatLog() != null) ctx.allLogs.addAll(result.combatLog());
    if (result.skillProcs() != null) ctx.allSkillProcs.addAll(result.skillProcs());

    boolean playerWon = result.winner().equals("Player");
    if (playerWon) {
      ctx.totalKills += count;
      ctx.expGained += tmpl.getBaseLevel() * 10L * count;
      ctx.allDrops.addAll(dropProcessor.processMonsterDrops(tmpl));
    } else {
      ctx.defeatCount++;
    }

    HighlightBattleDetector.HighlightInfo highlightInfo =
        highlightBattleDetector.detectHighlight(result, ctx.totalEncounters);
    boolean isHighlightBattle = highlightInfo != null;

    postCombatProcessor.applyHpToUser(ctx.user, playerTeam);
    postCombatProcessor.applyHpToBeasts(
        playerTeam, ctx.user, playerWon, isHighlightBattle, ctx.beastCache);

    if (!playerWon && playerTeam.isAllDead()) {
      if (!ctx.allDrops.isEmpty()) {
        dropProcessor.distributeDrops(ctx.userId, ctx.allDrops);
      }
      if (ctx.expGained > 0) {
        ctx.user.addExp(ctx.expGained);
      }
      ctx.user.setDying();
      ctx.user.setTrainingStartTime(null);
      userStateService.save(ctx.user);
      saveBeastCache(ctx.beastCache);
      return true;
    }
    return false;
  }

  private void finalizeTraining(CombatContext ctx) {
    if (ctx.expGained > 0) {
      ctx.user.addExp(ctx.expGained);
    }
    userStateService.save(ctx.user);
    saveBeastCache(ctx.beastCache);
    if (!ctx.allDrops.isEmpty()) {
      dropProcessor.distributeDrops(ctx.userId, ctx.allDrops);
    }
  }

  private BattleResultVO buildSummaryFromContext(CombatContext ctx) {
    return buildSummary(
        ctx.user,
        ctx.mapNode,
        ctx.totalEncounters,
        ctx.totalKills,
        ctx.defeatCount,
        ctx.expGained,
        ctx.totalRounds,
        ctx.allDrops,
        ctx.allLogs,
        ctx.allSkillProcs);
  }

  private record PreloadedMonsterData(
      Map<Long, MonsterTemplate> templateMap, Map<Long, Skill> skillMap) {}

  public static final class CombatContext {
    final Long userId;
    final User user;
    final MapNode mapNode;
    final List<MonsterEncounterEntry> monsterEncounters;
    long expGained = 0L;
    final List<DropItem> allDrops = new ArrayList<>();
    final List<CombatLogEntry> allLogs = new ArrayList<>();
    final List<Map<String, Object>> allSkillProcs = new ArrayList<>();
    int totalRounds = 0;
    int totalEncounters = 0;
    int totalKills = 0;
    int defeatCount = 0;
    final Map<Long, Beast> beastCache = new LinkedHashMap<>();

    CombatContext(
        Long userId, User user, MapNode mapNode, List<MonsterEncounterEntry> monsterEncounters) {
      this.userId = userId;
      this.user = user;
      this.mapNode = mapNode;
      this.monsterEncounters = monsterEncounters;
    }
  }

  private EncounterParams computeEncounterParams(
      Long userId, User user, MapNode mapNode, int durationMinutes) {
    int gearScore = calculateGearScore(userId);
    double interval =
        encounterCalculator.calculateInterval(
            mapNode.getLevelRequirement(), user.getLevel(), gearScore);
    int chances = encounterCalculator.calculateEncounterChances(durationMinutes, interval);
    double chance = encounterCalculator.calculateEncounterChance(interval);
    return new EncounterParams(chances, chance);
  }

  private Team buildMonsterTeamWithSkills(
      MonsterTemplate tmpl, int count, Map<Long, Skill> skillMap) {
    Team team = new Team(0L, "Monsters");
    for (int j = 0; j < count; j++) {
      List<Skill> monsterSkills =
          tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()
              ? tmpl.getSkills().stream().map(skillMap::get).filter(Objects::nonNull).toList()
              : List.of();
      int monsterLevel = tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3);
      monsterLevel = Math.max(1, monsterLevel);
      team.addMember(new Monster(tmpl, monsterLevel, monsterSkills));
    }
    return team;
  }

  private void saveBeastCache(Map<Long, Beast> beastCache) {
    for (Beast beast : beastCache.values()) {
      beastRepository.save(beast);
    }
  }

  private int calculateGearScore(Long userId) {
    List<Equipment> equipped = equipmentRepository.findEquippedByUserId(userId);
    int score = 0;
    for (Equipment equip : equipped) {
      score += equip.getFinalAttack() + equip.getFinalDefense() * 2;
      score +=
          switch (equip.getRarity()) {
            case BROKEN -> 0;
            case COMMON -> 10;
            case RARE -> 40;
            case EPIC -> 80;
            case LEGENDARY -> 160;
          };
    }
    return score;
  }

  private BattleResultVO buildEmptyBattleResult(MapNode mapNode) {
    return BattleResultVO.builder()
        .winner("NONE")
        .rounds(0)
        .expGained(0L)
        .summary(mapNode.getName() + "风平浪静，未遇敌袭")
        .build();
  }

  private BattleResultVO buildSummary(
      User user,
      MapNode mapNode,
      int totalEncounters,
      int totalKills,
      int defeatCount,
      long expGained,
      int totalRounds,
      List<DropItem> allDrops,
      List<CombatLogEntry> allLogs,
      List<Map<String, Object>> allSkillProcs) {
    StringBuilder summary = new StringBuilder();
    summary.append(String.format("遇敌%d场 | 击杀%d只", totalEncounters, totalKills));
    if (defeatCount > 0) {
      summary.append(String.format(" | 战败%d场", defeatCount));
    }
    summary.append("\n");
    if (expGained > 0) summary.append(String.format("经验+%d", expGained));
    if (!allDrops.isEmpty()) {
      summary.append(" | ");
      for (int i = 0; i < allDrops.size(); i++) {
        DropItem drop = allDrops.get(i);
        summary.append(String.format("%s×%d", drop.name(), drop.quantity()));
        if (i < allDrops.size() - 1) summary.append(" ");
      }
    }

    return BattleResultVO.builder()
        .winner("Player")
        .rounds(totalRounds)
        .drops(allDrops)
        .expGained(expGained)
        .combatLog(allLogs)
        .skillProcs(allSkillProcs)
        .summary(summary.toString())
        .build();
  }

  private record EncounterParams(int encounterChances, double encounterChance) {}
}
