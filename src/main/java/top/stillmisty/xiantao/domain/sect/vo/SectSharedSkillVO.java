package top.stillmisty.xiantao.domain.sect.vo;

import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;

public record SectSharedSkillVO(
    Long sharedSkillId,
    Long skillId,
    String skillName,
    String effectDesc,
    Integer levelRequirement,
    Integer contributionCost,
    SectSharedSkillStatus status,
    String submitterName) {}
