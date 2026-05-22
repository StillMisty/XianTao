package top.stillmisty.xiantao.domain.sect.vo;

import java.util.List;

public record SharedSkillsQueryVO(
    int myContribution,
    int usedSlots,
    int maxSlots,
    List<SectSharedSkillVO> skills,
    int pendingCount) {}
