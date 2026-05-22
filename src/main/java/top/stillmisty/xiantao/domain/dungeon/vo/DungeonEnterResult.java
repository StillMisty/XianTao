package top.stillmisty.xiantao.domain.dungeon.vo;

import java.util.List;

/** 进入秘境或推进区域的结果 VO — 承载 dungeon/area/poi 结构化数据，由 handle 层负责格式化展示 */
public record DungeonEnterResult(
    String dungeonName, String areaName, int memberCount, List<DungeonPoiEntry> pois) {

  public record DungeonPoiEntry(String name, String typeName, boolean locked) {}
}
