package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 上架共享功法操作结果。
 *
 * <p>仅长老/执事可操作。将弟子献上的待上架功法发布到功法库，供全体弟子学习。
 */
public record PublishSharedSkillResponse(
    @JsonPropertyDescription("上架的共享功法编号") long sharedSkillId,
    @JsonPropertyDescription("上架的功法名称") String skillName) {}
