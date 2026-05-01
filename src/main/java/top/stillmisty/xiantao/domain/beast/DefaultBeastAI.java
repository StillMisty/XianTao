package top.stillmisty.xiantao.domain.beast;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 默认灵兽AI实现
 * 配合玩家策略：优先攻击对玩家威胁最大的敌人
 */
@Slf4j
@Component
public class DefaultBeastAI implements BeastAI {

    @Override
    public BeastAction decideAction(Combatant beast, Team allies, Team enemies) {
        // 检查是否有治疗技能且玩家血量低于50%
        Combatant player = getPlayer(allies);
        if (player != null && player.getHp() < player.getMaxHp() * 0.5) {
            Skill healSkill = findSkillByType(beast, EffectType.HEAL);
            if (healSkill != null) {
                return BeastAction.SKILL;
            }
        }

        // 检查是否有增益技能且未激活
        Skill buffSkill = findSkillByType(beast, EffectType.ATTACK_BUFF);
        if (buffSkill != null) {
            return BeastAction.SKILL;
        }

        // 检查是否有防御增益技能
        Skill defenseBuffSkill = findSkillByType(beast, EffectType.DEFENSE_BUFF);
        if (defenseBuffSkill != null && beast.getHp() < beast.getMaxHp() * 0.7) {
            return BeastAction.SKILL;
        }

        // 检查是否有控制技能且目标可控制
        Skill controlSkill = findSkillByType(beast, EffectType.STUN);
        if (controlSkill != null && hasControlTarget(enemies)) {
            return BeastAction.SKILL;
        }

        // 检查是否有DOT技能（持续伤害）
        Skill dotSkill = findSkillByType(beast, EffectType.DOT);
        if (dotSkill != null && hasHighHpTarget(enemies)) {
            return BeastAction.SKILL;
        }

        // 检查是否有AOE技能（群体伤害）
        Skill aoeSkill = findSkillByType(beast, EffectType.AOE_DAMAGE);
        if (aoeSkill != null && enemies.aliveMembers().size() >= 3) {
            return BeastAction.SKILL;
        }

        // 检查是否有MULTI_HIT技能（多重攻击）
        Skill multiHitSkill = findSkillByType(beast, EffectType.MULTI_HIT);
        if (multiHitSkill != null) {
            return BeastAction.SKILL;
        }

        // 默认攻击
        return BeastAction.ATTACK;
    }

    @Override
    public Combatant selectTarget(Combatant beast, Team enemies, Team allies) {
        List<Combatant> aliveEnemies = enemies.aliveMembers();
        if (aliveEnemies.isEmpty()) {
            return null;
        }

        Combatant player = getPlayer(allies);

        // 优先攻击对玩家威胁最大的敌人（攻击力最高的怪物）
        if (player != null && player.getHp() < player.getMaxHp() * 0.3) {
            // 如果玩家血量低于30%，优先攻击攻击玩家的怪物
            Combatant attacker = findAttackerOf(aliveEnemies, player);
            if (attacker != null) {
                return attacker;
            }
        }

        // 优先攻击即将被击杀的敌人（血量低于20%）
        Combatant lowHpEnemy = aliveEnemies.stream()
                .filter(c -> c.getHp() < c.getMaxHp() * 0.2)
                .findFirst()
                .orElse(null);

        if (lowHpEnemy != null) {
            return lowHpEnemy;
        }

        // 优先攻击攻击力最高的敌人
        return aliveEnemies.stream()
                .max(Comparator.comparingInt(Combatant::getAttack))
                .orElse(aliveEnemies.getFirst());
    }

    @Override
    public Skill selectSkill(Combatant beast, Combatant target, Team allies, Team enemies) {
        // 如果有治疗技能且玩家血量低，使用治疗
        Combatant player = getPlayer(allies);
        if (player != null && player.getHp() < player.getMaxHp() * 0.5) {
            Skill healSkill = findSkillByType(beast, EffectType.HEAL);
            if (healSkill != null) {
                return healSkill;
            }
        }

        // 如果有增益技能，使用增益
        Skill buffSkill = findSkillByType(beast, EffectType.ATTACK_BUFF);
        if (buffSkill != null) {
            return buffSkill;
        }

        // 如果有控制技能且目标可控制，使用控制
        Skill controlSkill = findSkillByType(beast, EffectType.STUN);
        if (controlSkill != null && hasControlTarget(enemies)) {
            return controlSkill;
        }

        // 如果有伤害技能，使用伤害
        return findSkillByType(beast, EffectType.DAMAGE);
    }

    /**
     * 获取玩家战斗单位
     */
    private Combatant getPlayer(Team team) {
        return team.members().stream()
                .filter(c -> c instanceof top.stillmisty.xiantao.domain.monster.PlayerCombatant)
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找指定类型的技能
     */
    private Skill findSkillByType(Combatant combatant, EffectType type) {
        List<Skill> skills = combatant.getSkills();
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return skills.stream()
                .filter(skill -> skill.getEffects() != null && 
                        skill.getEffects().stream().anyMatch(e -> e.type() == type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找攻击指定目标的敌人
     */
    private Combatant findAttackerOf(List<Combatant> enemies, Combatant target) {
        // 简单实现：返回第一个敌人
        // 在实际实现中，应该记录谁攻击了谁
        return enemies.isEmpty() ? null : enemies.getFirst();
    }

    /**
     * 检查是否有可控制的目标
     */
    private boolean hasControlTarget(Team enemies) {
        return enemies.aliveMembers().stream()
                .anyMatch(c -> c.getHp() > c.getMaxHp() * 0.3);
    }

    private boolean hasHighHpTarget(Team enemies) {
        return enemies.aliveMembers().stream()
                .anyMatch(c -> c.getHp() > c.getMaxHp() * 0.5);
    }
}
