package top.stillmisty.xiantao.domain.monster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

import java.util.*;

/**
 * 默认战斗引擎实现
 * 支持PVE战斗场景
 */
@Slf4j
@Component
public class DefaultCombatEngine implements CombatEngine {

    @Override
    public BattleResultVO simulate(BattleContext context) {
        Team teamA = context.getTeamA();
        Team teamB = context.getTeamB();
        int maxRounds = context.getMaxRounds();

        int round = 0;
        List<Map<String, Object>> combatLog = new ArrayList<>();
        Map<String, Integer> damageDealt = new LinkedHashMap<>();
        Map<String, Integer> skillProcs = new LinkedHashMap<>();
        Map<String, Integer> skillCooldowns = new LinkedHashMap<>();
        BuffManager buffManager = new BuffManager();

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

            // 处理持续效果（DOT/HEAL）
            for (Combatant c : teamA.aliveMembers()) {
                int overTimeEffect = buffManager.processOverTimeEffects(c.getId());
                if (overTimeEffect != 0) {
                    if (overTimeEffect > 0) {
                        c.heal(overTimeEffect);
                    } else {
                        c.takeDamage(-overTimeEffect);
                    }
                }
            }
            for (Combatant c : teamB.aliveMembers()) {
                int overTimeEffect = buffManager.processOverTimeEffects(c.getId());
                if (overTimeEffect != 0) {
                    if (overTimeEffect > 0) {
                        c.heal(overTimeEffect);
                    } else {
                        c.takeDamage(-overTimeEffect);
                    }
                }
            }

            // 按speed降序排列（考虑减速效果）
            List<Combatant> allAlive = new ArrayList<>();
            allAlive.addAll(teamA.aliveMembers());
            allAlive.addAll(teamB.aliveMembers());
            allAlive.sort((c1, c2) -> {
                int speed1 = (int) (c1.getSpeed() * buffManager.getSpeedModifier(c1.getId()));
                int speed2 = (int) (c2.getSpeed() * buffManager.getSpeedModifier(c2.getId()));
                return Integer.compare(speed2, speed1);
            });

            int sequence = 0;
            for (Combatant attacker : allAlive) {
                if (!attacker.isAlive()) continue;

                // 检查是否有控制效果（眩晕/冰冻）
                if (buffManager.hasControl(attacker.getId())) {
                    Map<String, Object> logEntry = new LinkedHashMap<>();
                    logEntry.put("round", round);
                    logEntry.put("sequence", ++sequence);
                    logEntry.put("attackerName", attacker.getName());
                    logEntry.put("attackType", "CONTROLLED");
                    logEntry.put("description", attacker.getName() + "被控制，无法行动");
                    combatLog.add(logEntry);
                    continue;
                }

                Team attackerTeam = teamA.members().contains(attacker) ? teamA : teamB;
                Team defenderTeam = attackerTeam == teamA ? teamB : teamA;

                sequence++;

                // 尝试法决触发
                int cooldownRemaining = skillCooldowns.getOrDefault("skill_" + attacker.getId(), 0);
                boolean skillTriggered = false;
                Skill triggeredSkill = null;

                // 检查是否有沉默效果
                boolean silenced = buffManager.hasSilence(attacker.getId());

                if (cooldownRemaining <= 0 && !silenced) {
                    List<Skill> skills = attacker.getSkills();
                    if (skills != null && !skills.isEmpty()) {
                        for (Skill skill : skills) {
                            // 支持所有效果类型
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
                int damage = 0;
                boolean isControl = false;
                boolean isBuff = false;

                if (skillTriggered && triggeredSkill != null) {
                    // 遍历技能效果列表
                    List<SkillEffect> effects = triggeredSkill.getEffects();
                    if (effects != null && !effects.isEmpty()) {
                        for (SkillEffect effect : effects) {
                            // 检查触发概率
                            double chance = effect.chance() != null ? effect.chance() : 1.0;
                            if (Math.random() > chance) continue;
                            
                            switch (effect.type()) {
                                case DAMAGE -> {
                                    int effectDamage = calculateEffectDamage(attacker, defender, effect, buffManager);
                                    damage += effectDamage;
                                }
                                case MULTI_HIT -> {
                                    int effectDamage = calculateEffectDamage(attacker, defender, effect, buffManager);
                                    damage += effectDamage * 3;
                                }
                                case ARMOR_BREAK -> {
                                    double value = effect.value() != null ? effect.value() : 0.2;
                                    int duration = effect.duration() != null ? effect.duration() : 3;
                                    Buff armorBreak = Buff.builder()
                                            .type(BuffType.ARMOR_BREAK)
                                            .value(value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(defender.getId(), armorBreak);
                                    isControl = true;
                                }
                                case SLOW -> {
                                    double value = effect.value() != null ? effect.value() : 0.3;
                                    int duration = effect.duration() != null ? effect.duration() : 2;
                                    Buff slow = Buff.builder()
                                            .type(BuffType.SLOW)
                                            .value(value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(defender.getId(), slow);
                                    isControl = true;
                                }
                                case DOT -> {
                                    double value = effect.value() != null ? effect.value() : 0.3;
                                    int duration = effect.duration() != null ? effect.duration() : 3;
                                    int maxStacks = effect.maxStacks() != null ? effect.maxStacks() : 3;
                                    Buff dot = Buff.builder()
                                            .type(BuffType.DOT)
                                            .value(attacker.getAttack() * value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .stackable(true)
                                            .maxStacks(maxStacks)
                                            .build();
                                    buffManager.addBuff(defender.getId(), dot);
                                }
                                case EXECUTE -> {
                                    double hpRatio = (double) defender.getHp() / defender.getMaxHp();
                                    double threshold = effect.value() != null ? effect.value() : 0.3;
                                    double executeMultiplier = hpRatio < threshold ? 2.0 : 1.0;
                                    int effectDamage = calculateEffectDamage(attacker, defender, effect, buffManager);
                                    damage += (int) (effectDamage * executeMultiplier);
                                }
                                case LIFESTEAL -> {
                                    int effectDamage = calculateNormalDamage(attacker, defender, buffManager);
                                    damage += effectDamage;
                                    double ratio = effect.value() != null ? effect.value() : 0.33;
                                    attacker.heal((int) (effectDamage * ratio));
                                }
                                case HEAL -> {
                                    double ratio = effect.value() != null ? effect.value() : 0.5;
                                    attacker.heal((int) (attacker.getAttack() * ratio));
                                    isBuff = true;
                                }
                                case ATTACK_BUFF -> {
                                    double value = effect.value() != null ? effect.value() : 0.2;
                                    int duration = effect.duration() != null ? effect.duration() : 3;
                                    Buff buff = Buff.builder()
                                            .type(BuffType.ATTACK_BUFF)
                                            .value(value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(attacker.getId(), buff);
                                    isBuff = true;
                                }
                                case DEFENSE_BUFF -> {
                                    double value = effect.value() != null ? effect.value() : 0.2;
                                    int duration = effect.duration() != null ? effect.duration() : 3;
                                    Buff buff = Buff.builder()
                                            .type(BuffType.DEFENSE_BUFF)
                                            .value(value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(attacker.getId(), buff);
                                    isBuff = true;
                                }
                                case SPEED_BUFF -> {
                                    double value = effect.value() != null ? effect.value() : 0.2;
                                    int duration = effect.duration() != null ? effect.duration() : 3;
                                    Buff buff = Buff.builder()
                                            .type(BuffType.SPEED_BUFF)
                                            .value(value)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(attacker.getId(), buff);
                                    isBuff = true;
                                }
                                case STUN, FREEZE, SILENCE -> {
                                    int duration = effect.duration() != null ? effect.duration() : 1;
                                    BuffType buffType = effect.type() == EffectType.STUN ? BuffType.STUN :
                                                       effect.type() == EffectType.FREEZE ? BuffType.FREEZE : BuffType.SILENCE;
                                    Buff control = Buff.builder()
                                            .type(buffType)
                                            .value(1.0)
                                            .remainingTurns(duration)
                                            .source(triggeredSkill.getName())
                                            .build();
                                    buffManager.addBuff(defender.getId(), control);
                                    isControl = true;
                                }
                            }
                        }
                    }
                    // 如果没有效果，使用普通攻击
                    if (damage == 0 && !isBuff && !isControl) {
                        damage = calculateNormalDamage(attacker, defender, buffManager);
                    }
                } else {
                    damage = calculateNormalDamage(attacker, defender, buffManager);
                }

                // 应用伤害
                if (damage > 0) {
                    defender.takeDamage(damage);
                }

                int hpAfter = defender.getHp();
                boolean isKill = hpAfter <= 0;

                if (damage > 0) {
                    damageDealt.merge(attacker.getName(), damage, Integer::sum);
                }

                Map<String, Object> logEntry = new LinkedHashMap<>();
                logEntry.put("round", round);
                logEntry.put("sequence", sequence);
                logEntry.put("attackerName", attacker.getName());
                logEntry.put("defenderName", isBuff ? attacker.getName() : defender.getName());
                logEntry.put("attackType", skillTriggered ? "SKILL" : "NORMAL");
                if (skillTriggered && triggeredSkill != null) {
                    logEntry.put("skillName", triggeredSkill.getName());
                    // 记录所有效果类型
                    if (triggeredSkill.getEffects() != null) {
                        logEntry.put("effects", triggeredSkill.getEffects().stream()
                                .map(e -> e.type().name())
                                .toList());
                    }
                }
                if (isControl) {
                    logEntry.put("buffApplied", true);
                }
                if (isBuff) {
                    logEntry.put("buffApplied", true);
                    logEntry.put("buffTarget", attacker.getName());
                }
                logEntry.put("damageDealt", damage);
                logEntry.put("isCrit", false);
                logEntry.put("defenderHpBefore", hpBefore);
                logEntry.put("defenderHpAfter", hpAfter);
                logEntry.put("isKill", isKill);
                combatLog.add(logEntry);

                // 更新冷却（攻速影响CD恢复）
                double attackSpeed = attacker.getAttackSpeed();
                for (var entry : new HashMap<>(skillCooldowns).entrySet()) {
                    int cd = entry.getValue() - (int) Math.round(attackSpeed);
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

    @Override
    public String getEngineName() {
        return "DefaultCombatEngine";
    }

    /**
     * 计算普通攻击伤害
     */
    private int calculateNormalDamage(Combatant attacker, Combatant defender, BuffManager buffManager) {
        double advantageMultiplier = 1.0;
        if (attacker instanceof PlayerCombatant pc && defender instanceof Monster monster) {
            advantageMultiplier = getWeaponTypeAdvantage(pc.getWeaponType(), monster.getMonsterType());
        }

        // 应用攻击增益
        double attackModifier = buffManager.getAttackModifier(attacker.getId());
        int rawDamage = (int) Math.round(attacker.getAttack() * advantageMultiplier * attackModifier);

        // 应用防御修正（破甲效果）
        double defenseModifier = buffManager.getDefenseModifier(defender.getId());
        double defenseBonus = buffManager.getDefenseBonusModifier(defender.getId());
        int reduction = (int) Math.round(defender.getDefense() * 0.3 * defenseModifier * defenseBonus);

        return Math.max(1, rawDamage - reduction);
    }

    /**
     * 计算效果伤害（支持公式和普通攻击）
     */
    private int calculateEffectDamage(Combatant attacker, Combatant defender, SkillEffect effect, BuffManager buffManager) {
        String formula = effect.formula();
        
        double advantageMultiplier = 1.0;
        if (attacker instanceof PlayerCombatant pc && defender instanceof Monster monster) {
            advantageMultiplier = getWeaponTypeAdvantage(pc.getWeaponType(), monster.getMonsterType());
        }

        // 应用攻击增益
        double attackModifier = buffManager.getAttackModifier(attacker.getId());

        int baseDmg;
        if (formula != null && !formula.isBlank()) {
            // 使用公式计算
            if (attacker instanceof PlayerCombatant pc) {
                baseDmg = evaluateFormula(formula, pc.getWis());
            } else {
                baseDmg = (int) Math.round(attacker.getAttack() * 1.5);
            }
        } else {
            // 使用普通攻击
            baseDmg = attacker.getAttack();
        }

        int rawDamage = (int) Math.round(baseDmg * advantageMultiplier * attackModifier);

        // 应用防御修正（破甲效果）
        double defenseModifier = buffManager.getDefenseModifier(defender.getId());
        double defenseBonus = buffManager.getDefenseBonusModifier(defender.getId());
        int reduction = (int) Math.round(defender.getDefense() * 0.3 * defenseModifier * defenseBonus);

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
}
