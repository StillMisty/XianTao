package top.stillmisty.xiantao.domain.sect.vo;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;

public record SectSharedSkillVO(
    Long sharedSkillId,
    Long skillId,
    String skillName,
    @Nullable String effectDesc,
    Integer levelRequirement,
    Integer contributionCost,
    SectSharedSkillStatus status,
    @Nullable String submitterName) {}
