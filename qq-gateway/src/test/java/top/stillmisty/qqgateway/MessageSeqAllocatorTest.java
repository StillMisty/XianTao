package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MessageSeqAllocatorTest {

  @Test
  void incrementsPerMessageIndependently() {
    MessageSeqAllocator allocator = new MessageSeqAllocator();
    assertEquals(1, allocator.next("msg-1"));
    assertEquals(2, allocator.next("msg-1"));
    assertEquals(1, allocator.next("msg-2"));
    assertEquals(3, allocator.next("msg-1"));
    assertEquals(3, allocator.current("msg-1"));
    assertEquals(0, allocator.current("unknown"));
  }

  @Test
  void evictsEldestBeyondCapacity() {
    MessageSeqAllocator allocator = new MessageSeqAllocator();
    for (int i = 0; i < 5000; i++) {
      allocator.next("msg-" + i);
    }
    assertTrue(allocator.size() <= 4096);
    assertEquals(0, allocator.current("msg-0"));
    assertEquals(1, allocator.current("msg-4999"));
  }

  @Test
  void deduplicationIsFirstSeenOnly() {
    EventDeduplicator deduplicator = new EventDeduplicator();
    assertTrue(deduplicator.firstSeen("event-1"));
    assertFalse(deduplicator.firstSeen("event-1"));
    assertTrue(deduplicator.firstSeen("event-2"));

    for (int i = 0; i < 9000; i++) {
      deduplicator.firstSeen("event-" + i);
    }
    assertTrue(deduplicator.size() <= 8192);
  }
}
