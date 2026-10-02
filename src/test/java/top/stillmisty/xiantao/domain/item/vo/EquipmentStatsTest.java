package top.stillmisty.xiantao.domain.item.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.item.entity.Equipment;

class EquipmentStatsTest {

  @Test
  void aggregatesStatAffixQualityAndForgeBonuses() {
    Equipment equipment = new Equipment();
    equipment.setStatBonus(Map.of("STR", 5, "CON", 3));
    equipment.setAffixes(Map.of("AGI", 2, "WIS", 1));
    equipment.setAttackBonus(10);
    equipment.setDefenseBonus(8);
    equipment.setQualityMultiplier(1.5);
    equipment.setForgeLevel(2);

    // 攻击 10×1.5 + 2×5 = 25；防御 8×1.5 + 2×5 = 22；四维 = 基础 + 词条
    EquipmentStats stats = EquipmentStats.from(List.of(equipment));

    assertEquals(5, stats.str());
    assertEquals(3, stats.con());
    assertEquals(2, stats.agi());
    assertEquals(1, stats.wis());
    assertEquals(25, stats.attack());
    assertEquals(22, stats.defense());
  }

  @Test
  void emptyEquipmentListYieldsZeroStats() {
    EquipmentStats stats = EquipmentStats.from(List.of());

    assertEquals(EquipmentStats.EMPTY, stats);
  }
}
