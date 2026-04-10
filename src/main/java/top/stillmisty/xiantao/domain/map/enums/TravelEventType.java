package top.stillmisty.xiantao.domain.map.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 旅行事件类型枚举
 */
@Getter
public enum TravelEventType {

    /**
     * 遇袭
     */
    AMBUSH("ambush", "遇袭", 40),

    /**
     * 捡漏
     */
    FIND_TREASURE("find_treasure", "捡漏", 10),

    /**
     * 毒雾天气
     */
    WEATHER("weather", "毒雾天气", 50),

    /**
     * 安全通行
     */
    SAFE_PASSAGE("safe_passage", "安全通行", 0);

    @EnumValue
    private final String code;
    private final String name;
    private final int weight;

    TravelEventType(String code, String name, int weight) {
        this.code = code;
        this.name = name;
        this.weight = weight;
    }

    /**
     * 根据代码查找事件类型
     */
    public static TravelEventType fromCode(String code) {
        for (TravelEventType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据中文名称查找事件类型
     */
    public static TravelEventType fromChineseName(String name) {
        for (TravelEventType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据权重随机获取事件类型
     * @param eventTypeWeights 事件权重表 (eventTypeName -> weight)
     * @return 随机事件类型
     */
    public static TravelEventType randomEvent(java.util.Map<String, Integer> eventTypeWeights) {
        if (eventTypeWeights == null || eventTypeWeights.isEmpty()) {
            return SAFE_PASSAGE;
        }

        int totalWeight = eventTypeWeights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight == 0) {
            return SAFE_PASSAGE;
        }

        int random = (int) (Math.random() * totalWeight);
        int currentWeight = 0;

        for (TravelEventType type : values()) {
            if (type == SAFE_PASSAGE) continue; // 跳过安全通行

            Integer weight = eventTypeWeights.get(type.getName());
            if (weight == null) weight = 0;

            currentWeight += weight;
            if (random < currentWeight) {
                return type;
            }
        }

        return SAFE_PASSAGE;
    }
}
