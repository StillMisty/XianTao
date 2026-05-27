package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;
import java.util.List;

/** 兽栏地块值对象 */
public record PenCellVO(
    Integer cellId,
    Integer cellLevel,
    Long beastId,
    String beastName,
    Integer tier,
    String quality,
    Integer qualityOrdinal,
    List<String> mutationTraits,
    boolean isIncubating,
    LocalDateTime hatchTime,
    LocalDateTime matureTime,
    double productionIntervalHours,
    Integer productionStored,
    Integer powerScore,
    LocalDateTime birthTime) {}
