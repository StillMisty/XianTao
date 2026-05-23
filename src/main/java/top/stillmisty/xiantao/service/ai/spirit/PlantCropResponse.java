package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 种植操作结果。
 *
 * <p>播种成功后返回地块编号、作物名称和预计生长时间。 {@code baseGrowthHours} 表示基础生长周期，实际成熟时间受地块等级、世界事件等因素影响。
 */
public record PlantCropResponse(
    @JsonPropertyDescription("播种的地块编号") String position,
    @JsonPropertyDescription("种下的作物名称") String cropName,
    @JsonPropertyDescription("基础生长时间（小时），实际成熟时间可能受地块等级加成缩短") double baseGrowthHours) {}
