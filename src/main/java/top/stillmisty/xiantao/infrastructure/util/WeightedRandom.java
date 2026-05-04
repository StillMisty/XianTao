package top.stillmisty.xiantao.infrastructure.util;

import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

public final class WeightedRandom {

    private WeightedRandom() {
    }

    /**
     * 从列表中按权重随机选择一个元素。
     *
     * @param items          待选列表
     * @param weightExtractor 权重提取函数
     * @param rng            随机数生成器
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
        return null;
    }

    /**
     * 使用正态分布生成 [min, max] 范围内的随机整数。
     * 以中点为均值、标准差为 (max-min)/4，极值出现概率约 5%。
     */
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
