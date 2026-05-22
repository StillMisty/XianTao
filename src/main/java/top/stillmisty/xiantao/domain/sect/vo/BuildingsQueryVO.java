package top.stillmisty.xiantao.domain.sect.vo;

import java.util.List;

public record BuildingsQueryVO(List<BuildingEntry> built, List<BuildingEntry> buildable) {

  public record BuildingEntry(
      String typeCode, String name, int level, int maxLevel, long upgradeCost, long buildCost) {}
}
