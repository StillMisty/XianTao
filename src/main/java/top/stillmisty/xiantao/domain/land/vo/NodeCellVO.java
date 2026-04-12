package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;

/**
 * 阵眼地块值对象
 */
@Data
@Builder
public class NodeCellVO {
    /**
     * 坐标位置（格式："x,y"）
     */
    private String position;

    /**
     * 阵眼等级
     */
    private Integer level;

    /**
     * 五行属性
     */
    private WuxingType element;

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
