package top.stillmisty.xiantao.infrastructure.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

public final class WeightedRandom {

  private WeightedRandom() {}

  /**
   * 从列表中按权重随机选择一个元素。
   *
   * @param items 待选列表
   * @param weightExtractor 权重提取函数
   * @param rng 随机数生成器
   * @return 选中的元素，总权重≤0 时返回 null
   */
  public static <T> T select(List<T> items, ToIntFunction<T> weightExtractor, Random rng) {
    int total = 0;
    for (T item : items) {
      total += weightExtractor.applyAsInt(item);
    }
    if (total <= 0) return null;
    int roll = rng.nextInt(total);
    int cumulative = 0;
    for (T item : items) {
      cumulative += weightExtractor.applyAsInt(item);
      if (roll < cumulative) return item;
    }
    throw new IllegalStateException(
        "WeightedRandom: unreachable — weight sum is positive but no item selected");
  }

  /** 预构建累积权重数组，后续 select() 为 O(log n) 二分查找。 适用于需反复从同一列表中采样的场景（如历练遇怪循环）。 */
  public static <T> CumulativeWeighted<T> prebuild(
      List<T> items, ToIntFunction<T> weightExtractor) {
    int[] cumsum = new int[items.size()];
    int total = 0;
    for (int i = 0; i < items.size(); i++) {
      total += weightExtractor.applyAsInt(items.get(i));
      cumsum[i] = total;
    }
    return new CumulativeWeighted<>(List.copyOf(items), cumsum, total);
  }

  public record CumulativeWeighted<T>(List<T> items, int[] cumulativeSums, int totalWeight) {
    public T select(Random rng) {
      if (totalWeight <= 0) return null;
      int roll = rng.nextInt(totalWeight);
      int idx = Arrays.binarySearch(cumulativeSums, roll);
      if (idx < 0) idx = -idx - 1;
      return items.get(idx);
    }
  }

  /** 使用正态分布生成 [min, max] 范围内的随机整数。 以中点为均值、标准差为 (max-min)/4，极值出现概率约 5%。 */
  public static int normalInt(int min, int max, Random rng) {
    if (min >= max) return min;
    double mean = (min + max) / 2.0;
    double stddev = (max - min) / 4.0;
    int result;
    do {
      result = (int) Math.round(rng.nextGaussian() * stddev + mean);
    } while (result < min || result > max);
    return result;
  }
}
