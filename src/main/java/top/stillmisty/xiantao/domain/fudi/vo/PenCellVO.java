package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 兽栏地块值对象 */
@Data
@Builder
public class PenCellVO {
  private Integer cellId;
  private Integer cellLevel;
  private Long beastId;
  private String beastName;
  private Integer tier;
  private String quality;
  private Integer qualityOrdinal;
  private boolean isMutant;
  private List<String> mutationTraits;
  private boolean isIncubating;
  private LocalDateTime hatchTime;
  private LocalDateTime matureTime;
  private double productionIntervalHours;
  private Integer productionStored;
  private Integer powerScore;
  private LocalDateTime birthTime;
}
