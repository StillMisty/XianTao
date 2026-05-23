package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 学习共享功法操作结果。
 *
 * <p>消耗贡献值学习指定共享功法。学习前请确保贡献值充足且功法槽位未满。
 */
public record LearnSharedSkillResponse(
    @JsonPropertyDescription("学习的共享功法编号") long sharedSkillId,
    @JsonPropertyDescription("学到的功法名称") String skillName,
    @JsonPropertyDescription("本次学习消耗的贡献值") int cost,
    @JsonPropertyDescription("学习后剩余的贡献值") int remainingContribution) {}
