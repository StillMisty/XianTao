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
     * 当前劫数
     */
    private Integer tribulationStage;

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
     * 天劫连续胜利次数
     */
    private Integer tribulationWinStreak;

    /**
     * 天劫最后发生时间
     */
    private LocalDateTime lastTribulationTime;

    /**
     * 下次天劫时间
     */
    private LocalDateTime nextTribulationTime;

    /**
     * 刚结算的天劫结果消息（非天劫期为null）
     */
    private String tribulationResult;

    /**
     * 网格布局详情
     */
    private List<CellDetailVO> cellDetails;

    /**
     * 灵兽总数
     */
    private Integer totalBeasts;

    /**
     * 灵气耗尽倒计时（所有灵兽蛰伏时的剩余时间，秒）
     */
    private Long auraDepleteCountdownSeconds;
}
