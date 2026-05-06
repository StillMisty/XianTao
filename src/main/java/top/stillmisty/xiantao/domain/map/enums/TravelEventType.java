package top.stillmisty.xiantao.domain.map.enums;

import com.mybatisflex.annotation.EnumValue;
import java.util.List;
import lombok.Getter;
import top.stillmisty.xiantao.domain.map.entity.TravelEventEntry;

/** 旅行事件类型枚举 */
@Getter
public enum TravelEventType {

  /** 遇袭 */
  AMBUSH("AMBUSH", "遇袭"),

  /** 捡漏 */
  FIND_TREASURE("FIND_TREASURE", "捡漏"),

  /** 毒雾天气 */
  WEATHER("WEATHER", "毒雾天气"),

  /** 安全通行 */
  SAFE_PASSAGE("SAFE_PASSAGE", "安全通行");

  @EnumValue private final String code;
  private final String name;

  TravelEventType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  /** 根据代码查找事件类型 */
  public static TravelEventType fromCode(String code) {
    for (TravelEventType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown TravelEventType code: " + code);
  }

  /**
   * 根据权重随机获取事件类型
   *
   * @param eventEntries 事件权重列表
   * @return 随机事件类型
   */
  public static TravelEventType randomEvent(List<TravelEventEntry> eventEntries) {
    if (eventEntries == null || eventEntries.isEmpty()) {
      return SAFE_PASSAGE;
    }

    int totalWeight = eventEntries.stream().mapToInt(TravelEventEntry::weight).sum();
    if (totalWeight == 0) {
      return SAFE_PASSAGE;
    }

    int random = (int) (Math.random() * totalWeight);
    int currentWeight = 0;

    for (TravelEventEntry entry : eventEntries) {
      currentWeight += entry.weight();
      if (random < currentWeight) {
        try {
          return fromCode(entry.eventType());
        } catch (IllegalArgumentException ignored) {
          // fall through to next entry or default
        }
      }
    }

    return SAFE_PASSAGE;
  }
}
