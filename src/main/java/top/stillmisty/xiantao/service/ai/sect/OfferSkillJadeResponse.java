package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 献上功法玉简操作结果。
 *
 * <p>弟子将背包中的功法玉简献给宗门，玉简中的功法进入共享功法库（待上架状态）， 弟子获得对应贡献值奖励。
 */
public record OfferSkillJadeResponse(
    @JsonPropertyDescription("献上的玉简名称") String jadeName,
    @JsonPropertyDescription("玉简中记录的功法名称") String skillName,
    @JsonPropertyDescription("此次献上获得的贡献值") int contributionGained) {}
