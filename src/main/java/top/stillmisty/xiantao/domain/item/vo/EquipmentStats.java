package top.stillmisty.xiantao.domain.item.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.item.entity.Equipment;

/**
 * 已穿戴装备的属性聚合（四维 + 攻防）
 *
 * <p>攻防与四维均含品质波动、随机词条与锻造加成；战斗数值与状态展示共用同一聚合，避免两处口径漂移。
 */
public record EquipmentStats(int str, int con, int agi, int wis, int attack, int defense) {

  public static final EquipmentStats EMPTY = new EquipmentStats(0, 0, 0, 0, 0, 0);

  /** 聚合装备列表的属性加成（传空列表返回零值聚合） */
  public static EquipmentStats from(List<Equipment> equippedItems) {
    int str = 0, con = 0, agi = 0, wis = 0, attack = 0, defense = 0;
    for (Equipment equipment : equippedItems) {
      str += equipment.getStrBonus();
      con += equipment.getConBonus();
      agi += equipment.getAgiBonus();
      wis += equipment.getWisBonus();
      attack += equipment.getFinalAttack();
      defense += equipment.getFinalDefense();
    }
    return new EquipmentStats(str, con, agi, wis, attack, defense);
  }
}
