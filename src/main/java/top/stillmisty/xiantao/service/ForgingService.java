package top.stillmisty.xiantao.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.forge.entity.PlayerForgingRecipe;
import top.stillmisty.xiantao.domain.forge.repository.PlayerForgingRecipeRepository;
import top.stillmisty.xiantao.domain.forge.vo.ForgingRecipeVO;
import top.stillmisty.xiantao.domain.forge.vo.ForgingResultVO;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 锻造服务 处理：自动/手动锻造、图纸学习、已学列表 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgingService {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final PlayerForgingRecipeRepository playerForgingRecipeRepository;
  private final ForgingCombinationFinder combinationFinder;
  private final StackableItemService stackableItemService;
  private final top.stillmisty.xiantao.domain.item.repository.EquipmentRepository
      equipmentRepository;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<ForgingResultVO> forgeAuto(
      PlatformType platform, String openId, String blueprintName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(forgeAuto(userId, blueprintName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ForgingResultVO> forgeManual(
      PlatformType platform, String openId, List<String> materialInputs) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(forgeManual(userId, materialInputs));
  }

  @Authenticated
  public ServiceResult<List<ForgingRecipeVO>> getForgingRecipes(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getForgingRecipes(userId));
  }

  @Authenticated
  public ServiceResult<ForgingRecipeVO> getForgingRecipeDetail(
      PlatformType platform, String openId, String recipeName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getForgingRecipeDetail(userId, recipeName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ForgingRecipeVO> learnRecipe(
      PlatformType platform, String openId, String recipeName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(learnRecipe(userId, recipeName));
  }

  // ===================== 内部 API =====================

  @Transactional
  public ForgingResultVO forgeAuto(Long userId, String blueprintName) {
    List<PlayerForgingRecipe> recipes = playerForgingRecipeRepository.findByUserId(userId);
    PlayerForgingRecipe targetRecipe = null;
    ItemTemplate blueprintTemplate = null;
    for (PlayerForgingRecipe recipe : recipes) {
      ItemTemplate template =
          itemTemplateRepository.findById(recipe.getBlueprintTemplateId()).orElse(null);
      if (template != null && template.getName().contains(blueprintName)) {
        targetRecipe = recipe;
        blueprintTemplate = template;
        break;
      }
    }

    if (targetRecipe == null) {
      return new ForgingResultVO(
          false, "未找到锻造图纸：" + blueprintName, null, null, null, 0.0, null, null);
    }

    var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
    if (blueprint == null || blueprint.requirements().isEmpty()) {
      return new ForgingResultVO(false, "锻造图纸数据错误", null, null, null, 0.0, null, null);
    }

    List<StackableItem> materials =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.MATERIAL)
            .toList();

    if (materials.isEmpty()) {
      return new ForgingResultVO(false, "背包中没有锻材", null, null, null, 0.0, null, null);
    }

    return combinationFinder.forgeEquipment(
        userId,
        materials,
        blueprint.requirements(),
        blueprintTemplate,
        targetRecipe.getEquipmentTemplateId());
  }

  @Transactional
  public ForgingResultVO forgeManual(Long userId, List<String> materialInputs) {
    List<MaterialInput> parsedInputs = parseMaterialInputs(userId, materialInputs);
    if (parsedInputs.isEmpty()) {
      return new ForgingResultVO(false, "锻材输入格式错误", null, null, null, 0.0, null, null);
    }

    Map<String, Integer> attributeTotals = new HashMap<>();
    Map<String, Integer> usedMaterials = new HashMap<>();
    for (MaterialInput input : parsedInputs) {
      StackableItem mat = input.material();
      int quantity = input.quantity();
      if (!mat.hasEnoughQuantity(quantity)) {
        return new ForgingResultVO(
            false, "锻材数量不足：" + mat.getName(), null, null, null, 0.0, null, null);
      }
      for (String attr : List.of("RIGIDITY", "TOUGHNESS", "SPIRIT")) {
        int value = mat.getMaterialValue(attr) * quantity;
        attributeTotals.merge(attr, value, Integer::sum);
      }
      usedMaterials.put(mat.getName(), quantity);
    }

    List<PlayerForgingRecipe> recipes = playerForgingRecipeRepository.findByUserId(userId);
    for (PlayerForgingRecipe recipe : recipes) {
      ItemTemplate blueprintTemplate =
          itemTemplateRepository.findById(recipe.getBlueprintTemplateId()).orElse(null);
      if (blueprintTemplate == null) continue;

      var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
      if (blueprint == null) continue;

      if (matchesRequirements(attributeTotals, blueprint.requirements())) {
        double qualityScore =
            combinationFinder.calculateQualityScore(attributeTotals, blueprint.requirements());
        var rarity = combinationFinder.rollRarityByQualityScore(qualityScore, blueprint.grade());

        for (MaterialInput input : parsedInputs) {
          stackableItemService.reduceStackableItem(
              userId, input.material().getId(), input.quantity());
        }

        var equipTmpl =
            equipmentTemplateRepository.findById(recipe.getEquipmentTemplateId()).orElse(null);
        if (equipTmpl == null) continue;

        double qm = rarity.randomQualityMultiplier();
        int affixCount = rarity.randomAffixCount();
        Map<String, Integer> affixes = new LinkedHashMap<>();
        List<top.stillmisty.xiantao.domain.item.enums.AffixType> pool =
            new ArrayList<>(
                List.of(top.stillmisty.xiantao.domain.item.enums.AffixType.getAttributeAffixes()));
        if (rarity == top.stillmisty.xiantao.domain.item.enums.Rarity.LEGENDARY) {
          pool.addAll(
              List.of(top.stillmisty.xiantao.domain.item.enums.AffixType.getSpecialAffixes()));
        }
        Collections.shuffle(pool, java.util.concurrent.ThreadLocalRandom.current());
        for (int i = 0; i < affixCount && i < pool.size(); i++) {
          var at = pool.get(i);
          int value =
              at.isSpecial()
                  ? 5
                  : (1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(4));
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

        var equipment =
            top.stillmisty.xiantao.domain.item.entity.Equipment.create(
                userId,
                recipe.getEquipmentTemplateId(),
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

    return new ForgingResultVO(
        false, "锻材三性不匹配任何锻造图纸", null, null, null, 0.0, usedMaterials, attributeTotals);
  }

  public List<ForgingRecipeVO> getForgingRecipes(Long userId) {
    List<PlayerForgingRecipe> recipes = playerForgingRecipeRepository.findByUserId(userId);
    return recipes.stream()
        .map(
            recipe -> {
              ItemTemplate blueprintTemplate =
                  itemTemplateRepository.findById(recipe.getBlueprintTemplateId()).orElse(null);
              var equipTmpl =
                  equipmentTemplateRepository
                      .findById(recipe.getEquipmentTemplateId())
                      .orElse(null);
              if (blueprintTemplate == null || equipTmpl == null) return null;
              return convertToForgingRecipeVO(recipe, blueprintTemplate, equipTmpl);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  public ForgingRecipeVO getForgingRecipeDetail(Long userId, String recipeName) {
    List<PlayerForgingRecipe> recipes = playerForgingRecipeRepository.findByUserId(userId);
    for (PlayerForgingRecipe recipe : recipes) {
      ItemTemplate blueprintTemplate =
          itemTemplateRepository.findById(recipe.getBlueprintTemplateId()).orElse(null);
      if (blueprintTemplate != null && blueprintTemplate.getName().contains(recipeName)) {
        var equipTmpl =
            equipmentTemplateRepository.findById(recipe.getEquipmentTemplateId()).orElse(null);
        if (equipTmpl != null) {
          return convertToForgingRecipeVO(recipe, blueprintTemplate, equipTmpl);
        }
      }
    }
    return null;
  }

  @Transactional
  public ForgingRecipeVO learnRecipe(Long userId, String recipeName) {
    List<StackableItem> items = stackableItemRepository.findByUserId(userId);
    StackableItem recipeItem = null;
    for (StackableItem item : items) {
      if (item.getItemType() == ItemType.FORGING_BLUEPRINT && item.getName().contains(recipeName)) {
        recipeItem = item;
        break;
      }
    }

    if (recipeItem == null)
      throw new BusinessException(ErrorCode.BLUEPRINT_SCROLL_NOT_FOUND, recipeName);

    ItemTemplate blueprintTemplate =
        itemTemplateRepository.findById(recipeItem.getTemplateId()).orElse(null);
    if (blueprintTemplate == null)
      throw new BusinessException(ErrorCode.BLUEPRINT_SCROLL_WRONG_TYPE);

    if (playerForgingRecipeRepository.existsByUserIdAndBlueprintTemplateId(
        userId, blueprintTemplate.getId())) {
      throw new BusinessException(ErrorCode.BLUEPRINT_ALREADY_LEARNED);
    }

    var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
    if (blueprint == null) throw new BusinessException(ErrorCode.BLUEPRINT_SCROLL_WRONG_TYPE);
    long equipmentTemplateId = blueprint.equipmentTemplateId();

    PlayerForgingRecipe recipe =
        PlayerForgingRecipe.create(userId, blueprintTemplate.getId(), equipmentTemplateId);
    playerForgingRecipeRepository.save(recipe);

    var equipTmpl = equipmentTemplateRepository.findById(equipmentTemplateId).orElse(null);
    if (equipTmpl == null) throw new BusinessException(ErrorCode.BLUEPRINT_DATA_ERROR);

    return convertToForgingRecipeVO(recipe, blueprintTemplate, equipTmpl);
  }

  // ===================== 辅助方法 =====================

  private boolean matchesRequirements(
      Map<String, Integer> attributeTotals, Map<String, ElementRange> requirements) {
    for (var entry : requirements.entrySet()) {
      String attr = entry.getKey();
      int min = entry.getValue().min();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      int current = attributeTotals.getOrDefault(attr, 0);
      if (current < min || current > max) return false;
    }
    return true;
  }

  private ForgingRecipeVO convertToForgingRecipeVO(
      PlayerForgingRecipe recipe,
      ItemTemplate blueprintTemplate,
      top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate equipTmpl) {
    var blueprint = combinationFinder.getForgingBlueprint(blueprintTemplate);
    if (blueprint == null) {
      return new ForgingRecipeVO(
          recipe.getBlueprintTemplateId(),
          blueprintTemplate.getName(),
          0,
          recipe.getEquipmentTemplateId(),
          equipTmpl.getName(),
          Map.of());
    }
    return new ForgingRecipeVO(
        recipe.getBlueprintTemplateId(),
        blueprintTemplate.getName(),
        blueprint.grade(),
        recipe.getEquipmentTemplateId(),
        equipTmpl.getName(),
        blueprint.requirements());
  }

  private record MaterialInput(StackableItem material, int quantity) {}

  private List<MaterialInput> parseMaterialInputs(Long userId, List<String> materialInputs) {
    List<MaterialInput> result = new ArrayList<>();
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

      List<StackableItem> materials =
          stackableItemRepository.findByUserId(userId).stream()
              .filter(
                  item ->
                      item.getItemType() == ItemType.MATERIAL
                          && item.getName().contains(materialName))
              .toList();

      if (!materials.isEmpty()) {
        result.add(new MaterialInput(materials.getFirst(), quantity));
      }
    }
    return result;
  }
}
