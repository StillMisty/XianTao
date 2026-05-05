package top.stillmisty.xiantao.infrastructure.util;

import java.util.Map;
import java.util.Random;

public final class TypeUtils {

  private TypeUtils() {}

  public static Long toLong(Object value) {
    if (value instanceof Long longVal) return longVal;
    if (value instanceof Integer intVal) return intVal.longValue();
    if (value instanceof Number number) return number.longValue();
    return null;
  }

  /**
   * 从权重Map中按权重随机选择一个key
   *
   * @param weightMap key → weight 的 Map
   * @param rng 随机数生成器
   * @return 选中的 key，如果总权重为0则返回 null
   */
  public static Long weightedRandomSelect(Map<Long, Integer> weightMap, Random rng) {
    var entries =
        weightMap.entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue())).toList();
    var result = WeightedRandom.select(entries, Map.Entry::getValue, rng);
    return result != null ? result.getKey() : null;
  }
}
