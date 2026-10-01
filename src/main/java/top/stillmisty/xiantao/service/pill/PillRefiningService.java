package top.stillmisty.xiantao.service.pill;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.util.MaterialParser;
import top.stillmisty.xiantao.util.MaterialParser.ParsedMaterial;

/** 炼丹服务 处理：自动/手动炼丹、成色计算 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PillRefiningService {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final PlayerPillRecipeRepository playerPillRecipeRepository;
  private final StackableItemService stackableItemService;
  private final PillCombinationFinder combinationFinder;

  // ===================== 公开 API =====================

  @Transactional
  public ServiceResult<PillRefiningResultVO> refinePillAuto(Long userId, String recipeName) {
    return new ServiceResult.Success<>(refinePillAutoInternal(userId, recipeName));
  }

  @Transactional
  public ServiceResult<PillRefiningResultVO> refinePillManual(
      Long userId, List<String> herbInputs) {
    return new ServiceResult.Success<>(refinePillManualInternal(userId, herbInputs));
  }

  // ===================== 内部 API =====================

  @Transactional
  public PillRefiningResultVO refinePillAutoInternal(Long userId, String recipeName) {
    List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
    Map<Long, ItemTemplate> templateMap = loadRecipeTemplates(recipes);
    PlayerPillRecipe targetRecipe = null;
    ItemTemplate recipeTemplate = null;
    for (PlayerPillRecipe recipe : recipes) {
      ItemTemplate template = templateMap.get(recipe.getRecipeTemplateId());
      if (template != null && template.getName().contains(recipeName)) {
        targetRecipe = recipe;
        recipeTemplate = template;
        break;
      }
    }

    if (targetRecipe == null) {
      String scrollName = findScrollNameInBag(userId, recipeName);
      if (scrollName != null) {
        throw new BusinessException(ErrorCode.RECIPE_SCROLL_NOT_LEARNED, recipeName, scrollName);
      }
      throw new BusinessException(ErrorCode.RECIPE_NOT_FOUND, recipeName);
    }
    if (recipeTemplate == null) {
      throw new BusinessException(ErrorCode.RECIPE_PILL_DATA_ERROR);
    }

    var recipeScroll = combinationFinder.getRecipeScroll(recipeTemplate);
    if (recipeScroll == null || recipeScroll.requirements().isEmpty()) {
      throw new BusinessException(ErrorCode.RECIPE_PILL_DATA_ERROR);
    }
    var requirements = recipeScroll.requirements();

    List<StackableItem> herbs =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.HERB)
            .toList();

    if (herbs.isEmpty()) {
      throw new BusinessException(ErrorCode.HERBS_EMPTY);
    }

    return combinationFinder.findBestCombination(userId, herbs, requirements, recipeTemplate);
  }

  // ===================== 辅助方法 =====================

  private static final int MAX_HERB_TYPES = 5;

  @Transactional
  public PillRefiningResultVO refinePillManualInternal(Long userId, List<String> herbInputs) {
    if (herbInputs.size() > MAX_HERB_TYPES) {
      throw new BusinessException(ErrorCode.PILL_MATERIAL_TOO_MANY);
    }
    List<HerbInput> parsedInputs = parseHerbInputs(userId, herbInputs);
    if (parsedInputs.isEmpty()) {
      throw new BusinessException(ErrorCode.PILL_MATERIAL_INPUT_FORMAT);
    }

    Map<String, Integer> elementTotals = new HashMap<>();
    Map<String, Integer> usedHerbs = new HashMap<>();
    for (HerbInput input : parsedInputs) {
      StackableItem herb = input.herb();
      int quantity = input.quantity();
      for (var element : ElementType.values()) {
        int value = herb.getElementValue(element) * quantity;
        elementTotals.merge(element.getCode(), value, Integer::sum);
      }
      usedHerbs.put(herb.getName(), quantity);
    }

    List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
    Map<Long, ItemTemplate> templateMap = loadRecipeTemplates(recipes);
    for (PlayerPillRecipe recipe : recipes) {
      ItemTemplate recipeTemplate = templateMap.get(recipe.getRecipeTemplateId());
      if (recipeTemplate == null) continue;

      var recipeScroll = combinationFinder.getRecipeScroll(recipeTemplate);
      if (recipeScroll == null) continue;
      var requirements = recipeScroll.requirements();
      if (combinationFinder.matchesRequirements(elementTotals, requirements)) {
        double qualityScore = combinationFinder.calculateQualityScore(elementTotals, requirements);
        PillQuality quality = combinationFinder.determineQuality(qualityScore);

        long resultItemId = recipeScroll.resultItemId();
        int resultQuantity = recipeScroll.resultQuantity();
        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) throw new BusinessException(ErrorCode.RECIPE_PILL_DATA_ERROR);

        for (HerbInput input : parsedInputs) {
          stackableItemService.reduceStackableItem(userId, input.herb().getId(), input.quantity());
        }

        combinationFinder.createPillItem(
            userId, resultTemplate, recipeScroll.grade(), quality, resultQuantity);

        return new PillRefiningResultVO(
            "炼丹成功！",
            resultItemId,
            resultTemplate.getName(),
            resultQuantity,
            quality.getCode(),
            usedHerbs,
            null);
      }
    }

    throw new BusinessException(ErrorCode.PILL_NO_MATCHING_RECIPE);
  }

  private List<HerbInput> parseHerbInputs(Long userId, List<String> herbInputs) {
    List<HerbInput> result = new ArrayList<>();
    for (String input : herbInputs) {
      ParsedMaterial parsed = MaterialParser.parse(input);
      if (parsed == null) continue;

      String herbName = parsed.name();
      int quantity = parsed.quantity();

      List<StackableItem> herbs =
          stackableItemRepository.findByUserId(userId).stream()
              .filter(
                  item -> item.getItemType() == ItemType.HERB && item.getName().contains(herbName))
              .toList();

      if (!herbs.isEmpty()) {
        result.add(new HerbInput(herbs.getFirst(), quantity));
      }
    }
    return result;
  }

  private record HerbInput(StackableItem herb, int quantity) {}

  /** 背包中是否有与输入同名的丹方卷轴（用于提示先「使用」学习）。 */
  @Nullable
  private String findScrollNameInBag(Long userId, String input) {
    for (StackableItem item :
        stackableItemRepository.findByUserIdAndType(userId, ItemType.RECIPE_SCROLL)) {
      ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
      if (template != null && template.getName().contains(input)) {
        return template.getName();
      }
    }
    return null;
  }

  /** 批量加载丹方模板，避免循环内逐条查询 */
  private Map<Long, ItemTemplate> loadRecipeTemplates(List<PlayerPillRecipe> recipes) {
    List<Long> ids =
        recipes.stream().map(PlayerPillRecipe::getRecipeTemplateId).distinct().toList();
    if (ids.isEmpty()) return Map.of();
    return itemTemplateRepository.findByIds(ids).stream()
        .collect(java.util.stream.Collectors.toMap(ItemTemplate::getId, t -> t));
  }
}
