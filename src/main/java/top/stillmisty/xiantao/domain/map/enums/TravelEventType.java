package top.stillmisty.xiantao.domain.map.enums;

import com.mybatisflex.annotation.EnumValue;
import java.util.List;
import lombok.Getter;
import top.stillmisty.xiantao.domain.map.entity.TravelEventEntry;

/** 旅行事件类型枚举 */
@Getter
public enum TravelEventType {

  /** 遇袭 */
  AMBUSH("ambush", "遇袭"),

  /** 捡漏 */
  FIND_TREASURE("find_treasure", "捡漏"),

  /** 毒雾天气 */
  WEATHER("weather", "毒雾天气"),

  /** 安全通行 */
  SAFE_PASSAGE("safe_passage", "安全通行");

  @EnumValue private final String code;
  private final String name;

  TravelEventType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  /** 根据代码查找事件类型 */
  public static TravelEventType fromCode(String code) {
    for (TravelEventType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    return null;
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
        TravelEventType type = fromCode(entry.eventType());
        if (type != null) {
          return type;
        }
      }
    }

    return SAFE_PASSAGE;
  }
}
