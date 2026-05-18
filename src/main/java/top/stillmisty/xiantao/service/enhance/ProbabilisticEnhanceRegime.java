package top.stillmisty.xiantao.service.enhance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.forging.ForgingCombinationFinder;

/** 强化概率期 +4→+9：固定成功率 × 稀有度修正，需要锻材 */
@Component
@RequiredArgsConstructor
public class ProbabilisticEnhanceRegime {

  private final EnhancementCore core;
  private final SpiritStoneService spiritStoneService;
  private final ForgingCombinationFinder combinationFinder;
  private final StackableItemRepository stackableItemRepository;

  public boolean canHandle(int targetLevel) {
    return targetLevel >= 4 && targetLevel <= 9;
  }

  public EnhanceResultVO executeAuto(
      Long userId, Equipment equipment, int currentLevel, int targetLevel, int stoneCost) {
    Map<String, ElementRange> constraints =
        core.getGenericEnhanceConstraints(equipment.getRarity());
    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();

    var selection = combinationFinder.findBestMaterials(constraints, materials);
    if (!selection.success()) {
      throw new BusinessException(
          ErrorCode.FORGING_ATTRIBUTE_MISSING, String.join(", ", selection.missingAttributes()));
    }

    spiritStoneService.withdraw(userId, stoneCost);
    core.consumeMaterials(userId, selection.usedMaterials(), materials);

    return rollAndApply(
        userId, equipment, currentLevel, targetLevel, stoneCost, selection.usedMaterials());
  }

  public EnhanceResultVO executeManual(
      Long userId,
      Equipment equipment,
      int currentLevel,
      int targetLevel,
      int stoneCost,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials) {
    Map<String, ElementRange> constraints =
        core.getGenericEnhanceConstraints(equipment.getRarity());
    if (core.violatesConstraints(attributeTotals, constraints)) {
      throw new BusinessException(ErrorCode.ENHANCE_MATERIAL_NOT_MATCH);
    }

    spiritStoneService.withdraw(userId, stoneCost);
    core.consumeMaterialsByName(userId, usedMaterials);

    return rollAndApply(userId, equipment, currentLevel, targetLevel, stoneCost, usedMaterials);
  }

  private EnhanceResultVO rollAndApply(
      Long userId,
      Equipment equipment,
      int currentLevel,
      int targetLevel,
      int stoneCost,
      Map<String, Integer> usedMaterials) {
    double baseRate = core.getBaseSuccessRate(targetLevel);
    double rarityMod = core.getRarityModifier(equipment.getRarity());
    double successRate = baseRate * rarityMod;

    if (ThreadLocalRandom.current().nextDouble() < successRate) {
      return core.applyEnhanceSuccess(equipment, targetLevel, stoneCost, usedMaterials, userId);
    } else {
      return core.applyEnhanceFailure(
          equipment, currentLevel, targetLevel, stoneCost, successRate, usedMaterials);
    }
  }
}
