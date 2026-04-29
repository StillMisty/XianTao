package top.stillmisty.xiantao.domain.user.vo;

/**
 * StaminaService 返回的体力状态 VO。
 */
public record StaminaInfoVO(int currentStamina, int maxStamina, int percent, int offlineRecovered) {}
