package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 升级宗门建筑操作结果。
 *
 * <p>仅宗主可操作。消耗宗门资金升级指定建筑。每类建筑有最大等级限制。 升级效果取决于建筑类型（灵脉产出宗门资金、藏经阁解锁功法槽等）。
 */
public record UpgradeBuildingResponse(
    @JsonPropertyDescription("被升级的建筑类型代码") String buildingTypeCode,
    @JsonPropertyDescription("建筑中文名称") String buildingName,
    @JsonPropertyDescription("升级前等级") int oldLevel,
    @JsonPropertyDescription("升级后等级") int newLevel,
    @JsonPropertyDescription("本次消耗的宗门资金") long cost,
    @JsonPropertyDescription("升级后剩余的宗门资金") long remainingFunds) {}
