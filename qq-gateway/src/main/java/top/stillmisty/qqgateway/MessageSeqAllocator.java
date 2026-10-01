package top.stillmisty.qqgateway;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 被动回复序号分配器。
 *
 * <p>QQ 要求同一 {@code msg_id} 的多次回复使用递增的 {@code msg_seq}，相同 {@code msg_id + msg_seq} 会被拒绝。 被动回复窗口 5
 * 分钟，这里只保留最近使用的条目，超出容量按最早访问淘汰。
 */
final class MessageSeqAllocator {

  private static final int MAX_ENTRIES = 4096;

  private final Map<String, Integer> seqByMessageId =
      new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
          return size() > MAX_ENTRIES;
        }
      };

  /** 分配下一个序号，从 1 开始。 */
  synchronized int next(String messageId) {
    return seqByMessageId.merge(messageId, 1, Integer::sum);
  }

  /** 当前已分配的序号，未分配返回 0（测试用）。 */
  synchronized int current(String messageId) {
    Integer value = seqByMessageId.get(messageId);
    return value == null ? 0 : value;
  }

  /** 当前条目数（测试用）。 */
  synchronized int size() {
    return seqByMessageId.size();
  }
}
