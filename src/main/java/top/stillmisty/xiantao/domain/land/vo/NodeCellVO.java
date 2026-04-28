package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
/**
 * 阵眼地块值对象
 */
@Data
@Builder
public class NodeCellVO {
    /**
     * 地块编号
     */
    private Integer cellId;

    /**
     * 阵眼等级
     */
    private Integer level;

    /**
     * 当前耐久度
     */
    private Integer durability;

    /**
     * 最大耐久度
     */
    private Integer maxDurability;

    /**
     * 是否与其他阵眼相连（连环阵）
     */
    private Boolean isChained;
}
