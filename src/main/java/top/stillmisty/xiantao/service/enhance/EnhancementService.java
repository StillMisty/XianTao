package top.stillmisty.xiantao.service.enhance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.MaterialAttribute;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.util.MaterialParser;
import top.stillmisty.xiantao.util.MaterialParser.ParsedMaterial;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancementService {

  private final EnhancementCore core;
  private final SafeEnhanceRegime safeRegime;
  private final ProbabilisticEnhanceRegime probabilisticRegime;
  private final BlueprintEnhanceRegime blueprintRegime;

  // ===================== 公开 API =====================

  @Transactional
  public ServiceResult<EnhanceResultVO> enhanceAuto(Long userId, String equipmentInput) {
    return new ServiceResult.Success<>(enhanceAutoInternal(userId, equipmentInput));
  }

  @Transactional
  public ServiceResult<EnhanceResultVO> enhanceManual(
      Long userId, String equipmentInput, List<String> materialInputs) {
    return new ServiceResult.Success<>(
        enhanceManualInternal(userId, equipmentInput, materialInputs));
  }

  // ===================== 内部 API =====================

  public EnhanceResultVO enhanceAutoInternal(Long userId, String equipmentInput) {
    var resolved = resolveAndValidate(userId, equipmentInput);
    int currentLevel = resolved.currentLevel();
    int targetLevel = currentLevel + 1;

    if (safeRegime.canHandle(targetLevel)) {
      return safeRegime.executeAuto(
          userId, resolved.equipment(), currentLevel, targetLevel, resolved.stoneCost());
    }
    if (probabilisticRegime.canHandle(targetLevel)) {
      return probabilisticRegime.executeAuto(
          userId, resolved.equipment(), currentLevel, targetLevel, resolved.stoneCost());
    }
    return blueprintRegime.executeAuto(
        userId, resolved.equipment(), currentLevel, targetLevel, resolved.stoneCost());
  }

  private static final int MAX_MATERIAL_TYPES = 3;

  @Transactional
  public EnhanceResultVO enhanceManualInternal(
      Long userId, String equipmentInput, List<String> materialInputs) {
    if (materialInputs.size() > MAX_MATERIAL_TYPES) {
      throw new BusinessException(ErrorCode.FORGING_MATERIAL_TOO_MANY);
    }
    var resolved = resolveAndValidate(userId, equipmentInput);
    int currentLevel = resolved.currentLevel();
    int targetLevel = currentLevel + 1;

    Map<String, Integer> attributeTotals = new HashMap<>();
    Map<String, Integer> usedMaterials = new HashMap<>();
    parseManualMaterials(userId, materialInputs, attributeTotals, usedMaterials);

    if (targetLevel <= 9) {
      return probabilisticRegime.executeManual(
          userId,
          resolved.equipment(),
          currentLevel,
          targetLevel,
          resolved.stoneCost(),
          attributeTotals,
          usedMaterials);
    }
    return blueprintRegime.executeManual(
        userId,
        resolved.equipment(),
        currentLevel,
        targetLevel,
        resolved.stoneCost(),
        attributeTotals,
        usedMaterials);
  }

  // ===================== 辅助方法 =====================

  private record ResolvedEnhance(Equipment equipment, int currentLevel, int stoneCost) {}

  private ResolvedEnhance resolveAndValidate(Long userId, String equipmentInput) {
    Equipment equipment = core.resolveEquipment(userId, equipmentInput);
    if (equipment == null) {
      throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND);
    }

    int currentLevel = equipment.getForgeLevel();
    int targetLevel = currentLevel + 1;
    int maxLevel = core.getMaxForgeLevel(equipment.getRarity());

    if (currentLevel >= maxLevel) {
      throw new BusinessException(ErrorCode.EQUIPMENT_FORGE_LEVEL_MAX, maxLevel);
    }

    int stoneCost = core.calculateSpiritStoneCost(equipment.getRarity(), targetLevel);
    return new ResolvedEnhance(equipment, currentLevel, stoneCost);
  }

  private void parseManualMaterials(
      Long userId,
      List<String> materialInputs,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials) {
    for (String input : materialInputs) {
      ParsedMaterial parsed = MaterialParser.parse(input);
      if (parsed == null) continue;
      String materialName = parsed.name();
      int quantity = parsed.quantity();

      List<top.stillmisty.xiantao.domain.item.entity.StackableItem> mats =
          core.resolveManualMaterial(userId, materialName);
      if (mats.isEmpty() || !mats.getFirst().hasEnoughQuantity(quantity)) {
        throw new BusinessException(ErrorCode.FORGING_MATERIAL_NOT_ENOUGH, materialName);
      }
      top.stillmisty.xiantao.domain.item.entity.StackableItem mat = mats.getFirst();
      for (var attr : MaterialAttribute.values()) {
        attributeTotals.merge(attr.getCode(), mat.getMaterialValue(attr) * quantity, Integer::sum);
      }
      usedMaterials.merge(mat.getName(), quantity, Integer::sum);
    }
  }
}
