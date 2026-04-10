package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

import java.util.Map;

/**
 * 用户战斗属性 VO (包含装备加成)
 */
@Data
@Builder
public class UserCombatStatsVO {

    private Long userId;
    private String nickname;

    // 基础属性
    private int baseStr;
    private int baseCon;
    private int baseAgi;
    private int baseWis;

    // 装备加成
    private int equipStr;
    private int equipCon;
    private int equipAgi;
    private int equipWis;

    // 最终属性 (基础 + 装备)
    private int totalStr;
    private int totalCon;
    private int totalAgi;
    private int totalWis;

    // 战斗属性
    private int attack;
    private int defense;

    // HP
    private int hpCurrent;
    private int hpMax;

    // 已穿戴装备
    private Map<EquipmentSlot, EquipmentDetailVO> equippedItems;
}
