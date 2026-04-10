package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.entity.InventoryItem;

import java.util.List;

/**
 * 背包查看结果 VO
 * 包含：装备、材料、种子、灵蛋、消耗品、灵石/铜币
 */
@Data
@Builder
public class InventoryResult {

    private boolean success;
    private String message;

    private Long userId;
    private Integer capacity;
    private Integer currentSize;

    // 按类型分组的物品
    private List<InventoryItem> equipments;
    private List<InventoryItem> materials;
    private List<InventoryItem> seeds;
    private List<InventoryItem> spiritEggs;
    private List<InventoryItem> consumables;

    // 货币
    private Long coins;
    private Long spiritStones;
}
