package top.stillmisty.xiantao.handle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NextActionsTest {

  @Test
  void suggestOutsideCollectorIsIgnored() {
    NextActions.suggest("翠竹林", "前往 翠竹林");

    NextActions.Collected<String> collected = NextActions.collect(() -> "正文");

    assertEquals("正文", collected.value());
    assertEquals(0, collected.suggestions().size());
  }

  @Test
  void collectsSuggestionsInOrder() {
    NextActions.Collected<String> collected =
        NextActions.collect(
            () -> {
              NextActions.suggest("翠竹林", "前往 翠竹林");
              NextActions.suggest("黑风岭", "前往 黑风岭");
              return "正文";
            });

    assertEquals("正文", collected.value());
    assertEquals(2, collected.suggestions().size());
    assertEquals("翠竹林", collected.suggestions().getFirst().label());
    assertEquals("前往 翠竹林", collected.suggestions().getFirst().command());
    assertEquals("前往 黑风岭", collected.suggestions().getLast().command());
  }

  @Test
  void capsSuggestionCount() {
    NextActions.Collected<String> collected =
        NextActions.collect(
            () -> {
              for (int i = 0; i < NextActions.MAX_SUGGESTIONS + 3; i++) {
                NextActions.suggest("地点" + i, "前往 地点" + i);
              }
              return "";
            });

    assertEquals(NextActions.MAX_SUGGESTIONS, collected.suggestions().size());
  }

  @Test
  void blankLabelFallsBackToCommand() {
    NextActions.Collected<String> collected =
        NextActions.collect(
            () -> {
              NextActions.suggest("  ", "前往 翠竹林");
              return "";
            });

    assertEquals("前往 翠竹林", collected.suggestions().getFirst().label());
  }
}
