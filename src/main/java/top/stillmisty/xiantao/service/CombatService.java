package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterSpawn;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.monster.*;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CombatService {

    private static final int DEFAULT_MAX_ROUNDS = 20;

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final MapNodeRepository mapNodeRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final SkillRepository skillRepository;
    private final BeastRepository beastRepository;
    private final EquipmentService equipmentService;
    private final StackableItemService stackableItemService;
    private final AuthenticationService authService;
    private final CombatEngine combatEngine;
    private final EncounterCalculator encounterCalculator;
    private final HighlightBattleDetector highlightBattleDetector;
    private final BeastService beastService;
    private final PlayerBuffRepository playerBuffRepository;

    /**
     * 单次战斗模拟（泛型：仅操作 Team/Combatant 接口）
     */
    public BattleResultVO simulate(Team teamA, Team teamB, int maxRounds) {
        Battle battle = Battle.of(teamA, teamB, BattleContext.BattleScene.TRAINING, maxRounds, combatEngine);
        return battle.execute();
    }

    public ServiceResult<BattleResultVO> simulateTraining(PlatformType platform, String openId, int durationMinutes) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(simulateTraining(auth.userId(), durationMinutes));
    }

    public BattleResultVO simulateTraining(Long userId, int durationMinutes) {
        User user = userRepository.findById(userId).orElseThrow();
        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

        long expGained = 0L;
        List<Map<String, Object>> allDrops = new ArrayList<>();
        List<Map<String, Object>> allLogs = new ArrayList<>();
        List<Map<String, Object>> allSkillProcs = new ArrayList<>();
        int totalRounds = 0;
        int totalEncounters = 0;
        int totalKills = 0;
        int defeatCount = 0;

        Map<Long, MonsterSpawn> monsterEncounters = mapNode.getMonsterEncounters();
        if (monsterEncounters == null || monsterEncounters.isEmpty()) {
            return buildEmptyBattleResult(user, mapNode);
        }

        int gearScore = calculateGearScore(userId);
        double encounterInterval = encounterCalculator.calculateInterval(
                mapNode.getLevelRequirement(), user.getLevel(), gearScore);
        int encounterChances = encounterCalculator.calculateEncounterChances(durationMinutes, encounterInterval);
        double encounterChance = encounterCalculator.calculateEncounterChance(encounterInterval);

        List<HighlightBattleDetector.HighlightInfo> highlightInfos = new ArrayList<>();

        for (int i = 0; i < encounterChances; i++) {
            if (ThreadLocalRandom.current().nextDouble() >= encounterChance) continue;

            Long selectedTemplateId = weightedSelect(monsterEncounters);
            if (selectedTemplateId == null) continue;

            MonsterSpawn spawn = monsterEncounters.get(selectedTemplateId);
            MonsterTemplate tmpl = monsterTemplateRepository.findById(selectedTemplateId).orElse(null);
            if (tmpl == null) continue;

            totalEncounters++;
            int count = spawn.min() + ThreadLocalRandom.current().nextInt(spawn.max() - spawn.min() + 1);

            // 构建队伍
            Team playerTeam = buildPlayerTeam(user);
            Team monsterTeam = buildMonsterTeam(tmpl, count);

            // 创建战斗实体 → 执行
            Battle battle = Battle.of(playerTeam, monsterTeam,
                    BattleContext.BattleScene.TRAINING, DEFAULT_MAX_ROUNDS, combatEngine);
            battle.execute();
            BattleResultVO result = battle.getResult();

            totalRounds += result.rounds();
            if (result.combatLog() != null) allLogs.addAll(result.combatLog());
            if (result.skillProcs() != null) allSkillProcs.addAll(result.skillProcs());

            boolean playerWon = battle.isTeamAWin();
            if (playerWon) {
                totalKills += count;
                expGained += tmpl.getBaseLevel() * 10L * count;
                allDrops.addAll(processDrops(tmpl));
            } else {
                defeatCount++;
            }

            // 高光战斗检测
            HighlightBattleDetector.HighlightInfo highlightInfo =
                    highlightBattleDetector.detectHighlight(result, totalEncounters);
            boolean isHighlightBattle = highlightInfo != null;
            if (isHighlightBattle) highlightInfos.add(highlightInfo);

            // 战后 HP 回写 + 灵兽休养
            applyCombatHpToUser(user, playerTeam);
            applyCombatHpToBeasts(playerTeam, user, playerWon, isHighlightBattle);

            // 玩家阵亡
            if (!playerWon && playerTeam.isAllDead()) {
                user.setDying();
                userRepository.save(user);
                break;
            }
        }

        // 应用经验
        if (expGained > 0) {
            user.addExp(expGained);
        }

        // 发放掉落物品
        for (Map<String, Object> drop : allDrops) {
            String type = (String) drop.get("type");
            if ("equipment".equals(type)) {
                Long templateId = toLong(drop.get("templateId"));
                if (templateId != null) equipmentService.createEquipment(userId, templateId);
            } else if ("item".equals(type)) {
                Long templateId = toLong(drop.get("templateId"));
                String name = (String) drop.get("name");
                Integer quantity = (Integer) drop.getOrDefault("quantity", 1);
                if (templateId != null && name != null && quantity != null) {
                    stackableItemService.addStackableItem(userId, templateId, ItemType.MATERIAL, name, quantity);
                }
            }
        }

        userRepository.save(user);

        return buildSummary(user, mapNode, totalEncounters, totalKills, defeatCount,
                expGained, totalRounds, allDrops, allLogs, allSkillProcs);
    }

    // ===================== 队伍构建 =====================

    private Team buildPlayerTeam(User user) {
        Team team = new Team(user.getId(), "Player");

        List<PlayerBuff> activeBuffs = playerBuffRepository.findActiveByUserId(user.getId());
        int attackBuff = 0, defenseBuff = 0, speedBuff = 0;
        for (PlayerBuff buff : activeBuffs) {
            switch (buff.getBuffType()) {
                case "attack" -> attackBuff += buff.getValue();
                case "defense" -> defenseBuff += buff.getValue();
                case "speed" -> speedBuff += buff.getValue();
            }
        }

        team.addMember(new PlayerCombatant(user, equipmentRepository, equipmentTemplateRepository)
                .withBuffs(attackBuff, defenseBuff, speedBuff));

        List<Beast> deployed = beastRepository.findDeployedByUserId(user.getId());
        int beastLimit = Math.min(3, user.getLevel() / 5 + 1);
        for (int i = 0; i < Math.min(deployed.size(), beastLimit); i++) {
            Beast beast = deployed.get(i);
            if (beast.canFight()) {
                List<Skill> beastSkills = List.of();
                if (beast.getSkills() != null && !beast.getSkills().isEmpty()) {
                    beastSkills = skillRepository.findByIds(beast.getSkills());
                }
                team.addMember(new BeastCombatant(beast, beastSkills));
            }
        }
        return team;
    }

    private Team buildMonsterTeam(MonsterTemplate tmpl, int count) {
        Team team = new Team(0L, "Monsters");
        for (int j = 0; j < count; j++) {
            List<Skill> monsterSkills = tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()
                    ? skillRepository.findByIds(tmpl.getSkills())
                    : List.of();
            int monsterLevel = tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3);
            monsterLevel = Math.max(1, monsterLevel);
            team.addMember(new Monster(tmpl, monsterLevel, monsterSkills));
        }
        return team;
    }

    // ===================== 战后处理 =====================

    private void applyCombatHpToUser(User user, Team team) {
        for (Combatant c : team.members()) {
            if (c instanceof PlayerCombatant) {
                user.setHpCurrent(c.getHp());
            }
        }
    }

    private void applyCombatHpToBeasts(Team team, User user, boolean playerWon, boolean isHighlightBattle) {
        for (Combatant c : team.members()) {
            if (c instanceof BeastCombatant) {
                Beast beast = beastRepository.findById(c.getId()).orElse(null);
                if (beast != null) {
                    beast.setHpCurrent(c.getHp());
                    if (!c.isAlive()) {
                        beast.setIsDeployed(false);
                        long recoveryMinutes = switch (beast.getQuality()) {
                            case "mortal" -> 30;
                            case "spirit" -> 60;
                            case "immortal" -> 120;
                            case "saint" -> 240;
                            case "divine" -> 480;
                            default -> 30;
                        };
                        beast.setRecoveryUntil(LocalDateTime.now().plusMinutes(recoveryMinutes));

                        if (playerWon) {
                            beastService.tryAwakeningSkill(beast);
                        }
                    } else {
                        if (isHighlightBattle) {
                            beastService.tryAwakeningSkill(beast);
                        }
                    }
                    beastRepository.save(beast);
                }
            }
        }
    }

    private List<Map<String, Object>> processDrops(MonsterTemplate tmpl) {
        List<Map<String, Object>> drops = new ArrayList<>();
        Map<String, Object> dropTable = tmpl.getDropTable();
        if (dropTable == null || dropTable.isEmpty()) return drops;

        @SuppressWarnings("unchecked")
        Map<String, Object> equipmentDrops = (Map<String, Object>) dropTable.get("equipment");
        if (equipmentDrops != null) {
            for (var entry : equipmentDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    itemTemplateRepository.findById(templateId).ifPresent(tmplItem -> {
                        Map<String, Object> drop = new LinkedHashMap<>();
                        drop.put("type", "equipment");
                        drop.put("templateId", templateId);
                        drop.put("name", tmplItem.getName());
                        drop.put("quantity", 1);
                        drops.add(drop);
                    });
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> itemDrops = (Map<String, Object>) dropTable.get("items");
        if (itemDrops != null) {
            for (var entry : itemDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    itemTemplateRepository.findById(templateId).ifPresent(tmplItem -> {
                        int qty = 1 + ThreadLocalRandom.current().nextInt(3);
                        Map<String, Object> drop = new LinkedHashMap<>();
                        drop.put("type", "item");
                        drop.put("templateId", templateId);
                        drop.put("name", tmplItem.getName());
                        drop.put("quantity", qty);
                        drops.add(drop);
                    });
                }
            }
        }

        return drops;
    }

    // ===================== 工具方法 =====================

    private Long weightedSelect(Map<Long, MonsterSpawn> spawnMap) {
        int total = spawnMap.values().stream().mapToInt(MonsterSpawn::weight).sum();
        if (total <= 0) return null;
        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (var entry : spawnMap.entrySet()) {
            cumulative += entry.getValue().weight();
            if (roll < cumulative) return entry.getKey();
        }
        return null;
    }

    private int calculateGearScore(Long userId) {
        List<Equipment> equipped = equipmentRepository.findEquippedByUserId(userId);
        int score = 0;
        for (Equipment equip : equipped) {
            score += equip.getFinalAttack() + equip.getFinalDefense() * 2;
            if (equip.getRarity() != null) {
                score += switch (equip.getRarity().getCode()) {
                    case "common" -> 10;
                    case "uncommon" -> 20;
                    case "rare" -> 40;
                    case "epic" -> 80;
                    case "legendary" -> 160;
                    default -> 0;
                };
            }
        }
        return score;
    }

    private BattleResultVO buildEmptyBattleResult(User user, MapNode mapNode) {
        return BattleResultVO.builder()
                .winner("NONE")
                .rounds(0)
                .expGained(0L)
                .summary(mapNode.getName() + "风平浪静，未遇敌袭")
                .build();
    }

    private BattleResultVO buildSummary(User user, MapNode mapNode,
                                         int totalEncounters, int totalKills, int defeatCount,
                                         long expGained, int totalRounds,
                                         List<Map<String, Object>> allDrops,
                                         List<Map<String, Object>> allLogs,
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
                Map<String, Object> drop = allDrops.get(i);
                summary.append(String.format("%s×%d", drop.get("name"), drop.getOrDefault("quantity", 1)));
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

    private Long toLong(Object value) {
        if (value instanceof Long longVal) return longVal;
        if (value instanceof Integer intVal) return intVal.longValue();
        if (value instanceof Number number) return number.longValue();
        return null;
    }
}
