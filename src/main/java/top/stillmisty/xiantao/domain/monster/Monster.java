package top.stillmisty.xiantao.domain.monster;

import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Monster implements Combatant {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1_000_000_000L);

    private final MonsterTemplate template;
    private final long instanceId;
    private int hp;
    private int level;
    private final List<Skill> skills;

    public Monster(MonsterTemplate template, int level, List<Skill> skills) {
        this.template = template;
        this.instanceId = ID_GENERATOR.getAndIncrement();
        this.level = level;
        this.skills = skills != null ? skills : List.of();
        double levelScale = 1.0 + (level - template.getBaseLevel()) * 0.15;
        this.hp = (int) Math.round(template.getBaseHp() * levelScale);
    }

    @Override
    public Long getId() {
        return instanceId;
    }

    @Override
    public String getName() {
        return template.getName();
    }

    @Override
    public int getSpeed() {
        double levelScale = 1.0 + (level - template.getBaseLevel()) * 0.1;
        return (int) Math.round(template.getBaseSpeed() * levelScale);
    }

    @Override
    public int getAttack() {
        double levelScale = 1.0 + (level - template.getBaseLevel()) * 0.15;
        return (int) Math.round(template.getBaseAttack() * levelScale);
    }

    @Override
    public int getDefense() {
        double levelScale = 1.0 + (level - template.getBaseLevel()) * 0.1;
        return (int) Math.round(template.getBaseDefense() * levelScale);
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        double levelScale = 1.0 + (level - template.getBaseLevel()) * 0.15;
        return (int) Math.round(template.getBaseHp() * levelScale);
    }

    @Override
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    @Override
    public void heal(int amount) {
        hp = Math.min(getMaxHp(), hp + amount);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public List<Skill> getSkills() {
        return skills;
    }

    public MonsterType getMonsterType() {
        return template.getMonsterType();
    }
}
