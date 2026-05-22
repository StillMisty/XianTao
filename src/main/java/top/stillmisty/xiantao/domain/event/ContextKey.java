package top.stillmisty.xiantao.domain.event;

import java.util.Map;

/** 类型安全的上下文键，替代裸 String key 的 Map<String, Object> 存取。 存储类型 T 为编译期提示，实际运行时类型由调用方保证。 */
public record ContextKey<T>(String name) {

  public void put(Map<String, Object> map, T value) {
    map.put(name, value);
  }

  @SuppressWarnings("unchecked")
  public T get(Map<String, Object> map) {
    return (T) map.get(name);
  }

  @SuppressWarnings("unchecked")
  public T getOrDefault(Map<String, Object> map, T defaultVal) {
    Object val = map.get(name);
    return val != null ? (T) val : defaultVal;
  }
}
