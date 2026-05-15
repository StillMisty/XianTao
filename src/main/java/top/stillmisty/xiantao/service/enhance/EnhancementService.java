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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.fudi.FudiHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancementService {

  private final EnhancementCore core;
  private final SafeEnhanceRegime safeRegime;
  private final ProbabilisticEnhanceRegime probabilisticRegime;
  private final BlueprintEnhanceRegime blueprintRegime;
  private final FudiHelper fudiHelper;

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

  public EnhanceResultVO enhanceAuto(Long userId, String equipmentInput) {
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

  @Transactional
  public EnhanceResultVO enhanceManual(
      Long userId, String equipmentInput, List<String> materialInputs) {
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

    int currentLevel = equipment.getForgeLevel() != null ? equipment.getForgeLevel() : 0;
    int targetLevel = currentLevel + 1;
    int maxLevel = core.getMaxForgeLevel(equipment.getRarity());

    if (currentLevel >= maxLevel) {
      throw new BusinessException(ErrorCode.EQUIPMENT_FORGE_LEVEL_MAX, maxLevel);
    }

    int stoneCost = core.calculateSpiritStoneCost(equipment.getRarity(), targetLevel);
    fudiHelper.checkSpiritStones(userId, stoneCost);

    return new ResolvedEnhance(equipment, currentLevel, stoneCost);
  }

  private void parseManualMaterials(
      Long userId,
      List<String> materialInputs,
      Map<String, Integer> attributeTotals,
      Map<String, Integer> usedMaterials) {
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

      List<top.stillmisty.xiantao.domain.item.entity.StackableItem> mats =
          core.resolveManualMaterial(userId, materialName);
      if (mats.isEmpty() || !mats.getFirst().hasEnoughQuantity(quantity)) {
        throw new BusinessException(ErrorCode.FORGING_MATERIAL_NOT_ENOUGH, materialName);
      }
      top.stillmisty.xiantao.domain.item.entity.StackableItem mat = mats.getFirst();
      for (String attr : EnhancementCore.FORGE_ATTRIBUTES) {
        attributeTotals.merge(attr, mat.getMaterialValue(attr) * quantity, Integer::sum);
      }
      usedMaterials.merge(mat.getName(), quantity, Integer::sum);
    }
  }
}
