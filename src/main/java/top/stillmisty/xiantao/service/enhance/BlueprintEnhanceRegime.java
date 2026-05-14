package top.stillmisty.xiantao.service.enhance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.forge.repository.PlayerForgingRecipeRepository;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.*;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ForgingCombinationFinder;
import top.stillmisty.xiantao.service.FudiHelper;

/** 强化图纸期 +10→上限：需要蓝图，品质分 = 成功率 */
@Component
@RequiredArgsConstructor
public class BlueprintEnhanceRegime {

  private final EnhancementCore core;
  private final FudiHelper fudiHelper;
  private final ForgingCombinationFinder combinationFinder;
  private final StackableItemRepository stackableItemRepository;
  private final PlayerForgingRecipeRepository playerForgingRecipeRepository;
  private final ItemTemplateRepository itemTemplateRepository;

  public boolean canHandle(int targetLevel) {
    return targetLevel >= 10;
  }

  public EnhanceResultVO executeAuto(
      Long userId, Equipment equipment, int currentLevel, int targetLevel, int stoneCost) {
    var blueprint = loadBlueprint(userId, equipment);
    var amplifiedConstraints = core.getAmplifiedConstraints(blueprint.requirements(), targetLevel);

    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();

    var selection = combinationFinder.findBestMaterials(amplifiedConstraints, materials);
    if (!selection.success()) {
      throw new BusinessException(ErrorCode.ENHANCE_MATERIAL_NOT_MATCH);
    }

    fudiHelper.deductSpiritStones(userId, stoneCost);
    core.consumeMaterials(userId, selection.usedMaterials(), materials);

    double successRate =
        combinationFinder.calculateQualityScore(selection.attributeTotals(), amplifiedConstraints);

    return rollAndApply(
        userId,
        equipment,
        currentLevel,
        targetLevel,
        stoneCost,
        successRate,
        selection.usedMaterials());
  }

  public EnhanceResultVO executeManual(
      Long userId,
      Equipment equipment,
      int currentLevel,
      int targetLevel,
      int stoneCost,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials) {
    var blueprint = loadBlueprint(userId, equipment);
    Map<String, ElementRange> constraints =
        core.getAmplifiedConstraints(blueprint.requirements(), targetLevel);

    if (!core.matchesConstraints(attributeTotals, constraints)) {
      throw new BusinessException(ErrorCode.ENHANCE_MATERIAL_NOT_MATCH);
    }

    fudiHelper.deductSpiritStones(userId, stoneCost);
    core.consumeMaterialsByName(userId, usedMaterials);

    double successRate = combinationFinder.calculateQualityScore(attributeTotals, constraints);

    return rollAndApply(
        userId, equipment, currentLevel, targetLevel, stoneCost, successRate, usedMaterials);
  }

  private ItemProperties.ForgingBlueprint loadBlueprint(Long userId, Equipment equipment) {
    var recipe =
        playerForgingRecipeRepository.findByUserIdAndEquipmentTemplateId(
            userId, equipment.getTemplateId());
    if (recipe.isEmpty()) {
      throw new BusinessException(ErrorCode.EQUIPMENT_BLUEPRINT_REQUIRED);
    }

    ItemTemplate blueprintTemplate =
        itemTemplateRepository.findById(recipe.get().getBlueprintTemplateId()).orElse(null);
    if (blueprintTemplate == null) {
      throw new BusinessException(ErrorCode.BLUEPRINT_DATA_ERROR);
    }

    var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
    if (blueprint == null) {
      throw new BusinessException(ErrorCode.BLUEPRINT_DATA_ERROR);
    }

    return blueprint;
  }

  private EnhanceResultVO rollAndApply(
      Long userId,
      Equipment equipment,
      int currentLevel,
      int targetLevel,
      int stoneCost,
      double successRate,
      Map<String, Integer> usedMaterials) {
    if (ThreadLocalRandom.current().nextDouble() < successRate) {
      return core.applyEnhanceSuccess(equipment, targetLevel, stoneCost, usedMaterials, userId);
    } else {
      return core.applyEnhanceFailure(
          equipment, currentLevel, targetLevel, stoneCost, successRate, usedMaterials);
    }
  }
}
