package top.stillmisty.xiantao.domain.user.vo;

import org.jspecify.annotations.Nullable;

/** 护道结果 VO */
public record DaoProtectionResult(
    boolean success,
    String message,
    @Nullable Long protectorId,
    @Nullable String protectorName,
    @Nullable Integer protectorLevel,
    @Nullable Long protegeId,
    @Nullable String protegeName,
    @Nullable Integer protegeLevel,
    @Nullable Double singleProtectorBonus,
    @Nullable Double totalBonus,
    @Nullable Boolean isInSameLocation) {}
