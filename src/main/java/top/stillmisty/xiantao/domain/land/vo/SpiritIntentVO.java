package top.stillmisty.xiantao.domain.land.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 地灵意图识别结果 VO
 * 用于存储 LLM 解析后的玩家指令意图
 */
@Data
@Builder
public class SpiritIntentVO {

    /**
     * 意图类型
     */
    private IntentType intentType;

    /**
     * 意图参数（如坐标、物品名称、作物名称等）
     */
    private Map<String, String> parameters;

    /**
     * 置信度（0.0 - 1.0）
     */
    private Double confidence;

    /**
     * 原始用户输入
     */
    private String originalInput;

    /**
     * 意图类型枚举
     */
    public enum IntentType {
        /** 种植灵药 */
        PLANT,
        /** 收获灵药 */
        HARVEST,
        /** 建造地块 */
        BUILD,
        /** 拆除地块 */
        REMOVE,
        /** 献祭物品 */
        SACRIFICE,
        /** 喂养灵兽 */
        FEED,
        /** 查看福地状态 */
        STATUS,
        /** 查看网格布局 */
        GRID,
        /** 查看灵气详情 */
        AURA,
        /** 查看地灵信息 */
        SPIRIT_INFO,
        /** 升级福地 */
        UPGRADE,
        /** 扩建福地 */
        EXPAND,
        /** 切换自动模式 */
        AUTO_MODE,
        /** 普通对话（无操作意图） */
        CHAT,
        /** 无法识别 */
        UNKNOWN
    }
}
