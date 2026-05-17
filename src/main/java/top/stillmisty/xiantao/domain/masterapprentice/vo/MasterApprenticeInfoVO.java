package top.stillmisty.xiantao.domain.masterapprentice.vo;

/** 师徒关系信息 VO */
public record MasterApprenticeInfoVO(
    boolean hasMaster,
    Long masterId,
    String masterName,
    Integer masterLevel,
    String masterRealmDisplay,
    Long relationshipId,
    String status,
    Integer apprenticeCount,
    java.util.List<ApprenticeInfoVO> apprentices) {}
