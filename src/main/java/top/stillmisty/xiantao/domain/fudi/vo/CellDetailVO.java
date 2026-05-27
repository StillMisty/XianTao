package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;

/** 地块详情值对象 */
@Builder
public record CellDetailVO(
    Integer cellId,
    CellType type,
    String name,
    Integer cellLevel,
    Integer level,
    Double growthProgress,
    Boolean isMature,
    String quality,
    List<String> mutationTraits,
    Integer productionStored,
    Boolean isIncubating,
    LocalDateTime createTime) {}
