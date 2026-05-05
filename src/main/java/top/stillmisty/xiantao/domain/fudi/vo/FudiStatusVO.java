package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

/** 福地整体状态值对象 */
@Data
@Builder
public class FudiStatusVO {
  private Long fudiId;
  private Long userId;
  private Integer tribulationStage;
  private Integer totalCells;
  private MBTIPersonality mbtiType;
  private Integer spiritAffection;
  private Integer affectionMax;
  private String spiritForm;
  private String spiritFormName;
  private List<String> likedTags;
  private List<String> dislikedTags;
  private EmotionState emotionState;
  private Integer occupiedCells;
  private Integer tribulationWinStreak;
  private LocalDateTime lastTribulationTime;
  private LocalDateTime nextTribulationTime;
  private String tribulationResult;
  private List<CellDetailVO> cellDetails;
  private Integer totalBeasts;
}
