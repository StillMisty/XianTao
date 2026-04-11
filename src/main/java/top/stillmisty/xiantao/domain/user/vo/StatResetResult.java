package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 洗点结果 VO
 */
@Data
@Builder
public class StatResetResult {

    private boolean success;
    private String message;

    // 重置的属性信息
    private Integer resetStr;
    private Integer resetCon;
    private Integer resetAgi;
    private Integer resetWis;

    // 重置后的状态
    private Integer totalFreePoints;
    private LocalDateTime nextResetTime;
    private Long cooldownHoursRemaining;
}
