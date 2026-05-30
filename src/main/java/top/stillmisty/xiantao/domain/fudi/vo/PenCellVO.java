package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** 兽栏地块值对象 */
public record PenCellVO(
    Integer cellId,
    Integer cellLevel,
    @Nullable Long beastId,
    @Nullable String beastName,
    Integer tier,
    String quality,
    Integer qualityOrdinal,
    List<String> mutationTraits,
    boolean isIncubating,
    @Nullable LocalDateTime hatchTime,
    @Nullable LocalDateTime matureTime,
    double productionIntervalHours,
    Integer productionStored,
    Integer powerScore,
    @Nullable LocalDateTime birthTime) {}
