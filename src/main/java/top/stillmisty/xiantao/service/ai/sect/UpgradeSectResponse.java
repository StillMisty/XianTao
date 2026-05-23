package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 提升宗门等级操作结果。
 *
 * <p>仅宗主可操作。消耗宗门资金提升宗门等级。 等级提升后可能解锁新功能、增加成员上限等。 注意：宗门资金与灵石是不同的货币体系，宗门资金不可与灵石互兑。
 */
public record UpgradeSectResponse(
    @JsonPropertyDescription("宗门新等级") int newLevel,
    @JsonPropertyDescription("新的成员上限") int newMaxMembers,
    @JsonPropertyDescription("本次消耗的宗门资金") long cost,
    @JsonPropertyDescription("升级后剩余的宗门资金") long remainingFunds) {}
