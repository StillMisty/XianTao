package top.stillmisty.xiantao.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

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

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<PillRefiningResultVO> refinePillAuto(
      PlatformType platform, String openId, String recipeName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(refinePillAuto(userId, recipeName));
  }

  // ===================== 内部 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<PillRefiningResultVO> refinePillManual(
      PlatformType platform, String openId, List<String> herbInputs) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(refinePillManual(userId, herbInputs));
  }

  @Transactional
  public PillRefiningResultVO refinePillAuto(Long userId, String recipeName) {
    List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
    PlayerPillRecipe targetRecipe = null;
    ItemTemplate recipeTemplate = null;
    for (PlayerPillRecipe recipe : recipes) {
      ItemTemplate template =
          itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
      if (template != null && template.getName().contains(recipeName)) {
        targetRecipe = recipe;
        recipeTemplate = template;
        break;
      }
    }

    if (targetRecipe == null) {
      return new PillRefiningResultVO(
          false, "未找到丹方：" + recipeName, null, null, 0, null, null, null);
    }

    var recipeScroll = combinationFinder.getRecipeScroll(recipeTemplate);
    if (recipeScroll == null || recipeScroll.requirements().isEmpty()) {
      return new PillRefiningResultVO(false, "丹方数据错误", null, null, 0, null, null, null);
    }
    var requirements = recipeScroll.requirements();

    List<StackableItem> herbs =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(item -> item.getItemType() == ItemType.HERB)
            .toList();

    if (herbs.isEmpty()) {
      return new PillRefiningResultVO(false, "背包中没有药材", null, null, 0, null, null, List.of("所有药材"));
    }

    return combinationFinder.findBestCombination(userId, herbs, requirements, recipeTemplate);
  }

  // ===================== 辅助方法 =====================

  @Transactional
  public PillRefiningResultVO refinePillManual(Long userId, List<String> herbInputs) {
    List<HerbInput> parsedInputs = parseHerbInputs(userId, herbInputs);
    if (parsedInputs.isEmpty()) {
      return new PillRefiningResultVO(false, "药材输入格式错误", null, null, 0, null, null, null);
    }

    Map<String, Integer> elementTotals = new HashMap<>();
    Map<String, Integer> usedHerbs = new HashMap<>();
    for (HerbInput input : parsedInputs) {
      StackableItem herb = input.herb();
      int quantity = input.quantity();
      if (!herb.hasEnoughQuantity(quantity)) {
        return new PillRefiningResultVO(
            false, "药材数量不足：" + herb.getName(), null, null, 0, null, null, null);
      }
      for (String element : List.of("METAL", "WOOD", "WATER", "FIRE", "EARTH")) {
        int value = herb.getElementValue(element) * quantity;
        elementTotals.merge(element, value, Integer::sum);
      }
      usedHerbs.put(herb.getName(), quantity);
    }

    List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
    for (PlayerPillRecipe recipe : recipes) {
      ItemTemplate recipeTemplate =
          itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
      if (recipeTemplate == null) continue;

      var recipeScroll = combinationFinder.getRecipeScroll(recipeTemplate);
      if (recipeScroll == null) continue;
      var requirements = recipeScroll.requirements();
      if (combinationFinder.matchesRequirements(elementTotals, requirements)) {
        double qualityScore = combinationFinder.calculateQualityScore(elementTotals, requirements);
        PillQuality quality = combinationFinder.determineQuality(qualityScore);

        for (HerbInput input : parsedInputs) {
          stackableItemService.reduceStackableItem(
              userId, input.herb().getTemplateId(), input.quantity());
        }

        long resultItemId = recipeScroll.product().itemId();
        int resultQuantity = recipeScroll.product().quantity();
        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) continue;

        combinationFinder.createPillItem(
            userId, resultTemplate, recipeScroll.grade(), quality, resultQuantity);

        return new PillRefiningResultVO(
            true,
            "炼丹成功！",
            resultItemId,
            resultTemplate.getName(),
            resultQuantity,
            quality.getCode(),
            usedHerbs,
            null);
      }
    }

    return new PillRefiningResultVO(false, "药材五行不匹配任何丹方", null, null, 0, null, usedHerbs, null);
  }

  private List<HerbInput> parseHerbInputs(Long userId, List<String> herbInputs) {
    List<HerbInput> result = new ArrayList<>();
    for (String input : herbInputs) {
      String[] parts = input.split("[×xX]");
      if (parts.length != 2) continue;

      String herbName = parts[0].trim();
      int quantity;
      try {
        quantity = Integer.parseInt(parts[1].trim());
      } catch (NumberFormatException e) {
        continue;
      }

      if (quantity <= 0) continue;

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
}
