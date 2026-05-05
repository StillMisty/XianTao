package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 装备实例实体 */
@Data
@Table("xt_equipment")
public class Equipment {

  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 持有者用户ID */
  private Long userId;

  /** 物品模板ID (关联静态配置) */
  private Long templateId;

  /** 装备名称 (从模板复制或自定义，包含品质前缀) */
  private String name;

  /** 装备部位 */
  private EquipmentSlot slot;

  /** 品质（稀有度） */
  private Rarity rarity;

  /** 法器子类型（护甲/饰品为 null） */
  private WeaponType weaponType;

  /** 品质系数（实际波动值，如1.35） */
  private Double qualityMultiplier;

  /** 随机词条 JSONB: {"STR": 3, "AGI": 2, "LIFE_STEAL": 5} */
  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Integer> affixes;

  /** 锻造强化等级 */
  private Integer forgeLevel;

  /** 属性加成 JSONB: {"str": 5, "con": 3, "agi": 2, "wis": 0} 注：此字段保留用于存储基础属性加成，最终属性需加上词条和锻造加成 */
  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Integer> statBonus;

  /** 攻击力加成（基础值，不包含波动和锻造） */
  private Integer attackBonus;

  /** 防御力加成（基础值，不包含波动和锻造） */
  private Integer defenseBonus;

  /** 是否已穿戴 */
  private Boolean equipped;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务逻辑方法 =====================

  /** 创建装备实例 */
  public static Equipment create(
      Long userId,
      Long templateId,
      String name,
      EquipmentSlot slot,
      Rarity rarity,
      WeaponType weaponType,
      Double qualityMultiplier,
      Map<String, Integer> affixes,
      Map<String, Integer> statBonus,
      Integer attackBonus,
      Integer defenseBonus) {
    Equipment equipment = new Equipment();
    equipment.userId = userId;
    equipment.templateId = templateId;
    equipment.name = name;
    equipment.slot = slot;
    equipment.rarity = rarity;
    equipment.weaponType = weaponType;
    equipment.qualityMultiplier = qualityMultiplier;
    equipment.affixes = affixes;
    equipment.statBonus = statBonus;
    equipment.attackBonus = attackBonus;
    equipment.defenseBonus = defenseBonus;
    equipment.forgeLevel = 0;
    equipment.equipped = false;
    equipment.createTime = LocalDateTime.now();
    return equipment;
  }

  /** 获取最终攻击力（包含品质波动和锻造加成） */
  public int getFinalAttack() {
    int base = attackBonus != null ? attackBonus : 0;
    int fluctuated = getFluctuatedAttack();
    int forgeBonus = getForgeBonus();
    return base + fluctuated + forgeBonus;
  }

  /** 获取波动后的攻击力 */
  private int getFluctuatedAttack() {
    if (attackBonus == null || qualityMultiplier == null) return 0;
    return (int) Math.floor(attackBonus * qualityMultiplier);
  }

  /** 获取锻造加成（每级+5点攻击力） */
  private int getForgeBonus() {
    if (forgeLevel == null) return 0;
    return forgeLevel * 5;
  }

  /** 获取最终防御力（包含品质波动和锻造加成） */
  public int getFinalDefense() {
    int base = defenseBonus != null ? defenseBonus : 0;
    int fluctuated = getFluctuatedDefense();
    int forgeBonus = getForgeBonus();
    return base + fluctuated + forgeBonus;
  }

  /** 获取波动后的防御力 */
  private int getFluctuatedDefense() {
    if (defenseBonus == null || qualityMultiplier == null) return 0;
    return (int) Math.floor(defenseBonus * qualityMultiplier);
  }

  /** 获取指定属性的总加成（基础+词条） */
  public int getTotalStatBonus(String statName) {
    return getBaseStatBonus(statName) + getAffixBonus(statName);
  }

  /** 获取基础属性加成 */
  public int getBaseStatBonus(String statName) {
    if (statBonus == null) return 0;
    return statBonus.getOrDefault(statName.toLowerCase(), 0);
  }

  /** 获取词条属性加成 */
  public int getAffixBonus(String statName) {
    if (affixes == null) return 0;
    return affixes.getOrDefault(statName.toUpperCase(), 0);
  }

  /** 获取力量加成 */
  public int getStrBonus() {
    return getTotalStatBonus("str");
  }

  /** 获取体质加成 */
  public int getConBonus() {
    return getTotalStatBonus("con");
  }

  /** 获取敏捷加成 */
  public int getAgiBonus() {
    return getTotalStatBonus("agi");
  }

  /** 获取智慧加成 */
  public int getWisBonus() {
    return getTotalStatBonus("wis");
  }

  /** 是否含有特殊词条（吸血、寻宝等） */
  public boolean hasSpecialAffix(String affixCode) {
    if (affixes == null) return false;
    return affixes.containsKey(affixCode.toUpperCase());
  }

  /** 获取特殊词条值 */
  public Integer getSpecialAffixValue(String affixCode) {
    if (affixes == null) return null;
    return affixes.get(affixCode.toUpperCase());
  }
}
