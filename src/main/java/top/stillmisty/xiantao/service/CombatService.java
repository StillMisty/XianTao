package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterSpawn;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.BeastCombatant;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.EncounterCalculator;
import top.stillmisty.xiantao.domain.monster.HighlightBattleDetector;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.FudiService;

import java.time.LocalDateTime;
import java.util.*;
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
    private final ItemService itemService;
    private final AuthenticationService authService;
    private final CombatEngine combatEngine;
    private final EncounterCalculator encounterCalculator;
    private final HighlightBattleDetector highlightBattleDetector;
    private final FudiService fudiService;
    private final PlayerBuffRepository playerBuffRepository;

    public BattleResultVO simulate(Team teamA, Team teamB, int maxRounds) {
        BattleContext context = BattleContext.builder()
                .teamA(teamA)
                .teamB(teamB)
                .maxRounds(maxRounds)
                .scene(BattleContext.BattleScene.TRAINING)
                .build();
        return combatEngine.simulate(context);
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

        // 计算装备评分
        int gearScore = calculateGearScore(userId);

        // 动态计算遇怪间隔
        double encounterInterval = encounterCalculator.calculateInterval(
                mapNode.getLevelRequirement(),
                user.getLevel(),
                gearScore
        );

        // 计算遇怪次数
        int encounterChances = encounterCalculator.calculateEncounterChances(durationMinutes, encounterInterval);

        // 计算遇怪概率
        double encounterChance = encounterCalculator.calculateEncounterChance(encounterInterval);

        // 高光战斗检测
        List<BattleResultVO> battleResults = new ArrayList<>();
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

            // 构建玩家队伍
            Team playerTeam = buildPlayerTeam(user);

            // 构建怪物队伍
            Team monsterTeam = new Team(0L, "Monsters");
            for (int j = 0; j < count; j++) {
                List<Skill> monsterSkills = tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()
                        ? skillRepository.findByIds(tmpl.getSkills())
                        : List.of();
                int monsterLevel = tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3);
                monsterLevel = Math.max(1, monsterLevel);
                Monster monster = new Monster(tmpl, monsterLevel, monsterSkills);
                monsterTeam.addMember(monster);
            }

            BattleResultVO result = simulate(playerTeam, monsterTeam, DEFAULT_MAX_ROUNDS);
            battleResults.add(result);
            totalRounds += result.rounds();

            // 统计击杀数
            if ("Player".equals(result.winner())) {
                totalKills += count;
            } else {
                defeatCount++;
            }

            // 检测高光战斗
            HighlightBattleDetector.HighlightInfo highlightInfo = highlightBattleDetector.detectHighlight(result, totalEncounters);
            if (highlightInfo != null) {
                highlightInfos.add(highlightInfo);
            }

            // 合并日志
            if (result.combatLog() != null) allLogs.addAll(result.combatLog());
            if (result.skillProcs() != null) allSkillProcs.addAll(result.skillProcs());

            // 处理掉落
            boolean playerWon = "Player".equals(result.winner());
            if (playerWon) {
                expGained += tmpl.getBaseLevel() * 10L * count;
                List<Map<String, Object>> drops = processDrops(tmpl, user);
                allDrops.addAll(drops);
            }

            // 应用战斗后HP到用户和灵兽
            boolean isHighlightBattle = highlightInfo != null;
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
                if (templateId != null) itemService.createEquipment(userId, templateId);
            } else if ("item".equals(type)) {
                Long templateId = toLong(drop.get("templateId"));
                String name = (String) drop.get("name");
                Integer quantity = (Integer) drop.getOrDefault("quantity", 1);
                if (templateId != null && name != null && quantity != null) {
                    itemService.addStackableItem(userId, templateId, ItemType.MATERIAL, name, quantity);
                }
            }
        }

        userRepository.save(user);

        // 构建简洁的统计摘要
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

    private Team buildPlayerTeam(User user) {
        Team team = new Team(user.getId(), "Player");

        // 读取玩家活跃增益Buff
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
                // 加载灵兽技能
                List<Skill> beastSkills = List.of();
                if (beast.getSkills() != null && !beast.getSkills().isEmpty()) {
                    beastSkills = skillRepository.findByIds(beast.getSkills());
                }
                team.addMember(new BeastCombatant(beast, beastSkills));
            }
        }
        return team;
    }

    private void applyCombatHpToUser(User user, Team team) {
        for (Combatant c : team.members()) {
            if (c instanceof PlayerCombatant pc) {
                user.setHpCurrent(c.getHp());
            }
        }
    }

    private void applyCombatHpToBeasts(Team team, User user, boolean playerWon, boolean isHighlightBattle) {
        for (Combatant c : team.members()) {
            if (c instanceof BeastCombatant bc) {
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
                        
                        // 战斗觉醒判定：灵兽HP降至0但战斗最终胜利
                        if (playerWon) {
                            tryAwakeningSkill(beast);
                        }
                    } else {
                        // 灵兽存活，检查是否是高光战斗
                        if (isHighlightBattle) {
                            tryAwakeningSkill(beast);
                        }
                    }
                    beastRepository.save(beast);
                }
            }
        }
    }

    /**
     * 尝试解锁后天悟（战斗觉醒）
     * @param beast 灵兽实体
     * @return 是否成功觉醒
     */
    private boolean tryAwakeningSkill(Beast beast) {
        // 这里需要调用FudiService的方法，但为了避免循环依赖，我们直接实现
        // 15%概率觉醒
        if (new Random().nextInt(100) >= 15) {
            return false;
        }
        // 获取灵兽模板的技能池配置
        // 这里简化处理，暂时不实现具体逻辑
        log.info("灵兽 {} 尝试战斗觉醒", beast.getBeastName());
        return true;
    }

    private List<Map<String, Object>> processDrops(MonsterTemplate tmpl, User user) {
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

    /**
     * 计算玩家装备评分
     */
    private int calculateGearScore(Long userId) {
        List<Equipment> equipped = equipmentRepository.findEquippedByUserId(userId);
        int score = 0;
        for (Equipment equip : equipped) {
            // 基础评分：攻击力 + 防御力 * 2
            score += equip.getFinalAttack() + equip.getFinalDefense() * 2;
            // 稀有度加成
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

    private Long toLong(Object value) {
        if (value instanceof Long longVal) return longVal;
        if (value instanceof Integer intVal) return intVal.longValue();
        if (value instanceof Number number) return number.longValue();
        return null;
    }

    // ===================== Inner Classes =====================

    static class PlayerCombatant implements Combatant {
        private final User user;
        private final Equipment weapon;
        private int hp;
        private int attackBuff;
        private int defenseBuff;
        private int speedBuff;

        PlayerCombatant(User user, EquipmentRepository equipmentRepository,
                        EquipmentTemplateRepository equipmentTemplateRepository) {
            this.user = user;
            this.hp = user.getHpCurrent() != null ? user.getHpCurrent() : user.calculateMaxHp();

            Equipment equippedWeapon = equipmentRepository.findEquippedByUserId(user.getId()).stream()
                    .filter(e -> e.getSlot() == EquipmentSlot.WEAPON)
                    .findFirst().orElse(null);
            this.weapon = equippedWeapon;
        }

        PlayerCombatant withBuffs(int attackBuff, int defenseBuff, int speedBuff) {
            this.attackBuff = attackBuff;
            this.defenseBuff = defenseBuff;
            this.speedBuff = speedBuff;
            return this;
        }

        @Override
        public Long getId() {
            return user.getId();
        }

        @Override
        public String getName() {
            return user.getNickname();
        }

        @Override
        public int getSpeed() {
            return user.getStatAgi() * 2 + 10 + speedBuff;
        }

        @Override
        public int getAttack() {
            int statValue = user.getStatStr();
            int equipAttack = 0;
            if (weapon != null) {
                equipAttack = weapon.getFinalAttack();
            }
            return statValue * 2 + equipAttack + attackBuff;
        }

        @Override
        public int getDefense() {
            return user.getStatCon() + defenseBuff;
        }

        @Override
        public int getHp() {
            return hp;
        }

        @Override
        public int getMaxHp() {
            return user.calculateMaxHp();
        }

        @Override
        public void takeDamage(int amount) {
            hp = Math.max(0, hp - amount);
        }

        @Override
        public boolean isAlive() {
            return hp > 0;
        }

        @Override
        public List<Skill> getSkills() {
            return List.of();
        }

        WeaponType getWeaponType() {
            return weapon != null ? weapon.getWeaponType() : null;
        }

        int getWis() {
            return user.getStatWis();
        }
    }
}
