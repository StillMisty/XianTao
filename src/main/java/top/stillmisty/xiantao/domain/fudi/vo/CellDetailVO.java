package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;

/** 地块详情值对象 */
@Builder
public record CellDetailVO(
    Integer cellId,
    CellType type,
    @Nullable String name,
    Integer cellLevel,
    Integer level,
    @Nullable Double growthProgress,
    @Nullable Boolean isMature,
    @Nullable String quality,
    List<String> mutationTraits,
    Integer productionStored,
    @Nullable Boolean isIncubating,
    @Nullable LocalDateTime createTime) {}
