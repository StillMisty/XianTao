package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 打坐结束结果 VO
 */
@Data
@Builder
public class MeditationEndResult {
    
    private boolean success;
    private String message;
    private Long durationMinutes;
    private Integer hpRestored;
    private Long expGained;
    private Boolean leveledUp;
    private Boolean expCapReached;
    private UserStatusVO userStatus;
}
