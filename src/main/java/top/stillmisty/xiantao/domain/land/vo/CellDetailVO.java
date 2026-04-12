package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;

import java.time.LocalDateTime;

/**
 * 地块详情值对象
 */
@Data
@Builder
public class CellDetailVO {
    /**
     * 坐标位置
     */
    private String position;

    /**
     * 地块类型
     */
    private CellType type;

    /**
     * 五行属性（如果有）
     */
    private WuxingType element;

    /**
     * 作物/灵兽/阵眼名称（如果有）
     */
    private String name;

    /**
     * 等级（如果是阵眼或灵兽）
     */
    private Integer level;

    /**
     * 生长进度（如果是灵田，0.0-1.0）
     */
    private Double growthProgress;

    /**
     * 是否成熟（如果是灵田）
     */
    private Boolean isMature;

    /**
     * 饥饿值（如果是兽栏，0-100）
     */
    private Integer hunger;

    /**
     * 耐久度（如果是阵眼）
     */
    private Integer durability;

    /**
     * 种植/建造时间
     */
    private LocalDateTime createTime;
}
