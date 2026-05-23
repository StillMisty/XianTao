package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 建造宗门建筑操作结果。
 *
 * <p>仅宗主可操作。消耗宗门资金建造指定类型的建筑。每类建筑只能建一座。 建造后建筑初始等级为 1。宗门资金与玩家灵石是独立的货币体系。
 */
public record BuildStructureResponse(
    @JsonPropertyDescription(
            "建筑类型代码：SCRIPTURE_PAVILION/TRAINING_ROOM/ALCHEMY_CHAMBER/SPIRIT_VEIN/FORGE_WORKSHOP/GUARD_ARRAY/HERB_GARDEN")
        String buildingTypeCode,
    @JsonPropertyDescription("建造的建筑中文名称") String buildingName,
    @JsonPropertyDescription("建筑当前等级（新建为1）") int level,
    @JsonPropertyDescription("本次消耗的宗门资金") long cost,
    @JsonPropertyDescription("建造后剩余的宗门资金") long remainingFunds) {}
