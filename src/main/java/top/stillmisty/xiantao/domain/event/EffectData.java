package top.stillmisty.xiantao.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * 事件效果数据密封接口 — 替代 GameEvent.effects 和 ActivityEvent.params 的 Map&lt;String, Object&gt;，提供类型安全访问。
 *
 * <p>ChoiceOptions: GameEvent 的选项效果数据 ActivityConfig: ActivityEvent 的效果配置（含 effects 列表、分支、怪物模板等）
 */
public sealed interface EffectData {

  // ===================== GameEvent 选项效果 =====================

  /**
   * 选项效果数据 — GameEvent.effects 的类型安全表示。
   *
   * <p>JSONB 结构: {@code {"choice": {"options": [{"key":"A","text":"...","effects":[...]}, ...]}}}
   */
  record ChoiceOptions(@JsonProperty("choice") @Nullable ChoiceData choice) implements EffectData {

    public List<Option> options() {
      return choice != null && choice.options() != null ? choice.options() : List.of();
    }

    /** 从原始 Map 构造 ChoiceOptions（params 中含 choice 键） */
    @SuppressWarnings("unchecked")
    public static ChoiceOptions fromParamsMap(Map<String, Object> params) {
      try {
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(params);
        return mapper.readValue(json, ChoiceOptions.class);
      } catch (Exception e) {
        return new ChoiceOptions(null);
      }
    }
  }

  record ChoiceData(@JsonProperty("options") @Nullable List<Option> options) {}

  record Option(
      @JsonProperty("key") @Nullable String key,
      @JsonProperty("text") @Nullable String text,
      @JsonProperty("effects") @Nullable List<Map<String, Object>> effects) {}

  // ===================== ActivityEvent 效果配置 =====================

  /**
   * 活动事件效果配置 — ActivityEvent.params 的类型安全表示。
   *
   * <p>JSONB 结构: {@code {"effects": [{type, amount, ...}, ...], "branches": [...],
   * "monster_template_id": 123, ...}}
   */
  record ActivityConfig(
      @JsonProperty("effects") @Nullable List<Map<String, Object>> effects,
      @JsonProperty("branches") @Nullable List<Map<String, Object>> branches,
      @JsonProperty("monster_template_id") @Nullable Long monsterTemplateId,
      @JsonProperty("min_count") @Nullable Integer minCount,
      @JsonProperty("max_count") @Nullable Integer maxCount)
      implements EffectData {

    /** 从原始 Map 构造 ActivityConfig */
    public static ActivityConfig fromMap(Map<String, Object> map) {
      return new ActivityConfig(
          getMapList(map, "effects"),
          getMapList(map, "branches"),
          getLong(map, "monster_template_id"),
          getInt(map, "min_count"),
          getInt(map, "max_count"));
    }

    @SuppressWarnings("unchecked")
    private static @Nullable List<Map<String, Object>> getMapList(
        Map<String, Object> map, String key) {
      Object val = map.get(key);
      if (val instanceof List<?> list) {
        return list.stream()
            .filter(Map.class::isInstance)
            .map(e -> (Map<String, Object>) e)
            .toList();
      }
      return null;
    }

    private static @Nullable Long getLong(Map<String, Object> map, String key) {
      Object val = map.get(key);
      if (val instanceof Long l) return l;
      if (val instanceof Integer i) return i.longValue();
      if (val instanceof Number n) return n.longValue();
      return null;
    }

    private static @Nullable Integer getInt(Map<String, Object> map, String key) {
      Object val = map.get(key);
      if (val instanceof Integer i) return i;
      if (val instanceof Long l) return l.intValue();
      if (val instanceof Number n) return n.intValue();
      return null;
    }
  }

  // ===================== 工厂方法 =====================

  /** 从原始 Map 构造 EffectData — 用于渐进式迁移 */
  static EffectData fromMap(Map<String, Object> map) {
    if (map.containsKey("choice")) {
      return new ChoiceOptions(null); // 由 Jackson 反序列化处理
    }
    return ActivityConfig.fromMap(map);
  }
}
