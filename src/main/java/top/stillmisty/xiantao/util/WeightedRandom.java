package top.stillmisty.xiantao.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

public final class WeightedRandom {

  private WeightedRandom() {}

  public static <T> T weightedRandom(List<T> items, ToIntFunction<T> weightFn, int totalWeight) {
    if (items == null || items.isEmpty() || totalWeight <= 0) return null;
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (T item : items) {
      cumulative += weightFn.applyAsInt(item);
      if (roll < cumulative) return item;
    }
    return items.getLast();
  }
}
