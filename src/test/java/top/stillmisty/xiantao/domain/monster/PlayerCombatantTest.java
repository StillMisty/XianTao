package top.stillmisty.xiantao.domain.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.vo.EquipmentStats;
import top.stillmisty.xiantao.domain.skill.vo.PassiveSkillBonuses;
import top.stillmisty.xiantao.domain.user.entity.Player;

class PlayerCombatantTest {

  @Test
  void equipmentStatsParticipateInAttackDefenseSpeedAndSkillVariables() {
    EquipmentStats stats = new EquipmentStats(5, 3, 2, 1, 40, 25);
    PlayerCombatant combatant =
        new PlayerCombatant(createPlayer(), null, 1.0, List.of(), stats).withBuffs(7, 9, 11);

    // 力道 = 10 + 4 + 10 = 24；攻击 = (24 + 5) × 2 + 40 + 7
    assertEquals(105, combatant.getAttack());
    // 根骨 = 8 + 4 + 10 = 22；防御 = 22 + 3 + 25 + 9
    assertEquals(59, combatant.getDefense());
    // 身法 = 6 + 4 + 10 = 20；速度 = (20 + 2) × 2 + 10 + 11
    assertEquals(65, combatant.getSpeed());
    assertEquals(29, combatant.getStr());
    assertEquals(22, combatant.getAgi());
    assertEquals(19, combatant.getWis());
  }

  @Test
  void withoutEquipmentStatsOnlyBaseStatsApply() {
    PlayerCombatant combatant = new PlayerCombatant(createPlayer(), null, 1.0);

    // 力道 24 → 攻击 48；根骨 22；身法 20 → 速度 50
    assertEquals(48, combatant.getAttack());
    assertEquals(22, combatant.getDefense());
    assertEquals(50, combatant.getSpeed());
  }

  @Test
  void weaponAttackIsCountedOnceViaEquipmentStats() {
    Equipment weapon = new Equipment();
    weapon.setWeaponType(WeaponType.SWORD);
    weapon.setAttackBonus(30);
    weapon.setDefenseBonus(0);
    weapon.setForgeLevel(0);

    PlayerCombatant combatant =
        new PlayerCombatant(
            createPlayer(), weapon, 1.0, List.of(), EquipmentStats.from(List.of(weapon)));

    assertEquals(78, combatant.getAttack());
    assertEquals(WeaponType.SWORD, combatant.getWeaponType());
  }

  @Test
  void passiveBonusesScaleAttackDefenseSpeedAndMaxHp() {
    PassiveSkillBonuses bonuses = new PassiveSkillBonuses(0.10, 0.10, 0.10, 0, 0, 0.08, 0);
    PlayerCombatant combatant =
        new PlayerCombatant(createPlayer(), null, 1.0).withPassiveBonuses(bonuses);

    // 攻击 48×1.1；防御 22×1.1；速度 50×1.1；上限 540×1.08
    assertEquals(53, combatant.getAttack());
    assertEquals(24, combatant.getDefense());
    assertEquals(55, combatant.getSpeed());
    assertEquals(583, combatant.getMaxHp());
    // 初始气血按上限增幅同步放大：500×1.08
    assertEquals(540, combatant.getHp());
  }

  @Test
  void surviveLethalTriggersOncePerBattleKeepingConfiguredHpPercent() {
    PassiveSkillBonuses bonuses = new PassiveSkillBonuses(0, 0, 0, 0, 0, 0, 0.20);
    PlayerCombatant combatant =
        new PlayerCombatant(createPlayer(), null, 1.0).withPassiveBonuses(bonuses);

    combatant.takeDamage(9999);
    // 免死保留 20% 上限气血：540×0.2
    assertEquals(108, combatant.getHp());

    combatant.takeDamage(9999);
    assertEquals(0, combatant.getHp());
  }

  private static Player createPlayer() {
    Player player = new Player();
    player.setNickname("测试道友");
    player.setLevel(10);
    player.setStatStr(10);
    player.setStatCon(8);
    player.setStatAgi(6);
    player.setStatWis(4);
    player.setHpCurrent(500);
    return player;
  }
}
