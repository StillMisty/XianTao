package top.stillmisty.xiantao.domain.beast.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.beast.enums.TriggerType;

/** 变异效果定义，存储在 MutationTraitConfig.effects JSONB 中 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MutationEffect(
    MutationEffectType type, double value, @Nullable TriggerCondition condition) {

  /** 创建无条件效果 */
  public static MutationEffect of(MutationEffectType type, double value) {
    return new MutationEffect(type, value, null);
  }

  /** 创建带条件效果 */
  public static MutationEffect withCondition(
      MutationEffectType type, double value, TriggerType trigger, Double threshold) {
    return new MutationEffect(type, value, new TriggerCondition(trigger, threshold, null));
  }

  /** 触发条件 */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TriggerCondition(
      @JsonProperty("trigger") TriggerType trigger,
      @JsonProperty("threshold") Double threshold,
      @JsonProperty("turns") @Nullable Integer turns) {}
}
