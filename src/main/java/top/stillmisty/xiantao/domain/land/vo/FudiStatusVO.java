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
    /**
     * 福地ID
     */
    private Long fudiId;

    /**
     * 玩家ID
     */
    private Long userId;

    /**
     * 当前灵气值
     */
    private Integer auraCurrent;

    /**
     * 灵气上限
     */
    private Integer auraMax;

    /**
     * 每小时消耗
     */
    private Integer auraHourlyCost;

    /**
     * 聚灵核心等级
     */
    private Integer coreLevel;

    /**
     * 福地网格大小
     */
    private Integer gridSize;

    /**
     * 地灵MBTI人格
     */
    private MBTIPersonality mbtiType;

    /**
     * 地灵精力值
     */
    private Integer spiritEnergy;

    /**
     * 地灵好感度
     */
    private Integer spiritAffection;

    /**
     * 地灵情绪状态
     */
    private EmotionState emotionState;

    /**
     * 是否自动管理模式
     */
    private Boolean autoMode;

    /**
     * 是否蛰伏模式
     */
    private Boolean dormantMode;

    /**
     * 已占地块数
     */
    private Integer occupiedCells;

    /**
     * 焦土地块数
     */
    private Integer scorchedCells;

    /**
     * 天劫最后发生时间
     */
    private LocalDateTime lastTribulationTime;

    /**
     * 下次天劫时间
     */
    private LocalDateTime nextTribulationTime;

    /**
     * 网格布局详情
     */
    private List<CellDetailVO> cellDetails;
}
