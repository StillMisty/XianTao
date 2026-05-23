package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 扩充成员名额操作结果。
 *
 * <p>仅宗主可操作。消耗宗门资金扩充成员上限。不能超过宗门等级允许的最大成员数。
 */
public record ExpandMembersResponse(
    @JsonPropertyDescription("本次扩充增加的槽位数") int addedSlots,
    @JsonPropertyDescription("新的成员上限") int newMaxMembers,
    @JsonPropertyDescription("本次消耗的宗门资金") long cost,
    @JsonPropertyDescription("扩充后剩余的宗门资金") long remainingFunds) {}
