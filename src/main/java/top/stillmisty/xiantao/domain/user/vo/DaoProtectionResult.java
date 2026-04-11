package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 护道结果 VO
 */
@Data
@Builder
public class DaoProtectionResult {

    private boolean success;
    private String message;

    // 护道关系信息
    private Long protectorId;
    private String protectorName;
    private Integer protectorLevel;

    private Long protegeId;
    private String protegeName;
    private Integer protegeLevel;

    // 加成信息
    private Double singleProtectorBonus;
    private Double totalBonus;
    private Boolean isInSameLocation;
}
