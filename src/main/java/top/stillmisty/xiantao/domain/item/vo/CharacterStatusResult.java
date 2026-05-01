package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

import java.util.List;

/**
 * 角色状态查看结果 VO
 * 包含：HP、属性、装扮、境界进度、当前状态
 */
@Data
@Builder
public class CharacterStatusResult {

    private boolean success;
    private String message;

    private Long userId;
    private String nickname;

    // ===================== 境界进度 =====================
    private Integer level;
    private Long exp;
    private Long expToNextLevel;
    private Double expPercentage;

    // ===================== 当前状态 =====================
    private UserStatus status;
    private String statusName;
    private Long locationId;

    // ===================== HP =====================
    private Integer hpCurrent;
    private Integer hpMax;
    private Double hpPercentage;

    // ===================== 基础属性 =====================
    private Integer baseStr;
    private Integer baseCon;
    private Integer baseAgi;
    private Integer baseWis;

    // ===================== 装备加成 =====================
    private Integer equipStr;
    private Integer equipCon;
    private Integer equipAgi;
    private Integer equipWis;

    // ===================== 最终属性 =====================
    private Integer totalStr;
    private Integer totalCon;
    private Integer totalAgi;
    private Integer totalWis;

    // ===================== 战斗属性 =====================
    private Integer attack;
    private Integer defense;

    // ===================== 其他 =====================
    private Integer freeStatPoints;
    private Long spiritStones;

    // ===================== 突破相关 =====================
    private Double breakthroughSuccessRate; // 当前突破成功率
    private Integer breakthroughFailCount; // 突破失败次数

    // ===================== 护道相关 =====================
    private Integer protectorCount; // 正在为多少人护道
    private Integer maxProtectorCount; // 最大护道人数
    private List<ProtectionInfoVO> protectingList; // 正在为谁护道
    private List<ProtectionInfoVO> protectedByList; // 有谁在为自己护道
    private Double totalProtectionBonus; // 总护道加成

    // ===================== 装扮 =====================
    private EquipmentSummary equipment;

    @Data
    @Builder
    public static class EquipmentSummary {
        private Integer totalEquipped;
        private List<EquipmentSummaryItem> items;
    }

    @Data
    @Builder
    public static class EquipmentSummaryItem {
        private Long equipmentId;
        private String name;
        private EquipmentSlot slot;
        private String slotName;
        private Rarity rarity;
        private String rarityName;
        private Integer strBonus;
        private Integer conBonus;
        private Integer agiBonus;
        private Integer wisBonus;
        private Integer attackBonus;
        private Integer defenseBonus;
    }

    /**
     * 护道信息 VO
     */
    @Data
    @Builder
    public static class ProtectionInfoVO {
        private Long userId;
        private String userName;
        private Integer userLevel;
        private Long locationId;
        private String locationName;
        private Boolean isInSameLocation;
        private Double bonusPercentage;
    }
}
