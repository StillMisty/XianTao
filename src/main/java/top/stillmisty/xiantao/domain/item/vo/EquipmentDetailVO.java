package top.stillmisty.xiantao.domain.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;

import java.util.List;
import java.util.Map;

/**
 * 装备详情 VO
 * 包含完整的波动信息
 */
@Data
@Builder
public class EquipmentDetailVO {

    /**
     * 装备ID
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 显示名称（含品质前缀）
     * 例如: "锋利的铁剑(稀有)"
     */
    @JsonProperty("display_name")
    private String displayName;

    /**
     * 模板ID
     */
    @JsonProperty("template_id")
    private Long templateId;

    /**
     * 品质
     */
    @JsonProperty("rarity")
    private Rarity rarity;

    /**
     * 品质名称
     */
    @JsonProperty("rarity_name")
    private String rarityName;

    /**
     * 品质表情符号
     */
    @JsonProperty("rarity_emoji")
    private String rarityEmoji;

    /**
     * 装备部位
     */
    @JsonProperty("slot")
    private EquipmentSlot slot;

    /**
     * 部位名称
     */
    @JsonProperty("slot_name")
    private String slotName;

    /**
     * 法器子类型
     */
    @JsonProperty("weapon_type")
    private WeaponType weaponType;

    /**
     * 法器子类型名称
     */
    @JsonProperty("weapon_type_name")
    private String weaponTypeName;

    /**
     * 品质系数（实际波动值）
     */
    @JsonProperty("quality_multiplier")
    private Double qualityMultiplier;

    /**
     * 锻造强化等级
     */
    @JsonProperty("forge_level")
    private Integer forgeLevel;

    /**
     * 最终攻击力（包含波动和锻造）
     */
    @JsonProperty("attack")
    private Integer attack;

    /**
     * 最终防御力（包含波动和锻造）
     */
    @JsonProperty("defense")
    private Integer defense;

    /**
     * 力量加成（基础+词条）
     */
    @JsonProperty("str_bonus")
    private Integer strBonus;

    /**
     * 体质加成（基础+词条）
     */
    @JsonProperty("con_bonus")
    private Integer conBonus;

    /**
     * 敏捷加成（基础+词条）
     */
    @JsonProperty("agi_bonus")
    private Integer agiBonus;

    /**
     * 智慧加成（基础+词条）
     */
    @JsonProperty("wis_bonus")
    private Integer wisBonus;

    /**
     * 随机词条 JSONB
     */
    @JsonProperty("affixes")
    private Map<String, Integer> affixes;

    /**
     * 词条描述列表（用于展示）
     * 例如: ["力量 +3", "敏捷 +2"]
     */
    @JsonProperty("affix_descriptions")
    private List<String> affixDescriptions;

    /**
     * 是否已穿戴
     */
    @JsonProperty("equipped")
    private Boolean equipped;
}
