package top.stillmisty.xiantao.domain.shop.vo;

import java.util.List;

public record EquipmentListVO(List<EquipmentEntry> equipments) {

  public record EquipmentEntry(
      long id, String name, String rarity, int forgeLevel, String affixSummary) {}
}
