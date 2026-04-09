package top.stillmisty.xiantao.domain.item.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 装备波动生成器
 * 根据装备模板生成带波动的装备实例
 */
@Slf4j
@Component
public class EquipmentGenerator {

    /**
     * 根据装备模板生成装备实例
     *
     * @param userId   用户ID
     * @param template 装备模板
     * @return 生成的装备实例
     */
    public Equipment generate(UUID userId, ItemTemplate template) {
        // 1. 根据掉落权重随机决定品质
        Rarity rarity = rollRarity(template);

        // 2. 根据品质随机波动系数
        double qualityMultiplier = rarity.getRandomQualityMultiplier();

        // 3. 计算最终基础属性
        int finalAttack = calculateFluctuatedStat(template.getBaseAttack(), qualityMultiplier);
        int finalDefense = calculateFluctuatedStat(template.getBaseDefense(), qualityMultiplier);

        // 4. 随机生成前缀
        String prefix = rarity.getRandomPrefix();

        // 5. 随机生成词条
        Map<String, Integer> affixes = rollAffixes(rarity, template.getEquipLevel());

        // 6. 组合显示名称
        String displayName = buildDisplayName(template.getName(), prefix, rarity);

        // 7. 创建装备实例
        Equipment equipment = Equipment.create(
                userId,
                template.getId(),
                displayName,
                template.getSlot(),
                rarity,
                qualityMultiplier,
                affixes,
                template.getBaseStats(),
                finalAttack,
                finalDefense
        );

        log.debug("生成装备: {} (品质: {}, 系数: {:.2f}, 攻击: {}, 防御: {}, 词条: {})",
                displayName, rarity.getName(), qualityMultiplier,
                finalAttack, finalDefense, affixes);

        return equipment;
    }

    /**
     * 指定品质生成装备实例（用于测试或特殊场景）
     *
     * @param userId   用户ID
     * @param template 装备模板
     * @param rarity   指定品质
     * @return 生成的装备实例
     */
    public Equipment generateWithRarity(UUID userId, ItemTemplate template, Rarity rarity) {
        double qualityMultiplier = rarity.getRandomQualityMultiplier();
        int finalAttack = calculateFluctuatedStat(template.getBaseAttack(), qualityMultiplier);
        int finalDefense = calculateFluctuatedStat(template.getBaseDefense(), qualityMultiplier);
        String prefix = rarity.getRandomPrefix();
        Map<String, Integer> affixes = rollAffixes(rarity, template.getEquipLevel());
        String displayName = buildDisplayName(template.getName(), prefix, rarity);

        return Equipment.create(
                userId,
                template.getId(),
                displayName,
                template.getSlot(),
                rarity,
                qualityMultiplier,
                affixes,
                template.getBaseStats(),
                finalAttack,
                finalDefense
        );
    }

    /**
     * 根据掉落权重随机决定品质
     */
    private Rarity rollRarity(ItemTemplate template) {
        Map<String, Integer> dropWeight = template.getDropWeight();
        if (dropWeight == null || dropWeight.isEmpty()) {
            return Rarity.COMMON; // 默认品质
        }

        // 计算总权重
        int totalWeight = dropWeight.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight == 0) {
            return Rarity.COMMON;
        }

        // 随机抽取
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (Rarity rarity : Rarity.values()) {
            Integer weight = dropWeight.get(rarity.getCode());
            if (weight != null) {
                currentWeight += weight;
                if (random < currentWeight) {
                    return rarity;
                }
            }
        }

        return Rarity.COMMON;
    }

    /**
     * 计算波动后的属性值
     */
    private int calculateFluctuatedStat(Integer baseStat, double multiplier) {
        if (baseStat == null || baseStat == 0) return 0;
        return (int) Math.floor(baseStat * multiplier);
    }

    /**
     * 随机生成词条
     */
    private Map<String, Integer> rollAffixes(Rarity rarity, Integer equipLevel) {
        int affixCount = rarity.getRandomAffixCount();
        if (affixCount == 0) {
            return Collections.emptyMap();
        }

        Map<String, Integer> affixes = new HashMap<>();

        // 获取可用的词条类型池
        AffixType[] attributeAffixes = AffixType.getAttributeAffixes();
        AffixType[] specialAffixes = AffixType.getSpecialAffixes();

        // 随机选择词条
        List<AffixType> availableAffixes = new ArrayList<>(Arrays.asList(attributeAffixes));

        // 金装可以添加特殊词条
        if (rarity == Rarity.LEGENDARY) {
            availableAffixes.addAll(Arrays.asList(specialAffixes));
        }

        Collections.shuffle(availableAffixes);

        for (int i = 0; i < Math.min(affixCount, availableAffixes.size()); i++) {
            AffixType affixType = availableAffixes.get(i);

            if (affixType.isSpecial()) {
                // 特殊词条固定值
                affixes.put(affixType.getCode(), 5);
            } else {
                // 属性词条根据装备等级计算数值
                int statValue = calculateAffixValue(affixType, equipLevel);
                affixes.put(affixType.getStatField(), statValue);
            }
        }

        return affixes;
    }

    /**
     * 计算词条数值
     * 根据装备等级和词条类型计算合理的数值范围
     */
    private int calculateAffixValue(AffixType affixType, Integer equipLevel) {
        int level = equipLevel != null ? equipLevel : 10;

        // 基础值：每级约0.5-1点
        int baseValue = (int) Math.ceil(level * 0.8);

        // 随机波动 ±20%
        int random = ThreadLocalRandom.current().nextInt(-20, 21);
        int finalValue = (int) Math.floor(baseValue * (1 + random / 100.0));

        return Math.max(1, finalValue); // 最小值为1
    }

    /**
     * 构建显示名称
     */
    private String buildDisplayName(String baseName, String prefix, Rarity rarity) {
        if (prefix == null || prefix.isEmpty()) {
            return baseName + "(" + rarity.getName() + ")";
        }
        return prefix + baseName + "(" + rarity.getName() + ")";
    }
}
