package top.stillmisty.xiantao.domain.item.vo;

import java.util.List;

/** 背包查看结果 VO 包含：装备、材料、种子、灵蛋、消耗品、灵石/铜币 */
public record InventoryResult(
    boolean success,
    String message,
    Long userId,
    List<InventoryItem> equipments,
    List<InventoryItem> materials,
    List<InventoryItem> seeds,
    List<InventoryItem> beastEggs,
    Long spiritStones) {}
