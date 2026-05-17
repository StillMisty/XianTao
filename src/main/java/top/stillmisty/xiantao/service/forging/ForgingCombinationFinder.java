package top.stillmisty.xiantao.service.forging;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.forge.vo.ForgingResultVO;
import top.stillmisty.xiantao.domain.item.entity.*;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.CombinationStrategy;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/** 锻造组合算法 — 自动匹配锻材、计算三性配比、品质分→稀有度映射 */
@Component
@RequiredArgsConstructor
public class ForgingCombinationFinder {

  private static final List<String> FORGE_ATTRIBUTES = List.of("RIGIDITY", "TOUGHNESS", "SPIRIT");

  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final top.stillmisty.xiantao.domain.item.repository.EquipmentRepository
      equipmentRepository;

  private final CombinationStrategy strategy =
      new CombinationStrategy(FORGE_ATTRIBUTES, ForgingCombinationFinder::getMaterialValue);

  public ItemProperties.ForgingBlueprint getForgingBlueprint(ItemTemplate template) {
    var props = template.typedProperties();
    if (props instanceof ItemProperties.ForgingBlueprint fb) return fb;
    return null;
  }

  @Transactional
  public ForgingResultVO forgeEquipment(
      Long userId,
      List<StackableItem> materials,
      Map<String, ElementRange> requirements,
      ItemTemplate blueprintTemplate,
      long equipmentTemplateId) {
    Map<String, Integer> attributeTotals = new HashMap<>();
    Map<String, Integer> usedMaterials = new LinkedHashMap<>();
    Map<StackableItem, Integer> remainingQuantities = new HashMap<>();
    for (StackableItem mat : materials) {
      remainingQuantities.put(mat, mat.getQuantity());
    }

    strategy.tryFindBestCombination(
        requirements, materials, attributeTotals, usedMaterials, remainingQuantities);

    List<String> missingAttributes =
        strategy.collectMissingAttributes(requirements, attributeTotals);
    if (!missingAttributes.isEmpty()) {
      return new ForgingResultVO(
          false,
          "缺少锻材属性：" + String.join(", ", missingAttributes),
          null,
          null,
          null,
          0.0,
          null,
          attributeTotals);
    }

    if (strategy.exceedsAttributeMax(requirements, attributeTotals)) {
      String overAttribute = strategy.findOverMaxAttribute(requirements, attributeTotals);
      return new ForgingResultVO(
          false,
          "锻材属性超过上限：" + overAttribute,
          null,
          null,
          null,
          0.0,
          usedMaterials,
          attributeTotals);
    }

    return createEquipmentFromForge(
        userId, blueprintTemplate, equipmentTemplateId, attributeTotals, usedMaterials, materials);
  }

  public record MaterialSelectionResult(
      boolean success,
      Map<String, Integer> usedMaterials,
      Map<String, Integer> attributeTotals,
      List<String> missingAttributes) {}

  public MaterialSelectionResult findBestMaterials(
      Map<String, ElementRange> requirements, List<StackableItem> materials) {
    Map<String, Integer> attributeTotals = new HashMap<>();
    Map<String, Integer> usedMaterials = new LinkedHashMap<>();
    Map<StackableItem, Integer> remainingQuantities = new HashMap<>();
    for (StackableItem mat : materials) {
      remainingQuantities.put(mat, mat.getQuantity());
    }

    strategy.tryFindBestCombination(
        requirements, materials, attributeTotals, usedMaterials, remainingQuantities);

    List<String> missingAttributes =
        strategy.collectMissingAttributes(requirements, attributeTotals);
    return new MaterialSelectionResult(
        missingAttributes.isEmpty(), usedMaterials, attributeTotals, missingAttributes);
  }

  public double calculateQualityScore(
      Map<String, Integer> attributeTotals, Map<String, ElementRange> requirements) {
    return strategy.calculateQualityScore(attributeTotals, requirements);
  }

  public Rarity rollRarityByQualityScore(double qualityScore, int grade) {
    double finalScore = qualityScore + grade * 0.02;

    Map<String, Integer> pool;
    if (finalScore >= 0.9) {
      pool = Map.of("LEGENDARY", 10, "EPIC", 40);
    } else if (finalScore >= 0.7) {
      pool = Map.of("EPIC", 30, "RARE", 40, "COMMON", 10);
    } else if (finalScore >= 0.5) {
      pool = Map.of("RARE", 20, "COMMON", 50, "BROKEN", 10);
    } else {
      pool = Map.of("COMMON", 30, "BROKEN", 50);
    }

    return Rarity.roll(pool);
  }

  private ForgingResultVO createEquipmentFromForge(
      Long userId,
      ItemTemplate blueprintTemplate,
      long equipmentTemplateId,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials,
      List<StackableItem> materials) {
    var blueprint = getForgingBlueprint(blueprintTemplate);
    if (blueprint == null) {
      return new ForgingResultVO(false, "锻造图纸数据错误", null, null, null, 0.0, null, null);
    }

    double qualityScore = strategy.calculateQualityScore(attributeTotals, blueprint.requirements());
    Rarity rarity = rollRarityByQualityScore(qualityScore, blueprint.grade());

    for (Map.Entry<String, Integer> entry : usedMaterials.entrySet()) {
      for (StackableItem mat : materials) {
        if (mat.getName().equals(entry.getKey())) {
          stackableItemService.reduceStackableItem(userId, mat.getId(), entry.getValue());
          break;
        }
      }
    }

    var equipTmpl = equipmentTemplateRepository.findById(equipmentTemplateId).orElse(null);
    if (equipTmpl == null) {
      return new ForgingResultVO(false, "装备模板不存在", null, null, null, 0.0, null, null);
    }

    double qm = rarity.randomQualityMultiplier();
    int affixCount = rarity.randomAffixCount();
    Map<String, Integer> affixes = new LinkedHashMap<>();
    List<AffixType> pool = new ArrayList<>(List.of(AffixType.getAttributeAffixes()));
    if (rarity == Rarity.LEGENDARY) {
      pool.addAll(List.of(AffixType.getSpecialAffixes()));
    }
    Collections.shuffle(pool, ThreadLocalRandom.current());
    for (int i = 0; i < affixCount && i < pool.size(); i++) {
      AffixType at = pool.get(i);
      int value = at.isSpecial() ? 5 : (1 + ThreadLocalRandom.current().nextInt(4));
      if (at.getStatField() != null) {
        affixes.put(at.getStatField(), value);
      } else {
        affixes.put(at.name(), value);
      }
    }

    String name = rarity.randomPrefix() + equipTmpl.getName();

    Map<String, Integer> statBonus =
        Map.of(
            "STR", equipTmpl.getBaseStr(),
            "CON", equipTmpl.getBaseCon(),
            "AGI", equipTmpl.getBaseAgi(),
            "WIS", equipTmpl.getBaseWis());

    Equipment equipment =
        Equipment.create(
            userId,
            equipmentTemplateId,
            name,
            equipTmpl.getSlot(),
            rarity,
            equipTmpl.getWeaponType(),
            qm,
            affixes,
            statBonus,
            equipTmpl.getBaseAttack(),
            equipTmpl.getBaseDefense());
    equipmentRepository.save(equipment);

    return new ForgingResultVO(
        true,
        "锻造成功！",
        equipment.getId(),
        equipment.getName(),
        rarity,
        qualityScore,
        usedMaterials,
        attributeTotals);
  }

  @SuppressWarnings("unused")
  private static int getMaterialValue(StackableItem item, String attribute) {
    return item.getMaterialValue(attribute);
  }
}
