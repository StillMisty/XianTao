package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 突破结果 VO
 */
@Data
@Builder
public class BreakthroughResult {

    private boolean success;
    private String message;
    private Boolean breakthroughSuccess;
    private Double successRate;
    private Integer newLevel;
    private Integer freeStatPoints;
    private Integer failCount;
    private Double nextBreakthroughRate;
    private UserStatusVO userStatus;
}
