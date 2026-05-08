package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;

/** 旅行结果 VO */
@Data
@Builder
public class TravelResultVO {
  /** 是否成功 */
  private boolean success;

  /** 消息 */
  private String message;

  /** 源地图 ID */
  private Long fromMapId;

  /** 源地图名称 */
  private String fromMapName;

  /** 目的地地图 ID */
  private Long toMapId;

  /** 目的地地图名称 */
  private String toMapName;

  /** 旅行耗时（分钟） */
  private Integer travelTimeMinutes;

  /** 是否已到达 */
  private boolean arrived;

  /** 预计到达时间（仅真实时间模式） */
  private java.time.LocalDateTime estimatedArrivalTime;
}
