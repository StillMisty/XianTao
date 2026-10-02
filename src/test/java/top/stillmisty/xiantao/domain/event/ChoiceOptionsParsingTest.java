package top.stillmisty.xiantao.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** ChoiceOptions 解析：兼容 ActivityEvent.params 与 GameEvent.effects 两种 JSONB 结构 */
class ChoiceOptionsParsingTest {

  @Test
  void parsesActivityParamsWithDirectOptions() {
    Map<String, Object> params =
        Map.of(
            "options",
            List.of(
                Map.of(
                    "key",
                    "A",
                    "text",
                    "测试选项",
                    "effects",
                    List.of(Map.of("type", "ADD_ITEM", "template_id", 123, "count", 1)))));

    EffectData.ChoiceOptions parsed = EffectData.ChoiceOptions.fromParamsMap(params);

    assertEquals(1, parsed.options().size());
    assertEquals("A", parsed.options().getFirst().key());
    assertEquals(1, Objects.requireNonNull(parsed.options().getFirst().effects()).size());
  }

  @Test
  void parsesGameEventEffectsWithNestedChoice() {
    Map<String, Object> params =
        Map.of(
            "choice",
            Map.of("options", List.of(Map.of("key", "B", "text", "另一个选项", "effects", List.of()))));

    EffectData.ChoiceOptions parsed = EffectData.ChoiceOptions.fromParamsMap(params);

    assertEquals(1, parsed.options().size());
    assertEquals("B", parsed.options().getFirst().key());
    assertTrue(Objects.requireNonNull(parsed.options().getFirst().effects()).isEmpty());
  }

  @Test
  void unparsableParamsYieldEmptyOptions() {
    assertTrue(EffectData.ChoiceOptions.fromParamsMap(Map.of()).options().isEmpty());
  }
}
