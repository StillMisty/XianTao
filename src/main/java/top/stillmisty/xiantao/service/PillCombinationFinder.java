package top.stillmisty.xiantao.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;

/** 炼丹组合算法 — 自动匹配药材、计算五行配比、成色判定 */
@Component
@RequiredArgsConstructor
public class PillCombinationFinder {

  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;

  public ItemProperties.Scroll getRecipeScroll(ItemTemplate template) {
    var props = template.typedProperties();
    if (props instanceof ItemProperties.Scroll s) return s;
    return null;
  }

  @Transactional
  public PillRefiningResultVO findBestCombination(
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

    tryFindBestHerbCombination(requirements, herbs, elementTotals, usedHerbs, remainingQuantities);

    List<String> missingElements = collectMissingElements(requirements, elementTotals);
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

    if (exceedsElementMax(requirements, elementTotals)) {
      String overElement = findOverMaxElement(requirements, elementTotals);
      return new PillRefiningResultVO(
          false, "药材属性超过上限：" + overElement, null, null, 0, null, usedHerbs, null);
    }

    return craftPill(userId, herbs, elementTotals, usedHerbs, requirements, recipeTemplate);
  }

  private void tryFindBestHerbCombination(
      List<ItemProperties.ElementRequirement> requirements,
      List<StackableItem> herbs,
      Map<String, Integer> elementTotals,
      Map<String, Integer> usedHerbs,
      Map<StackableItem, Integer> remainingQuantities) {
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
  }

  private List<String> collectMissingElements(
      List<ItemProperties.ElementRequirement> requirements, Map<String, Integer> elementTotals) {
    List<String> missingElements = new ArrayList<>();
    for (var req : requirements) {
      if (elementTotals.getOrDefault(req.element(), 0) < req.min()) {
        missingElements.add(req.element());
      }
    }
    return missingElements;
  }

  private boolean exceedsElementMax(
      List<ItemProperties.ElementRequirement> requirements, Map<String, Integer> elementTotals) {
    for (var req : requirements) {
      String element = req.element();
      int max = req.max() == 0 ? Integer.MAX_VALUE : req.max();
      if (elementTotals.getOrDefault(element, 0) > max) {
        return true;
      }
    }
    return false;
  }

  private String findOverMaxElement(
      List<ItemProperties.ElementRequirement> requirements, Map<String, Integer> elementTotals) {
    for (var req : requirements) {
      String element = req.element();
      int max = req.max() == 0 ? Integer.MAX_VALUE : req.max();
      if (elementTotals.getOrDefault(element, 0) > max) {
        return element;
      }
    }
    return "";
  }

  private PillRefiningResultVO craftPill(
      Long userId,
      List<StackableItem> herbs,
      Map<String, Integer> elementTotals,
      Map<String, Integer> usedHerbs,
      List<ItemProperties.ElementRequirement> requirements,
      ItemTemplate recipeTemplate) {
    double qualityScore = calculateQualityScore(elementTotals, requirements);
    PillQuality quality = determineQuality(qualityScore);

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
        quality.getCode(),
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
    for (String element : List.of("METAL", "WOOD", "WATER", "FIRE", "EARTH")) {
      int value = herb.getElementValue(element);
      if (value > 0) {
        elementTotals.merge(element, value * quantity, Integer::sum);
      }
    }
  }

  public boolean matchesRequirements(
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

  public double calculateQualityScore(
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

  public PillQuality determineQuality(double score) {
    return PillQuality.determine(score);
  }

  public void createPillItem(
      Long userId, ItemTemplate resultTemplate, int grade, PillQuality quality, int quantity) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("grade", grade);
    properties.put("quality", quality.getCode());

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
}
