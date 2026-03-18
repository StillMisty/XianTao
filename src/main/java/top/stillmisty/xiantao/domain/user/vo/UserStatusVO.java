package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户状态 VO
 */
@Data
@Builder
public class UserStatusVO {
    
    private UUID userId;
    private String nickname;
    private Integer level;
    private Long exp;
    private Long expToNextLevel;
    private UserStatus status;
    private String statusName;
    private Integer hpCurrent;
    private Integer hpMax;
    private Double hpPercentage;
    private Integer statStr;
    private Integer statCon;
    private Integer statAgi;
    private Integer statWis;
    private Integer freeStatPoints;
    private Long coins;
    private Long spiritStones;
    private Integer breakthroughFailCount;
    private Double nextBreakthroughRate;
    private LocalDateTime afkStartTime;
    private Long meditationDurationMinutes;
}
