package top.stillmisty.xiantao.domain.event;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * 类型安全的事件上下文 — 替代 SubEventEffect 管线传递的 Map&lt;String, Object&gt; context。
 *
 * <p>内部委托给 HashMap，通过 {@link ContextKey} 提供类型安全的读写访问。
 */
public final class EventContext {

  private final Map<String, Object> map;

  public EventContext() {
    this.map = new HashMap<>();
  }

  public EventContext(Map<String, Object> map) {
    this.map = map;
  }

  /** 类型安全写入 */
  public <T> void put(ContextKey<T> key, T value) {
    key.put(map, value);
  }

  /** 类型安全读取 */
  public @Nullable <T> T get(ContextKey<T> key) {
    return key.get(map);
  }

  /** 类型安全读取（带默认值） */
  public <T> T getOrDefault(ContextKey<T> key, T defaultVal) {
    return key.getOrDefault(map, defaultVal);
  }

  /** 底层 Map 访问（供需要直接操作 Map 的遗留代码使用） */
  public Map<String, Object> map() {
    return map;
  }

  // ===================== 便捷工厂方法 =====================

  /** 创建空上下文 */
  public static EventContext empty() {
    return new EventContext();
  }

  /** 创建带地图信息的上下文 */
  public static EventContext withMap(top.stillmisty.xiantao.domain.map.entity.MapNode mapNode) {
    EventContext ctx = new EventContext();
    ctx.put(EventContextKeys.MAP_NODE, mapNode);
    ctx.put(EventContextKeys.MAP_NAME, mapNode.getName());
    return ctx;
  }

  /** 创建带地图信息和运势的上下文 */
  public static EventContext withMapAndFortune(
      top.stillmisty.xiantao.domain.map.entity.MapNode mapNode,
      top.stillmisty.xiantao.domain.event.vo.FortuneVO fortune) {
    EventContext ctx = withMap(mapNode);
    ctx.put(EventContextKeys.FORTUNE, fortune);
    return ctx;
  }
}
