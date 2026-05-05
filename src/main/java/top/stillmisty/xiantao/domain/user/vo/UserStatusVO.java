package top.stillmisty.xiantao.domain.user.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

/** 用户状态 VO */
@Data
@Builder
public class UserStatusVO {

  private Long userId;
  private String nickname;
  private Integer level;
  private Long exp;
  private Long expToNextLevel;
  private UserStatus status;
  private String statusName;
  private Integer hpCurrent;
  private Integer hpMax;
  private Double hpPercentage;
  private Integer statValue;
  private Long spiritStones;
  private Integer breakthroughFailCount;
  private Double nextBreakthroughRate;
  private LocalDateTime trainingStartTime;
  private Long meditationDurationMinutes;
}
