package top.stillmisty.xiantao.service.activity.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 类型安全的效果条目 — 替代 {@code Map<String, Object>} 表示单个效果。
 *
 * <p>JSONB 存储格式: {@code {"type": "ADD_EXP", "amount": 100}}，其中 {@code type} 为判别字段， 其余字段由 {@link
 * EffectParams} 密封接口处理。
 */
public record EffectEntry(@JsonProperty("type") SubEventEffectType type, EffectParams params) {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** 将 EffectEntry 转换为 {@code Map<String, Object>}，供 SubEventEffectExecutor 分发 */
  public Map<String, Object> toEffectMap() {
    Map<String, Object> map = MAPPER.convertValue(params, new TypeReference<>() {});
    map.put("type", type.name());
    return map;
  }
}
