package top.stillmisty.xiantao.domain.item.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;

/** 装备工厂 - 领域服务 封装装备创建的核心业务逻辑（词条生成、属性计算） 不依赖仓库，纯业务逻辑 */
@Component
public class EquipmentFactory {

  /**
   * 根据模板和稀有度创建装备实例
   *
   * @param userId 持有者用户ID
   * @param templateId 装备模板ID
   * @param template 装备模板
   * @param rarity 稀有度
   * @return 创建的装备实例（未持久化）
   */
  public Equipment createEquipment(
      Long userId, Long templateId, EquipmentTemplate template, Rarity rarity) {
    double qualityMultiplier = rarity.randomQualityMultiplier();
    Map<String, Integer> affixes = generateAffixes(rarity);
    Map<String, Integer> statBonus = calculateStatBonus(template);
    String name = template.getName() + "-" + rarity.getName();

    return Equipment.create(
        userId,
        templateId,
        name,
        template.getSlot(),
        rarity,
        template.getWeaponType(),
        qualityMultiplier,
        affixes,
        statBonus,
        template.getBaseAttack(),
        template.getBaseDefense());
  }

  /**
   * 生成随机词条
   *
   * @param rarity 稀有度，决定词条数量和池
   * @return 词条映射（属性名 -> 数值）
   */
  public Map<String, Integer> generateAffixes(Rarity rarity) {
    int affixCount = rarity.randomAffixCount();
    Map<String, Integer> affixes = new LinkedHashMap<>();
    List<AffixType> pool = buildAffixPool(rarity);

    Collections.shuffle(pool, ThreadLocalRandom.current());
    for (int i = 0; i < affixCount && i < pool.size(); i++) {
      AffixType affixType = pool.get(i);
      int value = generateAffixValue(affixType);
      String key = affixType.getStatField() != null ? affixType.getStatField() : affixType.name();
      affixes.put(key, value);
    }

    return affixes;
  }

  /**
   * 构建词条池
   *
   * @param rarity 稀有度，传说级包含特殊词条
   * @return 可用的词条类型列表
   */
  private List<AffixType> buildAffixPool(Rarity rarity) {
    List<AffixType> pool = new ArrayList<>(List.of(AffixType.getAttributeAffixes()));
    if (rarity == Rarity.LEGENDARY) {
      pool.addAll(List.of(AffixType.getSpecialAffixes()));
    }
    return pool;
  }

  /**
   * 生成单个词条的数值
   *
   * @param affixType 词条类型
   * @return 词条数值
   */
  private int generateAffixValue(AffixType affixType) {
    return affixType.isSpecial() ? 5 : (1 + ThreadLocalRandom.current().nextInt(4));
  }

  /**
   * 计算装备属性加成
   *
   * @param template 装备模板
   * @return 属性加成映射
   */
  public Map<String, Integer> calculateStatBonus(EquipmentTemplate template) {
    return Map.of(
        "STR", template.getBaseStr(),
        "CON", template.getBaseCon(),
        "AGI", template.getBaseAgi(),
        "WIS", template.getBaseWis());
  }
}
