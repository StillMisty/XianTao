package top.stillmisty.xiantao.service;

import java.util.*;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;

/** 共享贪心组合算法 — 从背包物品中选出满足属性要求的最优组合 */
public class CombinationStrategy {

  private final List<String> attributeNames;
  private final java.util.function.BiFunction<StackableItem, String, Integer> valueExtractor;

  public CombinationStrategy(
      List<String> attributeNames,
      java.util.function.BiFunction<StackableItem, String, Integer> valueExtractor) {
    this.attributeNames = List.copyOf(attributeNames);
    this.valueExtractor = valueExtractor;
  }

  public void tryFindBestCombination(
      Map<String, ElementRange> requirements,
      List<StackableItem> items,
      Map<String, Integer> totals,
      Map<String, Integer> used,
      Map<StackableItem, Integer> remaining) {
    for (int pass = 0; pass < requirements.size(); pass++) {
      boolean anyProgress = false;
      for (var entry : requirements.entrySet()) {
        String attr = entry.getKey();
        int min = entry.getValue().min();
        int currentTotal = totals.getOrDefault(attr, 0);
        if (currentTotal >= min) continue;

        int bestGain = 0;
        StackableItem bestItem = null;
        int bestQty = 0;

        for (StackableItem item : items) {
          Integer rem = remaining.get(item);
          if (rem == null || rem <= 0) continue;
          int val = valueExtractor.apply(item, attr);
          if (val <= 0) continue;
          int needed = (int) Math.ceil((double) (min - currentTotal) / val);
          int toUse = Math.min(needed, rem);
          int gain = computeTotalGain(item, toUse, requirements, totals);
          if (gain > bestGain || (gain == bestGain && toUse < bestQty)) {
            bestGain = gain;
            bestItem = item;
            bestQty = toUse;
          }
        }

        if (bestItem != null && bestQty > 0) {
          applyAttributes(bestItem, bestQty, totals);
          remaining.merge(bestItem, -bestQty, Integer::sum);
          used.merge(bestItem.getName(), bestQty, Integer::sum);
          anyProgress = true;
        }
      }
      if (!anyProgress) break;
    }
  }

  public List<String> collectMissingAttributes(
      Map<String, ElementRange> requirements, Map<String, Integer> totals) {
    List<String> missing = new ArrayList<>();
    for (var entry : requirements.entrySet()) {
      if (totals.getOrDefault(entry.getKey(), 0) < entry.getValue().min()) {
        missing.add(entry.getKey());
      }
    }
    return missing;
  }

  public boolean exceedsAttributeMax(
      Map<String, ElementRange> requirements, Map<String, Integer> totals) {
    for (var entry : requirements.entrySet()) {
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      if (totals.getOrDefault(entry.getKey(), 0) > max) {
        return true;
      }
    }
    return false;
  }

  public String findOverMaxAttribute(
      Map<String, ElementRange> requirements, Map<String, Integer> totals) {
    for (var entry : requirements.entrySet()) {
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      if (totals.getOrDefault(entry.getKey(), 0) > max) {
        return entry.getKey();
      }
    }
    return "";
  }

  public double calculateQualityScore(
      Map<String, Integer> totals, Map<String, ElementRange> requirements) {
    double totalScore = 0;
    int count = 0;
    for (var entry : requirements.entrySet()) {
      int min = entry.getValue().min();
      int max = entry.getValue().max();
      int current = totals.getOrDefault(entry.getKey(), 0);

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

  public boolean matchesRequirements(
      Map<String, Integer> totals, Map<String, ElementRange> requirements) {
    for (var entry : requirements.entrySet()) {
      int min = entry.getValue().min();
      int max = entry.getValue().max() == 0 ? Integer.MAX_VALUE : entry.getValue().max();
      int current = totals.getOrDefault(entry.getKey(), 0);
      if (current < min || current > max) return false;
    }
    return true;
  }

  private void applyAttributes(StackableItem item, int quantity, Map<String, Integer> totals) {
    for (String attr : attributeNames) {
      int value = valueExtractor.apply(item, attr);
      if (value > 0) {
        totals.merge(attr, value * quantity, Integer::sum);
      }
    }
  }

  private int computeTotalGain(
      StackableItem item,
      int quantity,
      Map<String, ElementRange> requirements,
      Map<String, Integer> currentTotals) {
    int gain = 0;
    for (var entry : requirements.entrySet()) {
      String attr = entry.getKey();
      int current = currentTotals.getOrDefault(attr, 0);
      int min = entry.getValue().min();
      if (current >= min) continue;
      int contrib = valueExtractor.apply(item, attr) * quantity;
      if (contrib > 0) {
        gain += Math.min(contrib, min - current);
      }
    }
    return gain;
  }
}
