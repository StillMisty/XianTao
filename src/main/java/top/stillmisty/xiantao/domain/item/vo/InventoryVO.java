package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.entity.InventoryItem;

import java.util.List;

/**
 * 背包查看结果 VO
 */
@Data
@Builder
public class InventoryVO {

    private Long userId;
    private Integer capacity;
    private Integer currentSize;

    // 按类型分组的物品
    private List<InventoryItem> equipments;
    private List<InventoryItem> materials;
    private List<InventoryItem> seeds;
    private List<InventoryItem> beastEggs;

    // 货币
    private Long coins;
    private Long spiritStones;
}
