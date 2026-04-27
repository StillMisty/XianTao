package top.stillmisty.xiantao.service;

/**
 * StaminaService 返回的体力状态 VO。
 */
public record StaminaInfoVO(int currentStamina, int maxStamina, int percent, int offlineRecovered) {}
