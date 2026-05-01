package top.stillmisty.xiantao.domain.monster;

import top.stillmisty.xiantao.domain.skill.entity.Skill;

import java.util.List;

public interface Combatant {

    Long getId();

    String getName();

    int getSpeed();

    int getAttack();

    int getDefense();

    int getHp();

    int getMaxHp();

    void takeDamage(int amount);

    /**
     * 恢复生命值
     * @param amount 恢复量
     */
    default void heal(int amount) {
        // 默认实现：不超过最大生命值
    }

    boolean isAlive();

    List<Skill> getSkills();
}
