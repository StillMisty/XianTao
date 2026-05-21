package top.stillmisty.xiantao.domain.map.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;

/** 历练奖励 VO */
@Data
@Builder
public class TrainingRewardVO {
  /** 用户 ID */
  private Long userId;

  /** 历练地图 ID */
  private Long mapId;

  /** 历练地图名称 */
  private String mapName;

  /** 历练时长（分钟） */
  private Long durationMinutes;

  /** 效率倍率（基于敏捷） */
  private Double efficiencyMultiplier;

  /** 等级衰减倍率 */
  private Double levelDecayMultiplier;

  /** 获得的灵石 */
  private Long spiritStones;

  /** 获得的修为 */
  private Long exp;

  /** 获得的物品 */
  private List<DropItem> items;

  /** 总奖励描述 */
  private String summary;
}
