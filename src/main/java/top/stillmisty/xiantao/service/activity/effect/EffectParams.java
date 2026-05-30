package top.stillmisty.xiantao.service.activity.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * 子事件效果参数密封接口 — 替代 Map&lt;String, Object&gt; params 提供类型安全访问。
 *
 * <p>每个 SubEventEffectType 对应一个 params record，由 Jackson 自动反序列化。
 */
public sealed interface EffectParams {

  // ===================== 经验相关 =====================

  /** ADD_EXP / ADD_SPIRIT_STONES / TAKE_SPIRIT_STONES / TAKE_DAMAGE_FLAT / HEAL_FLAT 共用 */
  record AmountParams(
      @JsonProperty("amount") @Nullable Long amount,
      @JsonProperty("min") @Nullable Long min,
      @JsonProperty("max") @Nullable Long max)
      implements EffectParams {

    /** 解析实际数值：有 amount 直接返回，否则在 [min, max] 范围随机 */
    public long resolveAmount() {
      if (amount != null) return amount;
      long minVal = min != null ? min : 0;
      long maxVal = max != null ? max : 0;
      if (minVal == 0 && maxVal == 0) return 0;
      return java.util.concurrent.ThreadLocalRandom.current().nextLong(minVal, maxVal + 1);
    }
  }

  // ===================== 百分比相关 =====================

  /** ADD_EXP_PERCENT / TAKE_DAMAGE_PERCENT */
  record PercentParams(@JsonProperty("percent") @Nullable Double percent) implements EffectParams {}

  /** MULTIPLY_BOUNTY_REWARD */
  record MultiplierParams(@JsonProperty("multiplier") @Nullable Double multiplier)
      implements EffectParams {}

  // ===================== 物品相关 =====================

  /** ADD_ITEM */
  record AddItemParams(
      @JsonProperty("template_id") @Nullable Long templateId,
      @JsonProperty("count") @Nullable Integer count,
      @JsonProperty("min") @Nullable Integer min,
      @JsonProperty("max") @Nullable Integer max)
      implements EffectParams {

    /** 解析数量：有 count 直接返回，否则在 [min, max] 范围随机 */
    public int resolveCount() {
      if (count != null && count > 0) return count;
      int minVal = min != null ? min : 1;
      int maxVal = max != null ? max : 1;
      return java.util.concurrent.ThreadLocalRandom.current().nextInt(minVal, maxVal + 1);
    }
  }

  /** ADD_RANDOM_ITEM */
  record AddRandomItemParams(
      @JsonProperty("chance") @Nullable Double chance,
      @JsonProperty("template_ids") @Nullable List<Long> templateIds)
      implements EffectParams {}

  /** CREATE_EQUIPMENT */
  record CreateEquipmentParams(@JsonProperty("template_id") @Nullable Long templateId)
      implements EffectParams {}

  // ===================== 掉落相关 =====================

  /** DROP_SPECIALTY */
  record DropSpecialtyParams(@JsonProperty("count") @Nullable Integer count)
      implements EffectParams {}

  // ===================== 纯叙事 =====================

  /** PURE_NARRATIVE（无参数） */
  record EmptyParams() implements EffectParams {}

  // ===================== 向后兼容 =====================

  /**
   * 从原始 Map 构造 EffectParams — 用于渐进式迁移
   *
   * @param type 效果类型
   * @param params 原始参数 Map
   * @return 类型安全的 EffectParams
   */
  static EffectParams fromMap(SubEventEffectType type, Map<String, Object> params) {
    return switch (type) {
      case ADD_EXP, ADD_SPIRIT_STONES, TAKE_SPIRIT_STONES, TAKE_DAMAGE_FLAT, HEAL_FLAT ->
          new AmountParams(
              getLong(params, "amount"), getLong(params, "min"), getLong(params, "max"));
      case ADD_EXP_PERCENT -> new PercentParams(getDouble(params, "percent"));
      case TAKE_DAMAGE_PERCENT -> new PercentParams(getDouble(params, "amount"));
      case MULTIPLY_BOUNTY_REWARD -> new MultiplierParams(getDouble(params, "multiplier"));
      case ADD_ITEM ->
          new AddItemParams(
              getLong(params, "template_id"),
              getInt(params, "count"),
              getInt(params, "min"),
              getInt(params, "max"));
      case ADD_RANDOM_ITEM -> {
        List<Long> ids = getLongList(params, "template_ids");
        yield new AddRandomItemIdsParams(getDouble(params, "chance"), ids);
      }
      case CREATE_EQUIPMENT -> new CreateEquipmentParams(getLong(params, "template_id"));
      case DROP_SPECIALTY -> new DropSpecialtyParams(getInt(params, "count"));
      case PURE_NARRATIVE -> new EmptyParams();
    };
  }

  // ===================== 内部工具方法 =====================

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

  private static @Nullable Double getDouble(Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Double d) return d;
    if (val instanceof Number n) return n.doubleValue();
    return null;
  }

  @SuppressWarnings("unchecked")
  private static List<Long> getLongList(Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof List<?> list) {
      return list.stream()
          .filter(Number.class::isInstance)
          .map(Number.class::cast)
          .map(Number::longValue)
          .toList();
    }
    return List.of();
  }

  /** ADD_RANDOM_ITEM 的实际 params 类型（template_ids 为 List<Long>） */
  record AddRandomItemIdsParams(
      @JsonProperty("chance") @Nullable Double chance,
      @JsonProperty("template_ids") @Nullable List<Long> templateIds)
      implements EffectParams {}
}
