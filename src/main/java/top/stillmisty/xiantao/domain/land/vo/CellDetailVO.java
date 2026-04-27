package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地块详情值对象
 */
@Data
@Builder
public class CellDetailVO {
    private String position;
    private CellType type;
    private WuxingType element;
    private String name;
    private Integer cellLevel;
    private Integer level;
    private Double growthProgress;
    private Boolean isMature;
    private String quality;
    private List<String> mutationTraits;
    private Integer productionStored;
    private Boolean isIncubating;
    private Integer durability;
    private LocalDateTime createTime;
}
