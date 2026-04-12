package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 兽栏地块值对象
 */
@Data
@Builder
public class PenCellVO {
    /**
     * 坐标位置（格式："x,y"）
     */
    private String position;

    /**
     * 灵兽ID
     */
    private Integer beastId;

    /**
     * 灵兽名称
     */
    private String beastName;

    /**
     * 灵兽等阶
     */
    private Integer beastTier;

    /**
     * 当前饥饿值（0-100）
     */
    private Integer hunger;

    /**
     * 是否饥饿（饥饿值 < 30）
     */
    private Boolean isHungry;

    /**
     * 上次喂食时间
     */
    private String lastFeedTime;

    /**
     * 战力评分
     */
    private Integer powerScore;
}
