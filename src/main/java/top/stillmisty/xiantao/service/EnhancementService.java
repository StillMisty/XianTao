package top.stillmisty.xiantao.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.forge.repository.PlayerForgingRecipeRepository;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.*;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 强化服务 处理：自动/手动强化、成功率计算、约束放大、里程碑奖励 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancementService {

  private final EquipmentRepository equipmentRepository;
  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final PlayerForgingRecipeRepository playerForgingRecipeRepository;
  private final StackableItemService stackableItemService;
  private final ForgingCombinationFinder combinationFinder;
  private final FudiHelper fudiHelper;
  private final ItemResolver itemResolver;

  private static final int BASE_STONE_COST = 50;
  private static final List<String> FORGE_ATTRIBUTES = List.of("RIGIDITY", "TOUGHNESS", "SPIRIT");

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<EnhanceResultVO> enhanceAuto(
      PlatformType platform, String openId, String equipmentInput) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(enhanceAuto(userId, equipmentInput));
  }

  @Authenticated
  @Transactional
  public ServiceResult<EnhanceResultVO> enhanceManual(
      PlatformType platform, String openId, String equipmentInput, List<String> materialInputs) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(enhanceManual(userId, equipmentInput, materialInputs));
  }

  // ===================== 内部 API =====================

  @Transactional
  public EnhanceResultVO enhanceAuto(Long userId, String equipmentInput) {
    var resolved = resolveEquipment(userId, equipmentInput);
    if (resolved == null) {
      return new EnhanceResultVO(
          false, "未找到装备：" + equipmentInput, null, null, 0, 0, 0, 0, null, null);
    }

    int currentLevel = resolved.getForgeLevel() != null ? resolved.getForgeLevel() : 0;
    int targetLevel = currentLevel + 1;
    int maxLevel = getMaxForgeLevel(resolved.getRarity());

    if (currentLevel >= maxLevel) {
      return new EnhanceResultVO(
          false,
          "装备强化等级已达上限（" + maxLevel + "）",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          0,
          null,
          null);
    }

    int stoneCost = calculateSpiritStoneCost(resolved.getRarity(), targetLevel);
    fudiHelper.checkSpiritStones(userId, stoneCost);

    // +0 → +3: 100% success, stones only
    if (targetLevel <= 3) {
      fudiHelper.deductSpiritStones(userId, stoneCost);
      return applyEnhanceSuccess(resolved, targetLevel, stoneCost, null, userId);
    }

    // +4 → +9: fixed probability, needs materials
    if (targetLevel <= 9) {
      var constraints = getGenericEnhanceConstraints(resolved.getRarity());
      List<StackableItem> materials =
          stackableItemRepository.findByUserId(userId).stream()
              .filter(item -> item.getItemType() == ItemType.MATERIAL)
              .toList();

      var selection = combinationFinder.findBestMaterials(constraints, materials);
      if (!selection.success()) {
        return new EnhanceResultVO(
            false,
            "锻材不足，缺少：" + String.join(", ", selection.missingAttributes()),
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            null,
            null);
      }

      fudiHelper.deductSpiritStones(userId, stoneCost);
      consumeMaterials(userId, selection.usedMaterials(), materials);

      double baseRate = getBaseSuccessRate(targetLevel);
      double rarityMod = getRarityModifier(resolved.getRarity());
      double successRate = baseRate * rarityMod;

      if (ThreadLocalRandom.current().nextDouble() < successRate) {
        return applyEnhanceSuccess(
            resolved, targetLevel, stoneCost, selection.usedMaterials(), userId);
      } else {
        return applyEnhanceFailure(
            resolved, currentLevel, targetLevel, stoneCost, successRate, selection.usedMaterials());
      }
    }

    // +10 → cap: blueprint required, quality score = success rate
    var recipe =
        playerForgingRecipeRepository.findByUserIdAndEquipmentTemplateId(
            userId, resolved.getTemplateId());
    if (recipe.isEmpty()) {
      return new EnhanceResultVO(
          false,
          "掉落装备强化上限为+9，锻造装备+10及以上需持有对应图纸",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          stoneCost,
          null,
          null);
    }

    ItemTemplate blueprintTemplate =
        itemTemplateRepository.findById(recipe.get().getBlueprintTemplateId()).orElse(null);
    if (blueprintTemplate == null) {
      return new EnhanceResultVO(
          false,
          "锻造图纸数据异常",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          stoneCost,
          null,
          null);
    }

    var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
    if (blueprint == null) {
      return new EnhanceResultVO(
          false,
          "锻造图纸数据异常",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          stoneCost,
          null,
          null);
    }

    var amplifiedConstraints = getAmplifiedConstraints(blueprint.requirements(), targetLevel);

    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();

    var selection = combinationFinder.findBestMaterials(amplifiedConstraints, materials);
    if (!selection.success()) {
      return new EnhanceResultVO(
          false,
          "锻材不足，无法满足+" + targetLevel + "强化约束",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          stoneCost,
          null,
          null);
    }

    fudiHelper.deductSpiritStones(userId, stoneCost);
    consumeMaterials(userId, selection.usedMaterials(), materials);

    double successRate =
        combinationFinder.calculateQualityScore(selection.attributeTotals(), amplifiedConstraints);

    if (ThreadLocalRandom.current().nextDouble() < successRate) {
      return applyEnhanceSuccess(
          resolved, targetLevel, stoneCost, selection.usedMaterials(), userId);
    } else {
      return applyEnhanceFailure(
          resolved, currentLevel, targetLevel, stoneCost, successRate, selection.usedMaterials());
    }
  }

  @Transactional
  public EnhanceResultVO enhanceManual(
      Long userId, String equipmentInput, List<String> materialInputs) {
    var resolved = resolveEquipment(userId, equipmentInput);
    if (resolved == null) {
      return new EnhanceResultVO(
          false, "未找到装备：" + equipmentInput, null, null, 0, 0, 0, 0, null, null);
    }

    int currentLevel = resolved.getForgeLevel() != null ? resolved.getForgeLevel() : 0;
    int targetLevel = currentLevel + 1;
    int maxLevel = getMaxForgeLevel(resolved.getRarity());

    if (currentLevel >= maxLevel) {
      return new EnhanceResultVO(
          false,
          "装备强化等级已达上限（" + maxLevel + "）",
          resolved.getId(),
          resolved.getName(),
          currentLevel,
          currentLevel,
          0,
          0,
          null,
          null);
    }

    int stoneCost = calculateSpiritStoneCost(resolved.getRarity(), targetLevel);
    fudiHelper.checkSpiritStones(userId, stoneCost);

    // Parse material inputs
    Map<String, Integer> attributeTotals = new HashMap<>();
    Map<String, Integer> usedMaterials = new HashMap<>();
    for (String input : materialInputs) {
      String[] parts = input.split("[×xX]");
      if (parts.length != 2) continue;
      String materialName = parts[0].trim();
      int quantity;
      try {
        quantity = Integer.parseInt(parts[1].trim());
      } catch (NumberFormatException e) {
        continue;
      }
      if (quantity <= 0) continue;

      List<StackableItem> mats =
          stackableItemRepository.findByUserId(userId).stream()
              .filter(
                  item ->
                      item.getItemType() == ItemType.MATERIAL
                          && item.getName().contains(materialName))
              .toList();
      if (mats.isEmpty() || !mats.getFirst().hasEnoughQuantity(quantity)) {
        return new EnhanceResultVO(
            false,
            "锻材不足：" + materialName,
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            null,
            null);
      }
      StackableItem mat = mats.getFirst();
      for (String attr : FORGE_ATTRIBUTES) {
        attributeTotals.merge(attr, mat.getMaterialValue(attr) * quantity, Integer::sum);
      }
      usedMaterials.merge(mat.getName(), quantity, Integer::sum);
    }

    Map<String, ElementRange> constraints;
    double successRate;

    if (targetLevel <= 9) {
      constraints = getGenericEnhanceConstraints(resolved.getRarity());
      if (!matchesConstraints(attributeTotals, constraints)) {
        return new EnhanceResultVO(
            false,
            "锻材三性不满足+" + targetLevel + "强化约束",
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            usedMaterials,
            null);
      }
      double baseRate = getBaseSuccessRate(targetLevel);
      double rarityMod = getRarityModifier(resolved.getRarity());
      successRate = baseRate * rarityMod;
    } else {
      var recipe =
          playerForgingRecipeRepository.findByUserIdAndEquipmentTemplateId(
              userId, resolved.getTemplateId());
      if (recipe.isEmpty()) {
        return new EnhanceResultVO(
            false,
            "掉落装备强化上限为+9",
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            null,
            null);
      }

      ItemTemplate blueprintTemplate =
          itemTemplateRepository.findById(recipe.get().getBlueprintTemplateId()).orElse(null);
      if (blueprintTemplate == null) {
        return new EnhanceResultVO(
            false,
            "锻造图纸数据异常",
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            null,
            null);
      }

      var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
      if (blueprint == null) {
        return new EnhanceResultVO(
            false,
            "锻造图纸数据异常",
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            null,
            null);
      }

      constraints = getAmplifiedConstraints(blueprint.requirements(), targetLevel);
      if (!matchesConstraints(attributeTotals, constraints)) {
        return new EnhanceResultVO(
            false,
            "锻材三性不满足+" + targetLevel + "强化约束",
            resolved.getId(),
            resolved.getName(),
            currentLevel,
            currentLevel,
            0,
            stoneCost,
            usedMaterials,
            null);
      }
      successRate = combinationFinder.calculateQualityScore(attributeTotals, constraints);
    }

    fudiHelper.deductSpiritStones(userId, stoneCost);
    consumeMaterialsByName(userId, usedMaterials);

    if (ThreadLocalRandom.current().nextDouble() < successRate) {
      return applyEnhanceSuccess(resolved, targetLevel, stoneCost, usedMaterials, userId);
    } else {
      return applyEnhanceFailure(
          resolved, currentLevel, targetLevel, stoneCost, successRate, usedMaterials);
    }
  }

  // ===================== 辅助方法 =====================

  private Equipment resolveEquipment(Long userId, String input) {
    var result = itemResolver.resolveEquipment(userId, input);
    if (result instanceof ItemResolver.Found<Equipment> f) return f.item();
    return null;
  }

  private EnhanceResultVO applyEnhanceSuccess(
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

  private EnhanceResultVO applyEnhanceFailure(
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

  private int getMaxForgeLevel(Rarity rarity) {
    return switch (rarity) {
      case BROKEN -> 3;
      case COMMON -> 5;
      case RARE -> 8;
      case EPIC -> 12;
      case LEGENDARY -> 18;
    };
  }

  private double getBaseSuccessRate(int targetLevel) {
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

  private double getRarityModifier(Rarity rarity) {
    return switch (rarity) {
      case LEGENDARY -> 0.9;
      case EPIC -> 0.95;
      case RARE -> 1.0;
      case COMMON -> 1.05;
      case BROKEN -> 1.1;
    };
  }

  private int calculateSpiritStoneCost(Rarity rarity, int forgeLevel) {
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

  private Map<String, ElementRange> getGenericEnhanceConstraints(Rarity rarity) {
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

  private Map<String, ElementRange> getAmplifiedConstraints(
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

  private boolean matchesConstraints(
      Map<String, Integer> attributeTotals, Map<String, ElementRange> constraints) {
    for (var entry : constraints.entrySet()) {
      String attr = entry.getKey();
      int min = entry.getValue().min();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      int current = attributeTotals.getOrDefault(attr, 0);
      if (current < min || current > max) return false;
    }
    return true;
  }

  private void consumeMaterials(
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

  private void consumeMaterialsByName(Long userId, Map<String, Integer> usedMaterials) {
    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();
    consumeMaterials(userId, usedMaterials, materials);
  }
}
