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
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    private static final long BASE_EXP_PER_MINUTE = 2;

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final EquipmentRepository equipmentRepository;
    private final BeastRepository beastRepository;
    private final AuthenticationService authService;
    private final StackableItemService stackableItemService;
    private final CombatService combatService;
    private final EncounterCalculator encounterCalculator;
    private final HighlightBattleDetector highlightBattleDetector;
    private final PostCombatProcessor postCombatProcessor;
    private final DropProcessor dropProcessor;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<TrainingStartResult> startTraining(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateStatus(platform, openId, UserStatus.IDLE);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(startTraining(auth.userId()));
    }

    public ServiceResult<TrainingRewardVO> endTraining(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateStatus(platform, openId, UserStatus.EXERCISING);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(endTraining(auth.userId()));
    }

    // ===================== 内部 API =====================

    public TrainingRewardVO calculateTrainingRewards(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getTrainingStartTime() == null) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您还没有开始历练")
                    .build();
        }

        long minutesTraining = Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesTraining <= 5) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("历练时间过短毫无收获")
                    .build();
        }

        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("当前地图不存在")
                    .build();
        }

        MapNode mapNode = mapOpt.get();

        double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getStatAgi());
        double levelDecayMultiplier = calculateLevelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
        long expGained = (long) (BASE_EXP_PER_MINUTE * minutesTraining * efficiencyMultiplier * levelDecayMultiplier);
        List<Map<String, Object>> items = calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
        if (expGained > 0) {
            summary.append(String.format("经验: +%d\n", expGained));
        }
        if (!items.isEmpty()) {
            summary.append("物品: ");
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                String name = (String) item.get("name");
                Integer quantity = (Integer) item.get("quantity");
                summary.append(String.format("%s x%d", name, quantity));
                if (i < items.size() - 1) {
                    summary.append(", ");
                }
            }
        }

        log.info(
                "用户 {} 历练奖励计算 - 时长: {} 分钟, 经验: {}, 物品数: {}",
                userId, minutesTraining, expGained, items.size()
        );

        return TrainingRewardVO.builder()
                .userId(userId)
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .durationMinutes(minutesTraining)
                .efficiencyMultiplier(efficiencyMultiplier)
                .levelDecayMultiplier(levelDecayMultiplier)
                .exp(expGained)
                .items(items)
                .summary(summary.toString())
                .build();
    }

    public TrainingStartResult startTraining(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

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
        userRepository.save(user);

        log.info("用户 {} 开始在 {} 历练", userId, mapName);
        return TrainingStartResult.builder()
                .success(true)
                .mapName(mapName)
                .build();
    }

    // ===================== 遭遇战斗 =====================

    private static final int DEFAULT_MAX_ROUNDS = 20;

    public TrainingRewardVO endTraining(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getTrainingStartTime() == null) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您当前没有在历练")
                    .build();
        }

        long minutesTraining = Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesTraining <= 5) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("历练时间过短毫无收获")
                    .build();
        }

        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("当前地图不存在")
                    .build();
        }
        MapNode mapNode = mapOpt.get();

        double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getStatAgi());
        double levelDecayMultiplier = calculateLevelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
        long baseExp = (long) (BASE_EXP_PER_MINUTE * minutesTraining * efficiencyMultiplier * levelDecayMultiplier);
        List<Map<String, Object>> trainingItems = calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

        BattleResultVO battleResult = simulateTraining(userId, (int) minutesTraining);
        long combatExp = battleResult.expGained();
        long totalExp = baseExp + combatExp;

        user = userRepository.findById(userId).orElseThrow();
        if (totalExp > 0) {
            user.addExp(totalExp);
        }

        addTrainingItemsToInventory(userId, trainingItems);

        user.setStatus(UserStatus.IDLE);
        user.setTrainingStartTime(null);
        userRepository.save(user);

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
        User user = userRepository.findById(userId).orElseThrow();
        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

        Map<Long, MonsterSpawn> monsterEncounters = mapNode.getMonsterEncounters();
        if (monsterEncounters == null || monsterEncounters.isEmpty()) {
            return buildEmptyBattleResult(mapNode);
        }

        var encounterParams = computeEncounterParams(userId, user, mapNode, durationMinutes);

        long expGained = 0L;
        List<DropItem> allDrops = new ArrayList<>();
        List<Map<String, Object>> allLogs = new ArrayList<>();
        List<Map<String, Object>> allSkillProcs = new ArrayList<>();
        int totalRounds = 0;
        int totalEncounters = 0;
        int totalKills = 0;
        int defeatCount = 0;

        Map<Long, Beast> beastCache = new LinkedHashMap<>();

        for (int i = 0; i < encounterParams.encounterChances(); i++) {
            if (ThreadLocalRandom.current().nextDouble() >= encounterParams.encounterChance()) continue;

            Long selectedTemplateId = weightedSelect(monsterEncounters);
            if (selectedTemplateId == null) continue;

            MonsterSpawn spawn = monsterEncounters.get(selectedTemplateId);
            MonsterTemplate tmpl = monsterTemplateRepository.findById(selectedTemplateId).orElse(null);
            if (tmpl == null) continue;

            totalEncounters++;
            int count = spawn.min() + ThreadLocalRandom.current().nextInt(spawn.max() - spawn.min() + 1);

            Team playerTeam = combatService.buildPlayerTeam(user);
            Team monsterTeam = combatService.buildMonsterTeam(tmpl, count);

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
                userRepository.save(user);
                saveBeastCache(beastCache);
                return buildSummary(user, mapNode, totalEncounters, totalKills, defeatCount,
                        0, totalRounds, List.of(), allLogs, allSkillProcs);
            }
        }

        if (expGained > 0) {
            user.addExp(expGained);
        }
        userRepository.save(user);
        saveBeastCache(beastCache);

        if (!allDrops.isEmpty()) {
            dropProcessor.distributeDrops(userId, allDrops);
        }

        return buildSummary(user, mapNode, totalEncounters, totalKills, defeatCount,
                expGained, totalRounds, allDrops, allLogs, allSkillProcs);
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

    private record EncounterParams(int encounterChances, double encounterChance) {
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

    private BattleResultVO buildEmptyBattleResult(MapNode mapNode) {
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
                                         List<DropItem> allDrops,
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

    // ===================== 基础历练收益 =====================

    private double calculateEfficiencyMultiplier(int agility) {
        return 1.0 + (agility * 0.01);
    }

    private double calculateLevelDecayMultiplier(int playerLevel, int mapLevel) {
        int levelDiff = playerLevel - mapLevel - 10;
        if (levelDiff <= 0) {
            return 1.0;
        }
        double decay = levelDiff * 0.05;
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
            int totalWeight = specialties.values().stream().mapToInt(Integer::intValue).sum();
            if (totalWeight == 0) continue;
            int roll = ThreadLocalRandom.current().nextInt(totalWeight);
            int cumulative = 0;
            Long selectedTemplateId = null;
            for (Map.Entry<Long, Integer> entry : specialties.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selectedTemplateId = entry.getKey();
                    break;
                }
            }
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
}
