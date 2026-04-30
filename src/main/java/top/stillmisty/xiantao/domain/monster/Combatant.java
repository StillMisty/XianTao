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

    boolean isAlive();

    List<Skill> getSkills();
}
