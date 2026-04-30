package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.EmotionState;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 福地整体状态值对象
 */
@Data
@Builder
public class FudiStatusVO {
    private Long fudiId;
    private Long userId;
    private Integer tribulationStage;
    private Integer totalCells;
    private MBTIPersonality mbtiType;
    private Integer spiritEnergy;
    private Integer spiritAffection;
    private Integer affectionMax;
    private Integer energyMax;
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
