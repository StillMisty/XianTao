package top.stillmisty.xiantao.handle.platform;

import java.util.ArrayList;
import java.util.List;

/** 消息分段 — 按平台的字节与段数限制切分 markdown，优先在换行处断开。 */
final class MessageSegments {

  /** 超出段数上限时的截断标注 */
  static final String TRUNCATION_NOTICE = "\n……（内容过长，后续已省略）";

  private MessageSegments() {}

  static List<String> split(String text, int maxSegmentBytes, int maxSegments) {
    List<String> parts = new ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      int end = byteLimitedEnd(text, start, maxSegmentBytes);
      if (end >= text.length()) {
        parts.add(text.substring(start));
        break;
      }
      if (parts.size() == maxSegments - 1) {
        int keep = byteLimitedEnd(text, start, maxSegmentBytes - 64);
        parts.add(text.substring(start, keep).stripTrailing() + TRUNCATION_NOTICE);
        break;
      }
      int cut = preferLineBreak(text, start, end);
      parts.add(text.substring(start, cut).strip());
      start = cut;
    }
    return parts.isEmpty() ? List.of(text) : parts;
  }

  /** [start, end) 中不超过 maxBytes 的最大结束位置（不切开代理对）。 */
  private static int byteLimitedEnd(String text, int start, int maxBytes) {
    int bytes = 0;
    int index = start;
    while (index < text.length()) {
      int codePoint = text.codePointAt(index);
      int byteCount = utf8Length(codePoint);
      if (bytes + byteCount > maxBytes) {
        break;
      }
      bytes += byteCount;
      index += Character.charCount(codePoint);
    }
    return index;
  }

  private static int utf8Length(int codePoint) {
    if (codePoint < 0x80) {
      return 1;
    }
    if (codePoint < 0x800) {
      return 2;
    }
    if (codePoint < 0x10000) {
      return 3;
    }
    return 4;
  }

  /** 尽量在靠后的换行处切分（至少保留一半内容，避免切得太碎）。 */
  private static int preferLineBreak(String text, int start, int end) {
    int newline = text.lastIndexOf('\n', end - 1);
    if (newline > start + (end - start) / 2) {
      return newline + 1;
    }
    return end;
  }
}
