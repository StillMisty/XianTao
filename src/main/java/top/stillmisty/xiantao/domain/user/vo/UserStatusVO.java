package top.stillmisty.xiantao.domain.user.vo;

import java.time.LocalDateTime;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

/** 用户状态 VO */
public record UserStatusVO(
    Long userId,
    String nickname,
    Integer level,
    Long exp,
    Long expToNextLevel,
    UserStatus status,
    String statusName,
    Integer hpCurrent,
    Integer hpMax,
    Double hpPercentage,
    Integer statValue,
    Long spiritStones,
    Integer breakthroughFailCount,
    Double nextBreakthroughRate,
    LocalDateTime trainingStartTime,
    Long meditationDurationMinutes) {}
