package top.stillmisty.xiantao.service;

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

    tryFindBestMaterialCombination(
        requirements, materials, attributeTotals, usedMaterials, remainingQuantities);

    List<String> missingAttributes = collectMissingAttributes(requirements, attributeTotals);
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

    if (exceedsAttributeMax(requirements, attributeTotals)) {
      String overAttribute = findOverMaxAttribute(requirements, attributeTotals);
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

    tryFindBestMaterialCombination(
        requirements, materials, attributeTotals, usedMaterials, remainingQuantities);

    List<String> missingAttributes = collectMissingAttributes(requirements, attributeTotals);
    return new MaterialSelectionResult(
        missingAttributes.isEmpty(), usedMaterials, attributeTotals, missingAttributes);
  }

  private void tryFindBestMaterialCombination(
      Map<String, ElementRange> requirements,
      List<StackableItem> materials,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials,
      Map<StackableItem, Integer> remainingQuantities) {
    for (int pass = 0; pass < requirements.size(); pass++) {
      boolean anyProgress = false;
      for (var entry : requirements.entrySet()) {
        String attribute = entry.getKey();
        int min = entry.getValue().min();
        int currentTotal = attributeTotals.getOrDefault(attribute, 0);
        if (currentTotal >= min) continue;

        int bestGain = 0;
        StackableItem bestMaterial = null;
        int bestQty = 0;

        for (StackableItem mat : materials) {
          Integer remaining = remainingQuantities.get(mat);
          if (remaining == null || remaining <= 0) continue;
          int matValue = mat.getMaterialValue(attribute);
          if (matValue <= 0) continue;
          int needed = (int) Math.ceil((double) (min - currentTotal) / matValue);
          int toUse = Math.min(needed, remaining);
          int totalGain = computeTotalGain(mat, toUse, requirements, attributeTotals);
          if (totalGain > bestGain || (totalGain == bestGain && toUse < bestQty)) {
            bestGain = totalGain;
            bestMaterial = mat;
            bestQty = toUse;
          }
        }

        if (bestMaterial != null && bestQty > 0) {
          applyMaterialAttributes(bestMaterial, bestQty, attributeTotals);
          remainingQuantities.merge(bestMaterial, -bestQty, Integer::sum);
          usedMaterials.merge(bestMaterial.getName(), bestQty, Integer::sum);
          anyProgress = true;
        }
      }
      if (!anyProgress) break;
    }
  }

  private int computeTotalGain(
      StackableItem mat,
      int quantity,
      Map<String, ElementRange> requirements,
      Map<String, Integer> currentTotals) {
    int gain = 0;
    for (var entry : requirements.entrySet()) {
      String attribute = entry.getKey();
      int current = currentTotals.getOrDefault(attribute, 0);
      int min = entry.getValue().min();
      if (current >= min) continue;
      int contrib = mat.getMaterialValue(attribute) * quantity;
      if (contrib > 0) {
        gain += Math.min(contrib, min - current);
      }
    }
    return gain;
  }

  private void applyMaterialAttributes(
      StackableItem mat, int quantity, Map<String, Integer> attributeTotals) {
    for (String attr : FORGE_ATTRIBUTES) {
      int value = mat.getMaterialValue(attr);
      if (value > 0) {
        attributeTotals.merge(attr, value * quantity, Integer::sum);
      }
    }
  }

  private List<String> collectMissingAttributes(
      Map<String, ElementRange> requirements, Map<String, Integer> attributeTotals) {
    List<String> missing = new ArrayList<>();
    for (var entry : requirements.entrySet()) {
      if (attributeTotals.getOrDefault(entry.getKey(), 0) < entry.getValue().min()) {
        missing.add(entry.getKey());
      }
    }
    return missing;
  }

  private boolean exceedsAttributeMax(
      Map<String, ElementRange> requirements, Map<String, Integer> attributeTotals) {
    for (var entry : requirements.entrySet()) {
      String attr = entry.getKey();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      if (attributeTotals.getOrDefault(attr, 0) > max) {
        return true;
      }
    }
    return false;
  }

  private String findOverMaxAttribute(
      Map<String, ElementRange> requirements, Map<String, Integer> attributeTotals) {
    for (var entry : requirements.entrySet()) {
      String attr = entry.getKey();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      if (attributeTotals.getOrDefault(attr, 0) > max) {
        return attr;
      }
    }
    return "";
  }

  public double calculateQualityScore(
      Map<String, Integer> attributeTotals, Map<String, ElementRange> requirements) {
    double totalScore = 0;
    int count = 0;
    for (var entry : requirements.entrySet()) {
      String attr = entry.getKey();
      int min = entry.getValue().min();
      int max = entry.getValue().max();
      int current = attributeTotals.getOrDefault(attr, 0);

      double center = (max + min) / 2.0;
      double halfWidth = (max - min) / 2.0;
      if (halfWidth == 0) {
        totalScore += 1.0;
      } else {
        double deviation = Math.abs(current - center);
        totalScore += Math.max(0, 1 - deviation / halfWidth);
      }
      count++;
    }
    return count > 0 ? totalScore / count : 0;
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

    double qualityScore = calculateQualityScore(attributeTotals, blueprint.requirements());
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
}
