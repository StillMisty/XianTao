package top.stillmisty.xiantao.domain.masterapprentice.vo;

import org.jspecify.annotations.Nullable;

/** 师徒关系信息 VO */
public record MasterApprenticeInfoVO(
    boolean hasMaster,
    @Nullable Long masterId,
    @Nullable String masterName,
    @Nullable Integer masterLevel,
    @Nullable String masterRealmDisplay,
    @Nullable Long relationshipId,
    String status,
    Integer apprenticeCount,
    java.util.List<ApprenticeInfoVO> apprentices) {}
