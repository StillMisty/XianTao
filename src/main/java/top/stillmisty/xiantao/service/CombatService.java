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
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CombatService {

    private static final int DEFAULT_MAX_ROUNDS = 20;
    private static final double BASE_ENCOUNTER_CHANCE_PER_10MIN = 0.4;

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

    public BattleResultVO simulate(Team teamA, Team teamB, int maxRounds) {
        int round = 0;
        List<Map<String, Object>> combatLog = new ArrayList<>();
        Map<String, Integer> damageDealt = new LinkedHashMap<>();
        Map<String, Integer> skillProcs = new LinkedHashMap<>();
        Map<String, Integer> skillCooldowns = new LinkedHashMap<>();

        Map<String, Integer> initialHpA = new LinkedHashMap<>();
        for (Combatant c : teamA.members()) {
            initialHpA.put("member_" + c.getId(), c.getHp());
        }
        Map<String, Integer> initialHpB = new LinkedHashMap<>();
        for (Combatant c : teamB.members()) {
            initialHpB.put("member_" + c.getId(), c.getHp());
        }

        String winner = "DRAW";
        while (round < maxRounds) {
            round++;

            // 按speed降序排列
            List<Combatant> allAlive = new ArrayList<>();
            allAlive.addAll(teamA.aliveMembers());
            allAlive.addAll(teamB.aliveMembers());
            allAlive.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());

            int sequence = 0;
            for (Combatant attacker : allAlive) {
                if (!attacker.isAlive()) continue;

                Team attackerTeam = teamA.members().contains(attacker) ? teamA : teamB;
                Team defenderTeam = attackerTeam == teamA ? teamB : teamA;

                sequence++;

                // 尝试法决触发
                int cooldownRemaining = skillCooldowns.getOrDefault("skill_" + attacker.getId(), 0);
                boolean skillTriggered = false;
                Skill triggeredSkill = null;

                if (cooldownRemaining <= 0) {
                    List<Skill> skills = attacker.getSkills();
                    if (skills != null && !skills.isEmpty()) {
                        for (Skill skill : skills) {
                            if (skill.getEffectType() != EffectType.DAMAGE && skill.getEffectType() != EffectType.MULTI_HIT)
                                continue;
                            triggeredSkill = skill;
                            skillTriggered = true;
                            skillCooldowns.put("skill_" + attacker.getId(), skill.getCooldownSeconds());
                            String key = attacker.getName() + ":" + skill.getName();
                            skillProcs.merge(key, 1, Integer::sum);
                            break;
                        }
                    }
                }

                // 普通攻击（无技能时）或技能攻击
                Combatant defender = defenderTeam.selectTargetForPVE();
                if (defender == null) break;

                int hpBefore = defender.getHp();
                int damage;

                if (skillTriggered && triggeredSkill != null) {
                    damage = calculateSkillDamage(attacker, defender, triggeredSkill);
                } else {
                    damage = calculateNormalDamage(attacker, defender);
                }

                // 连击
                int multiplier = 1;
                if (triggeredSkill != null && triggeredSkill.getEffectType() == EffectType.MULTI_HIT) {
                    multiplier = 3;
                    damage *= multiplier;
                }

                defender.takeDamage(damage);
                int hpAfter = defender.getHp();
                boolean isKill = hpAfter <= 0;

                damageDealt.merge(attacker.getName(), damage, Integer::sum);

                Map<String, Object> logEntry = new LinkedHashMap<>();
                logEntry.put("round", round);
                logEntry.put("sequence", sequence);
                logEntry.put("attackerName", attacker.getName());
                logEntry.put("defenderName", defender.getName());
                logEntry.put("attackType", skillTriggered ? "SKILL" : "NORMAL");
                if (skillTriggered && triggeredSkill != null) {
                    logEntry.put("skillName", triggeredSkill.getName());
                }
                logEntry.put("damageDealt", damage);
                logEntry.put("isCrit", false);
                logEntry.put("defenderHpBefore", hpBefore);
                logEntry.put("defenderHpAfter", hpAfter);
                logEntry.put("isKill", isKill);
                logEntry.put("multiplier", multiplier);
                combatLog.add(logEntry);

                // 更新冷却
                for (var entry : new HashMap<>(skillCooldowns).entrySet()) {
                    int cd = entry.getValue() - 1;
                    if (cd <= 0) {
                        skillCooldowns.remove(entry.getKey());
                    } else {
                        skillCooldowns.put(entry.getKey(), cd);
                    }
                }

                if (defenderTeam.isAllDead()) break;
            }

            if (teamB.isAllDead()) {
                winner = teamA.name();
                break;
            }
            if (teamA.isAllDead()) {
                winner = teamB.name();
                break;
            }
        }

        Map<String, Object> playerHpChange = new LinkedHashMap<>();
        for (Combatant c : teamA.members()) {
            Integer initial = initialHpA.get("member_" + c.getId());
            if (initial != null) {
                playerHpChange.put(c.getName(), Map.of("before", initial, "after", c.getHp()));
            }
        }

        List<Map<String, Object>> beastHpChanges = new ArrayList<>();
        for (Combatant c : teamA.members()) {
            if (c instanceof BeastCombatant) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("name", c.getName());
                change.put("after", c.getHp());
                beastHpChanges.add(change);
            }
        }

        List<Map<String, Object>> monsterHpChanges = new ArrayList<>();
        for (Combatant c : teamB.members()) {
            Map<String, Object> change = new LinkedHashMap<>();
            change.put("name", c.getName());
            change.put("after", c.getHp());
            monsterHpChanges.add(change);
        }

        List<Map<String, Object>> damageList = new ArrayList<>();
        for (var entry : damageDealt.entrySet()) {
            Map<String, Object> dmg = new LinkedHashMap<>();
            dmg.put("name", entry.getKey());
            dmg.put("total", entry.getValue());
            damageList.add(dmg);
        }

        List<Map<String, Object>> skillProcList = new ArrayList<>();
        for (var entry : skillProcs.entrySet()) {
            Map<String, Object> proc = new LinkedHashMap<>();
            proc.put("key", entry.getKey());
            proc.put("count", entry.getValue());
            skillProcList.add(proc);
        }

        return BattleResultVO.builder()
                .winner(winner)
                .rounds(round)
                .playerHpChange(playerHpChange)
                .beastHpChanges(beastHpChanges)
                .monsterHpChanges(monsterHpChanges)
                .damageDealt(damageList)
                .skillProcs(skillProcList)
                .combatLog(combatLog)
                .build();
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

        Map<Long, Integer> monsterEncounters = mapNode.getMonsterEncounters();
        if (monsterEncounters == null || monsterEncounters.isEmpty()) {
            return buildEmptyBattleResult(user, mapNode);
        }

        int encounterChances = durationMinutes / 10;
        Map<String, Object> encounterSize = mapNode.getEncounterSize();
        int minCount = 1, maxCount = 3;
        if (encounterSize != null) {
            if (encounterSize.get("min") instanceof Number minN) minCount = minN.intValue();
            if (encounterSize.get("max") instanceof Number maxN) maxCount = maxN.intValue();
        }

        for (int i = 0; i < encounterChances; i++) {
            if (ThreadLocalRandom.current().nextDouble() >= BASE_ENCOUNTER_CHANCE_PER_10MIN) continue;

            Long selectedTemplateId = weightedSelect(monsterEncounters);
            if (selectedTemplateId == null) continue;

            MonsterTemplate tmpl = monsterTemplateRepository.findById(selectedTemplateId).orElse(null);
            if (tmpl == null) continue;

            totalEncounters++;
            int count = minCount + ThreadLocalRandom.current().nextInt(maxCount - minCount + 1);

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
            totalRounds += result.rounds();

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
            applyCombatHpToUser(user, playerTeam);
            applyCombatHpToBeasts(playerTeam, user);

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

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("历练%d分钟，遭遇%d波怪物\n", durationMinutes, totalEncounters));
        if (expGained > 0) summary.append(String.format("经验: +%d\n", expGained));
        if (!allDrops.isEmpty()) {
            summary.append("战利品: ");
            for (int i = 0; i < allDrops.size(); i++) {
                Map<String, Object> drop = allDrops.get(i);
                summary.append(String.format("%s x%d", drop.get("name"), drop.getOrDefault("quantity", 1)));
                if (i < allDrops.size() - 1) summary.append(", ");
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
        team.addMember(new PlayerCombatant(user, equipmentRepository, equipmentTemplateRepository));
        List<Beast> deployed = beastRepository.findDeployedByUserId(user.getId());
        int beastLimit = Math.min(3, user.getLevel() / 5 + 1);
        for (int i = 0; i < Math.min(deployed.size(), beastLimit); i++) {
            Beast beast = deployed.get(i);
            if (beast.canFight()) {
                team.addMember(new BeastCombatant(beast));
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

    private void applyCombatHpToBeasts(Team team, User user) {
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
                    }
                    beastRepository.save(beast);
                }
            }
        }
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

    int calculateNormalDamage(Combatant attacker, Combatant defender) {
        double advantageMultiplier = 1.0;
        if (attacker instanceof PlayerCombatant pc && defender instanceof Monster monster) {
            advantageMultiplier = getWeaponTypeAdvantage(pc.getWeaponType(), monster.getMonsterType());
        }
        int rawDamage = (int) Math.round(attacker.getAttack() * advantageMultiplier);
        int reduction = (int) Math.round(defender.getDefense() * 0.3);
        return Math.max(1, rawDamage - reduction);
    }

    int calculateSkillDamage(Combatant attacker, Combatant defender, Skill skill) {
        String formula = skill.getDamageFormula();
        if (formula == null) return calculateNormalDamage(attacker, defender);

        double multiplier = skill.getPowerMultiplier() != null ? skill.getPowerMultiplier() : 1.0;
        double advantageMultiplier = 1.0;
        if (attacker instanceof PlayerCombatant pc && defender instanceof Monster monster) {
            advantageMultiplier = getWeaponTypeAdvantage(pc.getWeaponType(), monster.getMonsterType());
        }

        int baseDmg;
        if (attacker instanceof PlayerCombatant pc) {
            baseDmg = evaluateFormula(formula, pc.getWis());
        } else {
            baseDmg = (int) Math.round(attacker.getAttack() * 1.5);
        }

        int rawDamage = (int) Math.round(baseDmg * multiplier * advantageMultiplier);
        int reduction = (int) Math.round(defender.getDefense() * 0.3);
        return Math.max(1, rawDamage - reduction);
    }

    private int evaluateFormula(String formula, int wis) {
        try {
            String expr = formula.replace("wis", String.valueOf(wis)).replaceAll("\\s+", "");
            return evaluateExpression(expr);
        } catch (Exception e) {
            return 10;
        }
    }

    private int evaluateExpression(String expr) {
        String[] parts = expr.split("\\+");
        int result = 0;
        for (String part : parts) {
            part = part.trim();
            if (part.contains("*")) {
                String[] mulParts = part.split("\\*");
                int product = 1;
                for (String mp : mulParts) {
                    product *= Integer.parseInt(mp.trim());
                }
                result += product;
            } else {
                result += Integer.parseInt(part);
            }
        }
        return result;
    }

    static double getWeaponTypeAdvantage(WeaponType weaponType, MonsterType monsterType) {
        if (weaponType == null || monsterType == null) return 1.0;
        Map<WeaponType, MonsterType> advantageMap = Map.of(
                WeaponType.BLADE, MonsterType.BEAST,
                WeaponType.SWORD, MonsterType.SPIRIT,
                WeaponType.AXE, MonsterType.ARMORED,
                WeaponType.SPEAR, MonsterType.WILD_BEAST,
                WeaponType.STAFF, MonsterType.EVIL,
                WeaponType.BOW, MonsterType.FLYING
        );
        MonsterType advantaged = advantageMap.get(weaponType);
        if (advantaged == monsterType) return 1.5;
        return 1.0;
    }

    private <T> T weightedSelect(Map<T, Integer> weightMap) {
        int total = weightMap.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return null;
        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (var entry : weightMap.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }
        return null;
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

        PlayerCombatant(User user, EquipmentRepository equipmentRepository,
                        EquipmentTemplateRepository equipmentTemplateRepository) {
            this.user = user;
            this.hp = user.getHpCurrent() != null ? user.getHpCurrent() : user.calculateMaxHp();

            Equipment equippedWeapon = equipmentRepository.findEquippedByUserId(user.getId()).stream()
                    .filter(e -> e.getSlot() == EquipmentSlot.WEAPON)
                    .findFirst().orElse(null);
            this.weapon = equippedWeapon;
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
            return user.getStatAgi() != null ? user.getStatAgi() * 2 + 10 : 20;
        }

        @Override
        public int getAttack() {
            int totalStr = user.getStatStr() != null ? user.getStatStr() : 5;
            int equipAttack = 0;
            if (weapon != null) {
                equipAttack = weapon.getFinalAttack();
            }
            return totalStr * 2 + equipAttack;
        }

        @Override
        public int getDefense() {
            int totalCon = user.getStatCon() != null ? user.getStatCon() : 5;
            return totalCon;
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
            return user.getStatWis() != null ? user.getStatWis() : 5;
        }
    }

    static class BeastCombatant implements Combatant {
        private final Beast beast;
        private int hp;

        BeastCombatant(Beast beast) {
            this.beast = beast;
            this.hp = beast.getHpCurrent() != null ? beast.getHpCurrent() : beast.getMaxHp();
        }

        @Override
        public Long getId() {
            return beast.getId();
        }

        @Override
        public String getName() {
            return beast.getBeastName() != null ? beast.getBeastName() : "灵兽#" + beast.getId();
        }

        @Override
        public int getSpeed() {
            return beast.getLevel() != null ? beast.getLevel() * 2 + 8 : 10;
        }

        @Override
        public int getAttack() {
            return beast.getAttack() != null ? beast.getAttack() : 10;
        }

        @Override
        public int getDefense() {
            return beast.getDefense() != null ? beast.getDefense() : 8;
        }

        @Override
        public int getHp() {
            return hp;
        }

        @Override
        public int getMaxHp() {
            return beast.getMaxHp() != null ? beast.getMaxHp() : 100;
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
    }
}
