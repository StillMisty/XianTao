package top.stillmisty.xiantao.domain.monster;

import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.User;

import java.util.List;

/**
 * 玩家战斗单位
 */
public class PlayerCombatant implements Combatant {
    private final User user;
    private final Equipment weapon;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private int hp;

    public PlayerCombatant(User user, EquipmentRepository equipmentRepository,
                           EquipmentTemplateRepository equipmentTemplateRepository) {
        this.user = user;
        this.equipmentTemplateRepository = equipmentTemplateRepository;
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
        int statValue = user.getStatValue();
        return statValue * 2 + 10;
    }

    @Override
    public int getAttack() {
        int statValue = user.getStatValue();
        int equipAttack = 0;
        if (weapon != null) {
            equipAttack = weapon.getFinalAttack();
        }
        return statValue * 2 + equipAttack;
    }

    @Override
    public int getDefense() {
        int statValue = user.getStatValue();
        return statValue;
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
    public void heal(int amount) {
        hp = Math.min(getMaxHp(), hp + amount);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public List<Skill> getSkills() {
        return List.of();
    }

    public WeaponType getWeaponType() {
        return weapon != null ? weapon.getWeaponType() : null;
    }

    public int getWis() {
        return user.getStatValue();
    }

    @Override
    public double getAttackSpeed() {
        if (weapon == null) {
            return 1.0;
        }
        // 从装备模板获取攻速
        return equipmentTemplateRepository.findById(weapon.getTemplateId())
                .map(template -> template.getAttackSpeed() != null ? template.getAttackSpeed() : 1.0)
                .orElse(1.0);
    }
}
