package top.stillmisty.xiantao.infrastructure.util;

import java.util.Map;
import java.util.Random;
import org.jspecify.annotations.Nullable;

public final class TypeUtils {

  private TypeUtils() {}

  public static @Nullable Long toLong(Object value) {
    if (value instanceof Long longVal) return longVal;
    if (value instanceof Integer intVal) return intVal.longValue();
    if (value instanceof Number number) return number.longValue();
    return null;
  }

  // ===================== 类型安全的 Map 访问 =====================

  /**
   * 从 Map 中获取 Long 值，兼容 Integer/Long/Number 类型
   *
   * @return Long 值，不存在或类型不匹配时返回 null
   */
  public static @Nullable Long getLong(Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Long l) return l;
    if (val instanceof Integer i) return i.longValue();
    if (val instanceof Number n) return n.longValue();
    return null;
  }

  /**
   * 从 Map 中获取 Integer 值，兼容 Integer/Long/Number 类型
   *
   * @return Integer 值，不存在或类型不匹配时返回 null
   */
  public static @Nullable Integer getInt(Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Integer i) return i;
    if (val instanceof Long l) return l.intValue();
    if (val instanceof Number n) return n.intValue();
    return null;
  }

  /**
   * 从 Map 中获取 Double 值，兼容 Number 类型
   *
   * @return Double 值，不存在或类型不匹配时返回 null
   */
  public static @Nullable Double getDouble(Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Double d) return d;
    if (val instanceof Number n) return n.doubleValue();
    return null;
  }

  /**
   * 从 Map 中获取 String 值
   *
   * @return String 值，不存在或类型不匹配时返回 null
   */
  public static @Nullable String getString(Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val instanceof String s ? s : null;
  }

  /**
   * 从 Map 中获取 Long 值，带默认值
   *
   * @return Long 值，不存在或类型不匹配时返回 defaultVal
   */
  public static long getLongOrDefault(Map<String, Object> map, String key, long defaultVal) {
    Long val = getLong(map, key);
    return val != null ? val : defaultVal;
  }

  /**
   * 从 Map 中获取 Integer 值，带默认值
   *
   * @return Integer 值，不存在或类型不匹配时返回 defaultVal
   */
  public static int getIntOrDefault(Map<String, Object> map, String key, int defaultVal) {
    Integer val = getInt(map, key);
    return val != null ? val : defaultVal;
  }

  /**
   * 从 Map 中获取 Double 值，带默认值
   *
   * @return Double 值，不存在或类型不匹配时返回 defaultVal
   */
  public static double getDoubleOrDefault(Map<String, Object> map, String key, double defaultVal) {
    Double val = getDouble(map, key);
    return val != null ? val : defaultVal;
  }

  /**
   * 从 Map 中获取 String 值，带默认值
   *
   * @return String 值，不存在或类型不匹配时返回 defaultVal
   */
  public static String getStringOrDefault(Map<String, Object> map, String key, String defaultVal) {
    String val = getString(map, key);
    return val != null ? val : defaultVal;
  }

  // ===================== 权重随机选择 =====================

  /**
   * 从权重Map中按权重随机选择一个key
   *
   * @param weightMap key → weight 的 Map
   * @param rng 随机数生成器
   * @return 选中的 key，如果总权重为0则返回 null
   */
  public static @Nullable Long weightedRandomSelect(Map<Long, Integer> weightMap, Random rng) {
    var entries =
        weightMap.entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue())).toList();
    var result = WeightedRandom.select(entries, Map.Entry::getValue, rng);
    return result != null ? result.getKey() : null;
  }
}
