package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterSpawn;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingStartResult;
import top.stillmisty.xiantao.domain.monster.EncounterCalculator;
import top.stillmisty.xiantao.domain.monster.HighlightBattleDetector;
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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final long BASE_EXP_PER_MINUTE = 2;
    private static final int DEFAULT_MAX_ROUNDS = 20;
    private final UserStateService userStateService;
    private final MapNodeRepository mapNodeRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final EquipmentRepository equipmentRepository;
    private final BeastRepository beastRepository;
    private final StackableItemService stackableItemService;
    private final CombatService combatService;
    private final EncounterCalculator encounterCalculator;
    private final HighlightBattleDetector highlightBattleDetector;
    private final PostCombatProcessor postCombatProcessor;
    private final DropProcessor dropProcessor;

    // ===================== 公开 API（含认证） =====================
    private final SkillRepository skillRepository;

    @Authenticated
    @Transactional
    public ServiceResult<TrainingStartResult> startTraining(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(startTraining(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    @Authenticated
    @Transactional
    public ServiceResult<TrainingRewardVO> endTraining(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(endTraining(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 遭遇战斗 =====================

    @Transactional
    public TrainingStartResult startTraining(Long userId) {
        User user = userStateService.getUser(userId);

        if (user.getStatus() != UserStatus.IDLE) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法开始历练（需要 空闲 状态）");
        }

        if (user.getLocationId() == null) {
            return TrainingStartResult.builder()
                    .success(false)
                    .message("当前位置无效，无法开始历练")
                    .build();
        }

        var mapName = mapNodeRepository.findById(user.getLocationId())
                .map(MapNode::getName)
                .orElse(null);

        if (mapName == null) {
            return TrainingStartResult.builder()
                    .success(false)
                    .message("当前地图不存在，无法开始历练")
                    .build();
        }

        user.setTrainingStartTime(LocalDateTime.now());
        user.setStatus(UserStatus.EXERCISING);
        userStateService.save(user);

        log.info("用户 {} 开始在 {} 历练", userId, mapName);
        return TrainingStartResult.builder()
                .success(true)
                .mapName(mapName)
                .build();
    }

    @Transactional
    public TrainingRewardVO endTraining(Long userId) {
        User user = userStateService.getUser(userId);

        if (user.getStatus() != UserStatus.EXERCISING) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法结束历练（需要 历练 状态）");
        }

        if (user.getTrainingStartTime() == null) {
            user.setStatus(UserStatus.IDLE);
            userStateService.save(user);
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您当前没有在历练")
                    .build();
        }

        long minutesTraining = Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesTraining <= 5) {
            user.setStatus(UserStatus.IDLE);
            user.setTrainingStartTime(null);
            userStateService.save(user);
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("历练时间过短毫无收获")
                    .build();
        }

        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            user.setStatus(UserStatus.IDLE);
            user.setTrainingStartTime(null);
            userStateService.save(user);
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("当前地图不存在")
                    .build();
        }
        MapNode mapNode = mapOpt.get();

        double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getEffectiveStatAgi());
        double levelDecayMultiplier = calculateLevelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
        long baseExp = (long) (BASE_EXP_PER_MINUTE * minutesTraining * efficiencyMultiplier * levelDecayMultiplier);
        List<Map<String, Object>> trainingItems = calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

        BattleResultVO battleResult = simulateTraining(userId, (int) minutesTraining);
        long combatExp = battleResult.expGained();
        long totalExp = baseExp + combatExp;

        user = userStateService.getUser(userId);
        if (totalExp > 0) {
            user.addExp(totalExp);
        }

        addTrainingItemsToInventory(userId, trainingItems);

        user.setStatus(UserStatus.IDLE);
        user.setTrainingStartTime(null);
        userStateService.save(user);

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
        if (totalExp > 0) {
            summary.append(String.format("经验: +%d\n", totalExp));
        }
        if (battleResult.summary() != null) {
            summary.append(battleResult.summary()).append("\n");
        }
        if (!trainingItems.isEmpty()) {
            summary.append("物品:\n");
            for (Map<String, Object> item : trainingItems) {
                String name = (String) item.get("name");
                Integer qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
                summary.append(String.format("  %s x%d\n", name, qty));
            }
        }

        log.info("用户 {} 结束历练并应用奖励", userId);
        return TrainingRewardVO.builder()
                .userId(userId)
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .durationMinutes(minutesTraining)
                .efficiencyMultiplier(efficiencyMultiplier)
                .levelDecayMultiplier(levelDecayMultiplier)
                .exp(totalExp)
                .items(trainingItems)
                .summary(summary.toString())
                .build();
    }

    // ===================== 遭遇编排（核心） =====================

    private BattleResultVO simulateTraining(Long userId, int durationMinutes) {
        User user = userStateService.getUser(userId);
        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

        Map<Long, MonsterSpawn> monsterEncounters = mapNode.getMonsterEncounters();
        if (monsterEncounters == null || monsterEncounters.isEmpty()) {
            return buildEmptyBattleResult(mapNode);
        }

        var encounterParams = computeEncounterParams(userId, user, mapNode, durationMinutes);

        long expGained = 0L;
        List<DropItem> allDrops = new ArrayList<>();
        List<CombatLogEntry> allLogs = new ArrayList<>();
        List<Map<String, Object>> allSkillProcs = new ArrayList<>();
        int totalRounds = 0;
        int totalEncounters = 0;
        int totalKills = 0;
        int defeatCount = 0;

        Map<Long, Beast> beastCache = new LinkedHashMap<>();

        // 预加载怪物模板，避免 N+1 查询
        var templateIds = new ArrayList<>(monsterEncounters.keySet());
        Map<Long, MonsterTemplate> templateMap = monsterTemplateRepository.findByIds(templateIds).stream()
                .collect(Collectors.toMap(MonsterTemplate::getId, t -> t));

        // 预加载所有相关技能，避免 N+1 查询
        var skillIds = templateMap.values().stream()
                .flatMap(t -> t.getSkills() != null ? t.getSkills().stream() : java.util.stream.Stream.of())
                .distinct()
                .toList();
        Map<Long, top.stillmisty.xiantao.domain.skill.entity.Skill> skillMap;
        if (!skillIds.isEmpty()) {
            skillMap = skillRepository.findByIds(skillIds).stream()
                    .collect(Collectors.toMap(Skill::getId, s -> s));
        } else {
            skillMap = Map.of();
        }

        for (int i = 0; i < encounterParams.encounterChances(); i++) {
            if (ThreadLocalRandom.current().nextDouble() >= encounterParams.encounterChance()) continue;

            Long selectedTemplateId = weightedSelect(monsterEncounters);
            if (selectedTemplateId == null) continue;

            MonsterSpawn spawn = monsterEncounters.get(selectedTemplateId);
            MonsterTemplate tmpl = templateMap.get(selectedTemplateId);
            if (tmpl == null) continue;

            totalEncounters++;
            int count = spawn.min() + ThreadLocalRandom.current().nextInt(spawn.max() - spawn.min() + 1);

            Team playerTeam = combatService.buildPlayerTeam(user);
            Team monsterTeam = buildMonsterTeamWithSkills(tmpl, count, skillMap);

            BattleResultVO result = combatService.simulate(playerTeam, monsterTeam, DEFAULT_MAX_ROUNDS);
            totalRounds += result.rounds();
            if (result.combatLog() != null) allLogs.addAll(result.combatLog());
            if (result.skillProcs() != null) allSkillProcs.addAll(result.skillProcs());

            boolean playerWon = result.winner().equals("Player");
            if (playerWon) {
                totalKills += count;
                expGained += tmpl.getBaseLevel() * 10L * count;
                allDrops.addAll(dropProcessor.processMonsterDrops(tmpl));
            } else {
                defeatCount++;
            }

            HighlightBattleDetector.HighlightInfo highlightInfo =
                    highlightBattleDetector.detectHighlight(result, totalEncounters);
            boolean isHighlightBattle = highlightInfo != null;

            postCombatProcessor.applyHpToUser(user, playerTeam);
            postCombatProcessor.applyHpToBeasts(playerTeam, user, playerWon, isHighlightBattle, beastCache);

            if (!playerWon && playerTeam.isAllDead()) {
                user.setDying();
                userStateService.save(user);
                saveBeastCache(beastCache);
                return buildSummary(
                        user, mapNode, totalEncounters, totalKills, defeatCount,
                        0, totalRounds, List.of(), allLogs, allSkillProcs
                );
            }
        }

        if (expGained > 0) {
            user.addExp(expGained);
        }
        userStateService.save(user);
        saveBeastCache(beastCache);

        if (!allDrops.isEmpty()) {
            dropProcessor.distributeDrops(userId, allDrops);
        }

        return buildSummary(
                user, mapNode, totalEncounters, totalKills, defeatCount,
                expGained, totalRounds, allDrops, allLogs, allSkillProcs
        );
    }

    // ===================== 遭遇计算 =====================

    private EncounterParams computeEncounterParams(Long userId, User user, MapNode mapNode, int durationMinutes) {
        int gearScore = calculateGearScore(userId);
        double interval = encounterCalculator.calculateInterval(
                mapNode.getLevelRequirement(), user.getLevel(), gearScore);
        int chances = encounterCalculator.calculateEncounterChances(durationMinutes, interval);
        double chance = encounterCalculator.calculateEncounterChance(interval);
        return new EncounterParams(chances, chance);
    }

    private Team buildMonsterTeamWithSkills(MonsterTemplate tmpl, int count, Map<Long, Skill> skillMap) {
        Team team = new Team(0L, "Monsters");
        for (int j = 0; j < count; j++) {
            List<Skill> monsterSkills = tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()
                    ? tmpl.getSkills().stream().map(skillMap::get).filter(Objects::nonNull).toList()
                    : List.of();
            int monsterLevel = tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3);
            monsterLevel = Math.max(1, monsterLevel);
            team.addMember(new Monster(tmpl, monsterLevel, monsterSkills));
        }
        return team;
    }

    // ===================== 辅助方法 =====================

    private void saveBeastCache(Map<Long, Beast> beastCache) {
        for (Beast beast : beastCache.values()) {
            beastRepository.save(beast);
        }
    }

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
            score += switch (equip.getRarity()) {
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
            User user, MapNode mapNode,
            int totalEncounters, int totalKills, int defeatCount,
            long expGained, int totalRounds,
            List<DropItem> allDrops,
            List<CombatLogEntry> allLogs,
            List<Map<String, Object>> allSkillProcs
    ) {
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

    private double calculateEfficiencyMultiplier(int agility) {
        return 1.0 + (agility * 0.01);
    }

    // ===================== 基础历练收益 =====================

    private double calculateLevelDecayMultiplier(int playerLevel, int mapLevel) {
        int levelDiff = playerLevel - mapLevel - 15;
        if (levelDiff <= 0) {
            return 1.0;
        }
        double decay = levelDiff * 0.04;
        return Math.max(0.1, 1.0 - decay);
    }

    private List<Map<String, Object>> calculateItemsReward(long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
        List<Map<String, Object>> items = new ArrayList<>();
        var specialties = mapNode.getSpecialties();
        if (specialties == null || specialties.isEmpty()) return items;

        Map<Long, ItemTemplate> templateMap = itemTemplateRepository.findByIds(new ArrayList<>(specialties.keySet()))
                .stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

        int dropChances = (int) (minutesTraining / 10.0 * efficiencyMultiplier);

        for (int i = 0; i < dropChances; i++) {
            Long selectedTemplateId = TypeUtils.weightedRandomSelect(specialties, ThreadLocalRandom.current());
            if (selectedTemplateId == null) continue;

            int quantity = ThreadLocalRandom.current().nextInt(3) + 1;

            boolean exists = false;
            for (Map<String, Object> existing : items) {
                if (Objects.equals(existing.get("templateId"), selectedTemplateId)) {
                    existing.put("quantity", (Integer) existing.get("quantity") + quantity);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ItemTemplate template = templateMap.get(selectedTemplateId);
                String name = template != null ? template.getName() : "未知物品";
                Map<String, Object> item = new HashMap<>();
                item.put("templateId", selectedTemplateId);
                item.put("name", name);
                item.put("quantity", quantity);
                items.add(item);
            }
        }

        return items;
    }

    private void addTrainingItemsToInventory(Long userId, List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) return;
        for (Map<String, Object> item : items) {
            Long templateId = TypeUtils.toLong(item.get("templateId"));
            if (templateId == null) continue;
            String name = (String) item.get("name");
            int quantity = ((Number) item.getOrDefault("quantity", 1)).intValue();
            ItemType itemType = itemTemplateRepository.findById(templateId)
                    .map(ItemTemplate::getType)
                    .orElse(ItemType.MATERIAL);
            stackableItemService.addStackableItem(userId, templateId, itemType, name, quantity);
        }
    }

    private record EncounterParams(int encounterChances, double encounterChance) {
    }
}
