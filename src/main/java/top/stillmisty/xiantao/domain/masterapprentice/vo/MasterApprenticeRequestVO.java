package top.stillmisty.xiantao.domain.masterapprentice.vo;

/** 拜师/收徒请求 VO */
public record MasterApprenticeRequestVO(
    Long requestId,
    String type,
    Long fromUserId,
    String fromNickname,
    Integer fromLevel,
    String fromRealmDisplay,
    String message) {}
