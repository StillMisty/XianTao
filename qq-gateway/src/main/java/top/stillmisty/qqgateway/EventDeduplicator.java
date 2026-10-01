package top.stillmisty.qqgateway;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 事件去重器。
 *
 * <p>QQ 可能对同一事件重复推送，按事件 ID 去重可避免重复执行命令；容量上限内的最近事件被记住， 超出后按最早访问淘汰（远大于平台的重推时间窗口）。
 */
final class EventDeduplicator {

  private static final int MAX_ENTRIES = 8192;

  private final Map<String, Boolean> seen =
      new LinkedHashMap<>(1024, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
          return size() > MAX_ENTRIES;
        }
      };

  /** 返回是否为首次出现；同一 key 的后续调用返回 false。 */
  synchronized boolean firstSeen(String key) {
    return seen.putIfAbsent(key, Boolean.TRUE) == null;
  }

  /** 当前条目数（测试用）。 */
  synchronized int size() {
    return seen.size();
  }
}
