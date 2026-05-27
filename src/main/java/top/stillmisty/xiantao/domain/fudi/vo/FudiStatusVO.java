package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

/** 福地整体状态值对象 */
public record FudiStatusVO(
    Long fudiId,
    Long userId,
    Integer tribulationStage,
    Integer totalCells,
    MBTIPersonality mbtiType,
    Integer spiritAffection,
    Integer affectionMax,
    String spiritForm,
    String spiritFormName,
    List<String> likedTags,
    List<String> dislikedTags,
    Integer occupiedCells,
    Integer tribulationWinStreak,
    LocalDateTime lastTribulationTime,
    String tribulationResult,
    List<CellDetailVO> cellDetails,
    Integer totalBeasts) {}
