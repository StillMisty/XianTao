package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 移除共享功法操作结果。
 *
 * <p>仅长老/执事可操作。从功法库下架指定共享功法，下架后弟子无法学习。
 */
public record RemoveSharedSkillResponse(
    @JsonPropertyDescription("被移除的共享功法编号") long sharedSkillId,
    @JsonPropertyDescription("被移除的功法名称") String skillName) {}
