package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

/** 福地整体状态值对象 */
public record FudiStatusVO(
    Long fudiId,
    Long userId,
    Integer tribulationStage,
    Integer totalCells,
    @Nullable MBTIPersonality mbtiType,
    @Nullable Integer spiritAffection,
    @Nullable Integer affectionMax,
    @Nullable String spiritForm,
    @Nullable String spiritFormName,
    @Nullable List<String> likedTags,
    @Nullable List<String> dislikedTags,
    Integer occupiedCells,
    Integer tribulationWinStreak,
    @Nullable LocalDateTime lastTribulationTime,
    @Nullable String tribulationResult,
    List<CellDetailVO> cellDetails,
    Integer totalBeasts) {}
