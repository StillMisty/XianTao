package top.stillmisty.xiantao.domain.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 装备列表结果 VO */
@Data
@Builder
public class EquipmentListResult {

  /** 是否成功 */
  private Boolean success;

  /** 消息 */
  private String message;

  /** 用户ID */
  @JsonProperty("user_id")
  private Long userId;

  /** 装备列表 */
  private List<EquipmentDetailVO> equipments;

  /** 装备总数 */
  @JsonProperty("total_count")
  private Integer totalCount;
}
