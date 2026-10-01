package top.stillmisty.xiantao.service.pill;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.CombinationStrategy;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/** 炼丹组合算法 — 自动匹配药材、计算五行配比、成色判定 */
@Component
@RequiredArgsConstructor
public class PillCombinationFinder {

  private static final List<String> PILL_ELEMENTS =
      Arrays.stream(ElementType.values()).map(ElementType::getCode).toList();

  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;

  private final CombinationStrategy strategy =
      new CombinationStrategy(PILL_ELEMENTS, 5, PillCombinationFinder::getElementValue);

  public ItemProperties.@Nullable Scroll getRecipeScroll(ItemTemplate template) {
    var props = template.typedProperties();
    if (props instanceof ItemProperties.Scroll s) return s;
    return null;
  }

  @Transactional
  public PillRefiningResultVO findBestCombination(
      Long userId,
      List<StackableItem> herbs,
      Map<String, ElementRange> requirements,
      ItemTemplate recipeTemplate) {
    Map<String, Integer> elementTotals = new HashMap<>();
    Map<String, Integer> usedHerbs = new LinkedHashMap<>();
    Map<Long, Integer> usedHerbsById = new LinkedHashMap<>();
    Map<StackableItem, Integer> remainingQuantities = new HashMap<>();
    for (StackableItem herb : herbs) {
      remainingQuantities.put(herb, herb.getQuantity());
    }

    strategy.tryFindBestCombination(
        requirements, herbs, elementTotals, usedHerbs, usedHerbsById, remainingQuantities);
    List<String> missingElements = strategy.collectMissingAttributes(requirements, elementTotals);
    if (!missingElements.isEmpty()) {
      throw new BusinessException(
          ErrorCode.PILL_ELEMENT_MISSING,
          missingElements.stream().map(ElementType::displayName).collect(Collectors.joining(", ")));
    }

    if (strategy.exceedsAttributeMax(requirements, elementTotals)) {
      String overElement = strategy.findOverMaxAttribute(requirements, elementTotals);
      throw new BusinessException(
          ErrorCode.PILL_ELEMENT_EXCEED, ElementType.displayName(overElement));
    }
    return craftPill(userId, elementTotals, usedHerbs, usedHerbsById, requirements, recipeTemplate);
  }

  private PillRefiningResultVO craftPill(
      Long userId,
      Map<String, Integer> elementTotals,
      Map<String, Integer> usedHerbs,
      Map<Long, Integer> usedHerbsById,
      Map<String, ElementRange> requirements,
      ItemTemplate recipeTemplate) {
    double qualityScore = strategy.calculateQualityScore(elementTotals, requirements);
    PillQuality quality = determineQuality(qualityScore);

    // 按组合阶段记录的物品实例 ID 精确扣减，避免同名多行时错扣
    for (Map.Entry<Long, Integer> entry : usedHerbsById.entrySet()) {
      stackableItemService.reduceStackableItem(userId, entry.getKey(), entry.getValue());
    }

    var recipeScroll = getRecipeScroll(recipeTemplate);
    if (recipeScroll == null) {
      throw new BusinessException(ErrorCode.RECIPE_PILL_DATA_ERROR);
    }
    long resultItemId = recipeScroll.resultItemId();
    int resultQuantity = recipeScroll.resultQuantity();
    ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
    if (resultTemplate == null) {
      throw new BusinessException(ErrorCode.RECIPE_PILL_DATA_ERROR);
    }

    createPillItem(userId, resultTemplate, recipeScroll.grade(), quality, resultQuantity);

    return new PillRefiningResultVO(
        "炼丹成功！",
        resultItemId,
        resultTemplate.getName(),
        resultQuantity,
        quality.getCode(),
        usedHerbs,
        null);
  }

  public boolean matchesRequirements(
      Map<String, Integer> elementTotals, Map<String, ElementRange> requirements) {
    return strategy.matchesRequirements(elementTotals, requirements);
  }

  public double calculateQualityScore(
      Map<String, Integer> elementTotals, Map<String, ElementRange> requirements) {
    return strategy.calculateQualityScore(elementTotals, requirements);
  }

  public PillQuality determineQuality(double score) {
    return PillQuality.determine(score);
  }

  public void createPillItem(
      Long userId, ItemTemplate resultTemplate, int grade, PillQuality quality, int quantity) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("grade", grade);
    properties.put("quality", quality.getCode());
    int hash = StackableItem.computeHash(properties);

    // ON CONFLICT 原子累加，避免并发炼制同一丹药时 last-write-wins 丢失产量
    StackableItem newItem =
        StackableItem.create(
            userId,
            resultTemplate.getId(),
            resultTemplate.getType(),
            resultTemplate.getName() + "-" + quality.getChineseName(),
            quantity);
    newItem.setTags(resultTemplate.getTags());
    newItem.setProperties(properties);
    newItem.setPropertiesHash(hash);
    stackableItemRepository.upsertIncrementQuantity(newItem);
  }

  @SuppressWarnings("unused")
  private static int getElementValue(StackableItem item, String element) {
    return item.getElementValue(element);
  }
}
