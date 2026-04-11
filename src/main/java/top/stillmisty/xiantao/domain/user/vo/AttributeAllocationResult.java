package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.user.enums.AttributeType;

/**
 * 属性分配结果 VO
 */
@Data
@Builder
public class AttributeAllocationResult {

    private boolean success;
    private String message;

    // 分配的属性信息
    private AttributeType attributeType;
    private String attributeName;
    private Integer allocatedPoints;

    // 分配后的状态
    private Integer remainingPoints;
    private Integer currentAttributeValue;
}
