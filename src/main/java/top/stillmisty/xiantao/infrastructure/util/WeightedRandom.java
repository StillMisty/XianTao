package top.stillmisty.xiantao.infrastructure.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;
import org.jspecify.annotations.Nullable;

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
  public static <T> @Nullable T select(
      List<T> items, ToIntFunction<T> weightExtractor, Random rng) {
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

  @SuppressWarnings("ArrayRecordComponent")
  public record CumulativeWeighted<T>(List<T> items, int[] cumulativeSums, int totalWeight) {
    public @Nullable T select(Random rng) {
      if (totalWeight <= 0) return null;
      int roll = rng.nextInt(totalWeight);
      int idx = Arrays.binarySearch(cumulativeSums, roll);
      if (idx < 0) idx = -idx - 1;
      return items.get(idx);
    }
  }

  /**
   * 从列表中按权重不放回抽取 n 个元素。若总权重≤0则退化为均匀随机 shuffle。
   *
   * @param items 待选列表
   * @param weightExtractor 权重提取函数
   * @param n 抽取数量
   * @param rng 随机数生成器
   * @return 选中的元素列表（顺序不保证）
   */
  public static <T> List<T> selectN(
      List<T> items, ToIntFunction<T> weightExtractor, int n, Random rng) {
    if (items.isEmpty() || n <= 0) return List.of();
    if (n >= items.size()) return new ArrayList<>(items);

    int totalWeight = items.stream().mapToInt(weightExtractor).sum();
    if (totalWeight <= 0) {
      List<T> shuffled = new ArrayList<>(items);
      Collections.shuffle(shuffled, rng);
      return new ArrayList<>(shuffled.subList(0, n));
    }

    List<T> pool = new ArrayList<>(items);
    List<T> result = new ArrayList<>(n);
    for (int i = 0; i < n && !pool.isEmpty(); i++) {
      T selected = WeightedRandom.select(pool, weightExtractor, rng);
      if (selected == null) break;
      result.add(selected);
      pool.remove(selected);
    }
    return result;
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
