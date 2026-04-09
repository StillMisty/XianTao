package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

import java.util.List;
import java.util.UUID;

/**
 * 角色状态查看结果 VO
 * 包含：HP、属性、装扮、境界进度、当前状态
 */
@Data
@Builder
public class CharacterStatusResult {

    private boolean success;
    private String message;

    private UUID userId;
    private String nickname;

    // ===================== 境界进度 =====================
    private Integer level;
    private Long exp;
    private Long expToNextLevel;
    private Double expPercentage;

    // ===================== 当前状态 =====================
    private UserStatus status;
    private String statusName;
    private String locationId;

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
    private Long coins;
    private Long spiritStones;

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
        private UUID equipmentId;
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
}
