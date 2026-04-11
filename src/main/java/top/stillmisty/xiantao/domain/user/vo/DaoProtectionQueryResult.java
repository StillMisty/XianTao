package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 护道查询结果 VO
 */
@Data
@Builder
public class DaoProtectionQueryResult {

    private boolean success;
    private String message;

    // 正在为谁护道 (作为护道者)
    private List<ProtectionInfo> protectingList;
    private Integer protectingCount;
    private Integer maxProtectingCount;

    // 有谁在为自己护道 (作为被护道者)
    private List<ProtectionInfo> protectedByList;
    private Double totalBonusPercentage;
    private Boolean allInSameLocation;
}

