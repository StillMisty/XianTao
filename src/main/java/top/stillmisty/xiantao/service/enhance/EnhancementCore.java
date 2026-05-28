package top.stillmisty.xiantao.service.enhance;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.*;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.MaterialAttribute;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.inventory.ItemResolver;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
@RequiredArgsConstructor
public class EnhancementCore {

  private final EquipmentRepository equipmentRepository;
  private final StackableItemRepository stackableItemRepository;
  private final ItemResolver itemResolver;
  private final StackableItemService stackableItemService;

  static final int BASE_STONE_COST = 50;
  public static final List<String> FORGE_ATTRIBUTES =
      Arrays.stream(MaterialAttribute.values()).map(MaterialAttribute::getCode).toList();

  public Equipment resolveEquipment(Long userId, String input) {
    var result = itemResolver.resolveEquipment(userId, input);
    if (result instanceof ItemResolver.Found<Equipment> f) return f.item();
    return null;
  }

  public int getMaxForgeLevel(Rarity rarity) {
    return switch (rarity) {
      case BROKEN -> 3;
      case COMMON -> 5;
      case RARE -> 8;
      case EPIC -> 12;
      case LEGENDARY -> 18;
    };
  }

  double getBaseSuccessRate(int targetLevel) {
    return switch (targetLevel) {
      case 4 -> 0.80;
      case 5 -> 0.75;
      case 6 -> 0.70;
      case 7 -> 0.65;
      case 8 -> 0.60;
      case 9 -> 0.55;
      default -> 0.50;
    };
  }

  double getRarityModifier(Rarity rarity) {
    return switch (rarity) {
      case LEGENDARY -> 0.9;
      case EPIC -> 0.95;
      case RARE -> 1.0;
      case COMMON -> 1.05;
      case BROKEN -> 1.1;
    };
  }

  public int calculateSpiritStoneCost(Rarity rarity, int forgeLevel) {
    double rarityCoefficient =
        switch (rarity) {
          case BROKEN -> 0.5;
          case COMMON -> 1.0;
          case RARE -> 2.0;
          case EPIC -> 4.0;
          case LEGENDARY -> 8.0;
        };
    return (int) (BASE_STONE_COST * rarityCoefficient * (1 + forgeLevel * 0.3));
  }

  Map<String, ElementRange> getGenericEnhanceConstraints(Rarity rarity) {
    return switch (rarity) {
      case BROKEN ->
          Map.of(
              "RIGIDITY", new ElementRange(3, 8),
              "TOUGHNESS", new ElementRange(2, 5),
              "SPIRIT", new ElementRange(0, 3));
      case COMMON ->
          Map.of(
              "RIGIDITY", new ElementRange(5, 15),
              "TOUGHNESS", new ElementRange(3, 10),
              "SPIRIT", new ElementRange(1, 5));
      case RARE ->
          Map.of(
              "RIGIDITY", new ElementRange(10, 25),
              "TOUGHNESS", new ElementRange(5, 15),
              "SPIRIT", new ElementRange(3, 10));
      case EPIC ->
          Map.of(
              "RIGIDITY", new ElementRange(20, 40),
              "TOUGHNESS", new ElementRange(10, 25),
              "SPIRIT", new ElementRange(5, 15));
      case LEGENDARY ->
          Map.of(
              "RIGIDITY", new ElementRange(30, 60),
              "TOUGHNESS", new ElementRange(15, 40),
              "SPIRIT", new ElementRange(10, 25));
    };
  }

  Map<String, ElementRange> getAmplifiedConstraints(
      Map<String, ElementRange> blueprintReqs, int targetLevel) {
    double multiplier = 1 + (targetLevel - 9) * 0.3;
    Map<String, ElementRange> amplified = new HashMap<>();
    for (var entry : blueprintReqs.entrySet()) {
      ElementRange original = entry.getValue();
      amplified.put(
          entry.getKey(),
          new ElementRange(
              (int) Math.round(original.min() * multiplier),
              (int) Math.round(original.max() * multiplier)));
    }
    return amplified;
  }

  boolean violatesConstraints(
      Map<String, Integer> attributeTotals, Map<String, ElementRange> constraints) {
    for (var entry : constraints.entrySet()) {
      String attr = entry.getKey();
      int min = entry.getValue().min();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      int current = attributeTotals.getOrDefault(attr, 0);
      if (current < min || current > max) return true;
    }
    return false;
  }

