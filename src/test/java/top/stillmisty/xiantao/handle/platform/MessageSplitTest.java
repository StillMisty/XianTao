package top.stillmisty.xiantao.handle.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageSplitTest {

  @Test
  void shortTextStaysSingleSegment() {
    assertEquals(List.of("正文"), QQPlatformHandler.splitMessage("正文"));
  }

  @Test
  void splitsChineseTextUnderByteLimit() {
    String text = "测".repeat(1000);

    List<String> parts = QQPlatformHandler.splitMessage(text);

    assertEquals(2, parts.size());
    for (String part : parts) {
      assertTrue(
          part.getBytes(StandardCharsets.UTF_8).length <= QQPlatformHandler.MAX_SEGMENT_BYTES);
    }
    assertEquals(text, String.join("", parts));
  }

  @Test
  void prefersLineBreaks() {
    String text = ("测".repeat(300) + "\n").repeat(4);

    List<String> parts = QQPlatformHandler.splitMessage(text);

    assertTrue(parts.size() >= 2);
    for (String part : parts) {
      assertTrue(
          part.getBytes(StandardCharsets.UTF_8).length <= QQPlatformHandler.MAX_SEGMENT_BYTES);
    }
  }

  @Test
  void overlongTextIsTruncatedAtSegmentLimit() {
    String text = "测".repeat(5000);

    List<String> parts = QQPlatformHandler.splitMessage(text);

    assertEquals(QQPlatformHandler.MAX_SEGMENTS, parts.size());
    assertTrue(parts.getLast().contains("后续已省略"));
  }
}
