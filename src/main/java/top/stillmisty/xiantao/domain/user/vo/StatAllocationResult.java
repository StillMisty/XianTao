package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

import top.stillmisty.xiantao.domain.user.enums.AttributeType;

/**
 * 属性分配结果 VO
 */
@Data
@Builder
public class StatAllocationResult {
    
    private boolean success;
    private String message;
    private AttributeType statType;
    private Integer pointsAllocated;
    private Integer remainingFreePoints;
    private Integer currentStr;
    private Integer currentCon;
    private Integer currentAgi;
    private Integer currentWis;
    private UserStatusVO userStatus;
}
