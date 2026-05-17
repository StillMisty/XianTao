package top.stillmisty.xiantao.domain.sect.vo;

import top.stillmisty.xiantao.domain.sect.enums.SectPosition;

public record SectOverviewVO(
    Long sectId,
    String name,
    Integer level,
    Long funds,
    Integer memberCount,
    Integer maxMembers,
    String leaderName,
    String description,
    String notice,
    String verse,
    String ethos,
    String eventText,
    SectPosition myPosition,
    Integer myContribution) {}
