package top.stillmisty.xiantao.domain.fudi.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地块详情值对象
 */
@Data
@Builder
public class CellDetailVO {
    private Integer cellId;
    private CellType type;
    private String name;
    private Integer cellLevel;
    private Integer level;
    private Double growthProgress;
    private Boolean isMature;
    private String quality;
    private List<String> mutationTraits;
    private Integer productionStored;
    private Boolean isIncubating;
    private LocalDateTime createTime;
}
