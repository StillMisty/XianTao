package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 灵田地块值对象 */
@Data
@Builder
public class FarmCellVO {
  private Integer cellId;
  private Integer cellLevel;
  private Integer cropId;
  private String cropName;
  private LocalDateTime plantTime;
  private LocalDateTime matureTime;
  private Double growthProgress;
  private Boolean isMature;
  private Double baseGrowthHours;
  private Double actualGrowthHours;
  private Integer harvestCount;
  private Integer maxHarvest;
  private boolean isPerennial;
}
