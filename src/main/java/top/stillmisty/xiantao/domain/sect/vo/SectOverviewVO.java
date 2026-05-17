package top.stillmisty.xiantao.domain.sect.vo;

import top.stillmisty.xiantao.domain.sect.enums.SectPosition;

/** 宗门概览 VO */
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
    SectPosition myPosition,
    Integer myContribution) {}
