package top.stillmisty.xiantao.domain.user.vo;

/** 护道结果 VO */
public record DaoProtectionResult(
    boolean success,
    String message,
    Long protectorId,
    String protectorName,
    Integer protectorLevel,
    Long protegeId,
    String protegeName,
    Integer protegeLevel,
    Double singleProtectorBonus,
    Double totalBonus,
    Boolean isInSameLocation) {}
