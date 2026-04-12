package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;

import java.time.LocalDateTime;

/**
 * 灵田地块值对象
 */
@Data
@Builder
public class FarmCellVO {
    /**
     * 坐标位置（格式："x,y"）
     */
    private String position;

    /**
     * 作物ID
     */
    private Integer cropId;

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 五行属性
     */
    private WuxingType element;

    /**
     * 种植时间
     */
    private LocalDateTime plantTime;

    /**
     * 预计成熟时间
     */
    private LocalDateTime matureTime;

    /**
     * 生长进度（0.0-1.0）
     */
    private Double growthProgress;

    /**
     * 是否已成熟
     */
    private Boolean isMature;

    /**
     * 基础生长速度（小时）
     */
    private Double baseGrowthHours;

    /**
     * 实际生长速度修正系数
     */
    private Double growthModifier;
}
