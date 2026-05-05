package top.stillmisty.xiantao.domain.user.vo;

import lombok.Builder;
import lombok.Data;

/** 护道信息内部类 */
@Data
@Builder
public class ProtectionInfo {
  private Long userId;
  private String userName;
  private Integer userLevel;
  private Long locationId;
  private String locationName;
  private Boolean isInSameLocation;
  private Double bonusPercentage;
}
