package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.map.enums.TravelEventType;

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

  /** 是否使用体力模式 */
  private boolean useStamina;

  /** 消耗的体力 */
  private Integer staminaCost;

  /** 旅行耗时（分钟） */
  private Integer travelTimeMinutes;

  /** D20 骰点结果 */
  private Integer d20Roll;

  /** 触发的事件类型 */
  private TravelEventType eventType;

  /** 事件描述 */
  private String eventDescription;

  /** 是否已到达 */
  private boolean arrived;

  /** 预计到达时间（仅真实时间模式） */
  private java.time.LocalDateTime estimatedArrivalTime;
}
