package top.stillmisty.xiantao.domain.beast;

import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

import java.util.List;

/**
 * 灵兽AI接口
 * 定义灵兽的战斗行为决策
 */
public interface BeastAI {

    /**
     * 决定灵兽的行动
     *
     * @param beast 灵兽战斗单位
     * @param allies 友方队伍
     * @param enemies 敌方队伍
     * @return 行动决策
     */
    BeastAction decideAction(Combatant beast, Team allies, Team enemies);

    /**
     * 选择攻击目标
     *
     * @param beast 灵兽战斗单位
     * @param enemies 敌方队伍
     * @param allies 友方队伍
     * @return 目标战斗单位
     */
    Combatant selectTarget(Combatant beast, Team enemies, Team allies);

    /**
     * 选择要使用的技能
     *
     * @param beast 灵兽战斗单位
     * @param target 目标
     * @param allies 友方队伍
     * @param enemies 敌方队伍
     * @return 技能（如果没有合适的技能返回null）
     */
    Skill selectSkill(Combatant beast, Combatant target, Team allies, Team enemies);

    /**
     * 灵兽行动枚举
     */
    enum BeastAction {
        ATTACK("攻击"),
        DEFEND("防御"),
        SUPPORT("辅助"),
        SKILL("使用技能");

        private final String name;

        BeastAction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
