package top.stillmisty.xiantao.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
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

  // ===================== 公开 API（含认证） =====================

  private static ItemProperties.Scroll getRecipeScroll(ItemTemplate template) {
    var props = template.typedProperties();
    if (props instanceof ItemProperties.Scroll s) return s;
    return null;
  }

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

    var recipeScroll = getRecipeScroll(recipeTemplate);
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

    return findBestCombination(userId, herbs, requirements, recipeTemplate);
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
      for (String element : List.of("metal", "wood", "water", "fire", "earth")) {
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

      var recipeScroll = getRecipeScroll(recipeTemplate);
      if (recipeScroll == null) continue;
      var requirements = recipeScroll.requirements();
      if (matchesRequirements(elementTotals, requirements)) {
        double qualityScore = calculateQualityScore(elementTotals, requirements);
        String quality = determineQuality(qualityScore);

        for (HerbInput input : parsedInputs) {
          stackableItemService.reduceStackableItem(
              userId, input.herb().getTemplateId(), input.quantity());
        }

        long resultItemId = recipeScroll.product().itemId();
        int resultQuantity = recipeScroll.product().quantity();
        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) continue;

        createPillItem(userId, resultTemplate, recipeScroll.grade(), quality, resultQuantity);

        return new PillRefiningResultVO(
            true,
            "炼丹成功！",
            resultItemId,
            resultTemplate.getName(),
            resultQuantity,
            quality,
            usedHerbs,
            null);
      }
    }

    return new PillRefiningResultVO(false, "药材五行不匹配任何丹方", null, null, 0, null, usedHerbs, null);
  }

  private PillRefiningResultVO findBestCombination(
      Long userId,
      List<StackableItem> herbs,
      List<ItemProperties.ElementRequirement> requirements,
      ItemTemplate recipeTemplate) {
    Map<String, Integer> elementTotals = new HashMap<>();
    Map<String, Integer> usedHerbs = new LinkedHashMap<>();
    Map<StackableItem, Integer> remainingQuantities = new HashMap<>();
    for (StackableItem herb : herbs) {
      remainingQuantities.put(herb, herb.getQuantity());
    }

    for (int pass = 0; pass < requirements.size(); pass++) {
      boolean anyProgress = false;
      for (var req : requirements) {
        String element = req.element();
        int min = req.min();
        int currentTotal = elementTotals.getOrDefault(element, 0);
        if (currentTotal >= min) continue;

        int bestGain = 0;
        StackableItem bestHerb = null;
        int bestQty = 0;

        for (StackableItem herb : herbs) {
          Integer remaining = remainingQuantities.get(herb);
          if (remaining == null || remaining <= 0) continue;
          int herbValue = herb.getElementValue(element);
          if (herbValue <= 0) continue;
          int needed = (int) Math.ceil((double) (min - currentTotal) / herbValue);
          int toUse = Math.min(needed, remaining);
          int totalGain = computeTotalGain(herb, toUse, requirements, elementTotals);
          if (totalGain > bestGain || (totalGain == bestGain && toUse < bestQty)) {
            bestGain = totalGain;
            bestHerb = herb;
            bestQty = toUse;
          }
        }

        if (bestHerb != null && bestQty > 0) {
          applyHerbElements(bestHerb, bestQty, elementTotals);
          remainingQuantities.merge(bestHerb, -bestQty, Integer::sum);
          usedHerbs.merge(bestHerb.getName(), bestQty, Integer::sum);
          anyProgress = true;
        }
      }
      if (!anyProgress) break;
    }

    List<String> missingElements = new ArrayList<>();
    for (var req : requirements) {
      if (elementTotals.getOrDefault(req.element(), 0) < req.min()) {
        missingElements.add(req.element());
      }
    }

    if (!missingElements.isEmpty()) {
      return new PillRefiningResultVO(
          false,
          "缺少药材属性：" + String.join(", ", missingElements),
          null,
          null,
          0,
          null,
          null,
          missingElements);
    }

    for (var req : requirements) {
      String element = req.element();
      int max = req.max() == 0 ? Integer.MAX_VALUE : req.max();
      if (elementTotals.getOrDefault(element, 0) > max) {
        return new PillRefiningResultVO(
            false, "药材属性超过上限：" + element, null, null, 0, null, usedHerbs, null);
      }
    }

    double qualityScore = calculateQualityScore(elementTotals, requirements);
    String quality = determineQuality(qualityScore);

    for (Map.Entry<String, Integer> entry : usedHerbs.entrySet()) {
      for (StackableItem herb : herbs) {
        if (herb.getName().equals(entry.getKey())) {
          stackableItemService.reduceStackableItem(userId, herb.getTemplateId(), entry.getValue());
          break;
        }
      }
    }

    var recipeScroll = getRecipeScroll(recipeTemplate);
    if (recipeScroll == null) {
      return new PillRefiningResultVO(false, "丹方数据错误", null, null, 0, null, null, null);
    }
    long resultItemId = recipeScroll.product().itemId();
    int resultQuantity = recipeScroll.product().quantity();
    ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
    if (resultTemplate == null) {
      return new PillRefiningResultVO(false, "丹药模板不存在", null, null, 0, null, null, null);
    }

    createPillItem(userId, resultTemplate, recipeScroll.grade(), quality, resultQuantity);

    return new PillRefiningResultVO(
        true,
        "炼丹成功！",
        resultItemId,
        resultTemplate.getName(),
        resultQuantity,
        quality,
        usedHerbs,
        null);
  }

  private int computeTotalGain(
      StackableItem herb,
      int quantity,
      List<ItemProperties.ElementRequirement> requirements,
      Map<String, Integer> currentTotals) {
    int gain = 0;
    for (var req : requirements) {
      int current = currentTotals.getOrDefault(req.element(), 0);
      int min = req.min();
      if (current >= min) continue;
      int contrib = herb.getElementValue(req.element()) * quantity;
      if (contrib > 0) {
        gain += Math.min(contrib, min - current);
      }
    }
    return gain;
  }

  private void applyHerbElements(
      StackableItem herb, int quantity, Map<String, Integer> elementTotals) {
    for (String element : List.of("metal", "wood", "water", "fire", "earth")) {
      int value = herb.getElementValue(element);
      if (value > 0) {
        elementTotals.merge(element, value * quantity, Integer::sum);
      }
    }
  }

  private boolean matchesRequirements(
      Map<String, Integer> elementTotals, List<ItemProperties.ElementRequirement> requirements) {
    for (var req : requirements) {
      String element = req.element();
      int min = req.min();
      int max = req.max() == 0 ? Integer.MAX_VALUE : req.max();
      int current = elementTotals.getOrDefault(element, 0);
      if (current < min || current > max) return false;
    }
    return true;
  }

  private double calculateQualityScore(
      Map<String, Integer> elementTotals, List<ItemProperties.ElementRequirement> requirements) {
    double totalScore = 0;
    int count = 0;
    for (var req : requirements) {
      String element = req.element();
      int min = req.min();
      int max = req.max();
      int current = elementTotals.getOrDefault(element, 0);

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

  private String determineQuality(double score) {
    if (score >= 0.8) return "superior";
    if (score >= 0.5) return "normal";
    return "inferior";
  }

  private void createPillItem(
      Long userId, ItemTemplate resultTemplate, int grade, String quality, int quantity) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("grade", grade);
    properties.put("quality", quality);

    Optional<StackableItem> existingItem =
        stackableItemRepository.findByUserIdAndTemplateId(userId, resultTemplate.getId());
    if (existingItem.isPresent()) {
      StackableItem item = existingItem.get();
      item.addQuantity(quantity);
      item.setProperties(properties);
      stackableItemRepository.save(item);
    } else {
      StackableItem newItem =
          StackableItem.create(
              userId,
              resultTemplate.getId(),
              resultTemplate.getType(),
              resultTemplate.getName(),
              quantity);
      newItem.setProperties(properties);
      stackableItemRepository.save(newItem);
    }
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
