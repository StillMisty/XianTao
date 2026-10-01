package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.util.TextFormat;

class NotificationAppenderTest {

  @Test
  void buildsChoiceButtonsFromPendingChoiceEvent() {
    GameEventService events = mock(GameEventService.class);
    when(events.findUndelivered(42L)).thenReturn(List.of(choiceEvent(2)));
    NotificationAppender appender =
        new NotificationAppender(events, mock(AuthenticationService.class));

    NotificationAppender.AppendResult result = appender.prepareAppend(42L, "状态", TextFormat.get());

    assertNotNull(result.keyboard());
    assertEquals(2, result.keyboard().buttonCount());
    var button = result.keyboard().rows().getFirst().buttons().getFirst();
    assertEquals("选项0", button.label());
    assertEquals("选 A", button.data());
    assertTrue(result.text().contains("请选择"));
    assertTrue(result.text().contains("选项0"), "文本选项作为兜底保留");
    assertTrue(result.eventIds().isEmpty(), "选择事件保持未投递状态");
  }

  @Test
  void capsButtonsAtPlatformLimit() {
    GameEventService events = mock(GameEventService.class);
    when(events.findUndelivered(42L)).thenReturn(List.of(choiceEvent(26)));
    NotificationAppender appender =
        new NotificationAppender(events, mock(AuthenticationService.class));

    NotificationAppender.AppendResult result = appender.prepareAppend(42L, "状态", TextFormat.get());

    assertNotNull(result.keyboard());
    assertEquals(QqKeyboard.MAX_BUTTONS, result.keyboard().buttonCount());
    assertTrue(result.text().contains("选项25"), "超出按钮上限的选项仍保留文本形式");
  }

  @Test
  void noKeyboardWithoutChoiceEvent() {
    GameEventService events = mock(GameEventService.class);
    when(events.findUndelivered(42L))
        .thenReturn(List.of(GameEvent.create(42L, GameEventCategory.HP_RECOVERED)));
    NotificationAppender appender =
        new NotificationAppender(events, mock(AuthenticationService.class));

    NotificationAppender.AppendResult result = appender.prepareAppend(42L, "状态", TextFormat.get());

    assertNull(result.keyboard());
    assertTrue(result.text().contains("气血"));
  }

  @Test
  void skipsOptionsWithoutKey() {
    GameEvent choice =
        GameEvent.create(42L, GameEventCategory.TRAVEL_EVENT)
            .withEffectData(
                new EffectData.ChoiceOptions(
                    new EffectData.ChoiceData(
                        List.of(
                            new EffectData.Option(null, "无键选项", null),
                            new EffectData.Option("B", "有键选项", null)))));
    GameEventService events = mock(GameEventService.class);
    when(events.findUndelivered(42L)).thenReturn(List.of(choice));
    NotificationAppender appender =
        new NotificationAppender(events, mock(AuthenticationService.class));

    NotificationAppender.AppendResult result = appender.prepareAppend(42L, "状态", TextFormat.get());

    assertNotNull(result.keyboard());
    assertEquals(1, result.keyboard().buttonCount());
    assertEquals("选 B", result.keyboard().rows().getFirst().buttons().getFirst().data());
  }

  private static GameEvent choiceEvent(int optionCount) {
    List<EffectData.Option> options = new ArrayList<>();
    for (int i = 0; i < optionCount; i++) {
      options.add(new EffectData.Option(String.valueOf((char) ('A' + i)), "选项" + i, null));
    }
    return GameEvent.create(42L, GameEventCategory.TRAVEL_EVENT)
        .withEffectData(new EffectData.ChoiceOptions(new EffectData.ChoiceData(options)));
  }
}