  @Transactional
  EnhanceResultVO applyEnhanceSuccess(
      Equipment equipment,
      int targetLevel,
      int stoneCost,
      Map<String, Integer> usedMaterials,
      Long userId) {
    int previousLevel = equipment.getForgeLevel() != null ? equipment.getForgeLevel() : 0;
    equipment.setForgeLevel(targetLevel);

    String milestoneReward = applyMilestoneReward(equipment, targetLevel);
    equipmentRepository.save(equipment);

    return new EnhanceResultVO(
        true,
        "强化成功！" + equipment.getName() + " → +" + targetLevel,
        equipment.getId(),
        equipment.getName(),
        targetLevel,
        previousLevel,
        previousLevel < 3
            ? 1.0
            : (targetLevel <= 9
                ? getBaseSuccessRate(targetLevel) * getRarityModifier(equipment.getRarity())
                : 0),
        stoneCost,
        usedMaterials,
        milestoneReward);
  }

  @Transactional
  EnhanceResultVO applyEnhanceFailure(
      Equipment equipment,
      int currentLevel,
      int targetLevel,
      int stoneCost,
      double successRate,
      Map<String, Integer> usedMaterials) {
    int newLevel = currentLevel > 0 ? currentLevel - 1 : 0;
    equipment.setForgeLevel(newLevel);
    equipmentRepository.save(equipment);

    return new EnhanceResultVO(
        false,
        String.format(
            "强化失败！%s 降为 +%d（成功率 %.0f%%）", equipment.getName(), newLevel, successRate * 100),
        equipment.getId(),
        equipment.getName(),
        newLevel,
        currentLevel,
        successRate,
        stoneCost,
        usedMaterials,
        null);
  }

  void consumeMaterials(
      Long userId, Map<String, Integer> usedMaterials, List<StackableItem> materials) {
    for (Map.Entry<String, Integer> entry : usedMaterials.entrySet()) {
      for (StackableItem mat : materials) {
        if (mat.getName().equals(entry.getKey())) {
          stackableItemService.reduceStackableItem(userId, mat.getId(), entry.getValue());
          break;
        }
      }
    }
  }

  public void consumeMaterialsByName(Long userId, Map<String, Integer> usedMaterials) {
    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();
    consumeMaterials(userId, usedMaterials, materials);
  }

  public List<StackableItem> resolveManualMaterial(Long userId, String materialName) {
    return stackableItemRepository.findByUserId(userId).stream()
        .filter(
            item ->
                item.getItemType() == ItemType.MATERIAL && item.getName().contains(materialName))
        .toList();
  }

  private String applyMilestoneReward(Equipment equipment, int newForgeLevel) {
    if (newForgeLevel == 5) {
      var statAffixes =
          new ArrayList<>(
              equipment.getAffixes().entrySet().stream()
                  .filter(e -> !isSpecialAffix(e.getKey()))
                  .toList());
      if (!statAffixes.isEmpty()) {
        var entry = statAffixes.get(ThreadLocalRandom.current().nextInt(statAffixes.size()));
        equipment.getAffixes().merge(entry.getKey(), 1, Integer::sum);
        return "词缀 " + entry.getKey() + " +1";
      }
    } else if (newForgeLevel == 10) {
      var allAffixes = new ArrayList<>(equipment.getAffixes().entrySet());
      if (!allAffixes.isEmpty()) {
        var entry = allAffixes.get(ThreadLocalRandom.current().nextInt(allAffixes.size()));
        equipment.getAffixes().merge(entry.getKey(), 1, Integer::sum);
        return "词缀 " + entry.getKey() + " +1";
      }
    } else if (newForgeLevel == 15) {
      int maxAffixes = equipment.getRarity().getAffixCountMax();
      if (equipment.getAffixes().size() < maxAffixes) {
        List<AffixType> pool = new ArrayList<>(List.of(AffixType.getAttributeAffixes()));
        if (equipment.getRarity() == Rarity.LEGENDARY) {
          pool.addAll(List.of(AffixType.getSpecialAffixes()));
        }
        pool.removeIf(
            at -> {
              String key = at.getStatField() != null ? at.getStatField() : at.name();
              return equipment.getAffixes().containsKey(key);
            });
        if (!pool.isEmpty()) {
          Collections.shuffle(pool, ThreadLocalRandom.current());
          AffixType newAffix = pool.getFirst();
          int value = newAffix.isSpecial() ? 5 : (1 + ThreadLocalRandom.current().nextInt(4));
          String key = newAffix.getStatField() != null ? newAffix.getStatField() : newAffix.name();
          equipment.getAffixes().put(key, value);
          return "解锁新词缀 " + newAffix.getDisplayName() + " +" + value;
        }
      }
    }
    return null;
  }

  private boolean isSpecialAffix(String key) {
    try {
      AffixType at = AffixType.fromKey(key);
      return at.isSpecial();
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
