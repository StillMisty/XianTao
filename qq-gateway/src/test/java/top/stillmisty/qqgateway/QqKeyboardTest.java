package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QqKeyboardTest {

  @Test
  void commandGridSplitsFivePerRow() {
    List<QqButton> buttons = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      buttons.add(QqButton.command("btn-" + i, "按钮" + i, "选 " + i));
    }

    QqKeyboard keyboard = QqKeyboard.commandGrid(buttons);

    assertEquals(2, keyboard.rows().size());
    assertEquals(5, keyboard.rows().get(0).buttons().size());
    assertEquals(2, keyboard.rows().get(1).buttons().size());
    assertEquals(7, keyboard.buttonCount());
  }

  @Test
  void commandButtonDefaults() {
    QqButton button = QqButton.command("choice-0", "进入洞穴", "选 A");

    assertEquals(QqButton.ACTION_COMMAND, button.actionType());
    assertEquals(QqButton.STYLE_BLUE, button.style());
    assertEquals("进入洞穴", button.label());
    assertEquals("进入洞穴", button.visitedLabel());
    assertTrue(button.enter());
    assertEquals(null, button.specifyUserIds());
    assertTrue(button.unsupportTips().contains("选 A"));
  }

  @Test
  void rejectsTooManyRowsOrButtons() {
    List<QqKeyboard.QqButtonRow> sixRows = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      sixRows.add(new QqKeyboard.QqButtonRow(List.of(QqButton.command("b-" + i, "按钮", "选"))));
    }
    assertThrows(IllegalArgumentException.class, () -> new QqKeyboard(sixRows));

    List<QqButton> tooMany = new ArrayList<>();
    for (int i = 0; i < 26; i++) {
      tooMany.add(QqButton.command("b-" + i, "按钮", "选"));
    }
    assertThrows(IllegalArgumentException.class, () -> QqKeyboard.commandGrid(tooMany));

    List<QqButton> sixInRow = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      sixInRow.add(QqButton.command("b-" + i, "按钮", "选"));
    }
    assertThrows(IllegalArgumentException.class, () -> new QqKeyboard.QqButtonRow(sixInRow));
  }

  @Test
  void rejectsDuplicateButtonIds() {
    QqKeyboard.QqButtonRow row =
        new QqKeyboard.QqButtonRow(
            List.of(QqButton.command("dup", "一", "选 A"), QqButton.command("dup", "二", "选 B")));
    assertThrows(IllegalArgumentException.class, () -> new QqKeyboard(List.of(row)));
  }

  @Test
  void rejectsInvalidButtonFields() {
    assertThrows(IllegalArgumentException.class, () -> QqButton.command("id", " ", "选 A"));
    assertThrows(IllegalArgumentException.class, () -> QqButton.command("id", "按钮", " "));
    assertThrows(
        IllegalArgumentException.class,
        () -> new QqButton("id", "按钮", "按钮", 9, QqButton.ACTION_COMMAND, "选 A", null, true, "提示"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new QqButton("id", "按钮", "按钮", 0, 9, "选 A", null, true, "提示"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new QqButton(
                "id", "按钮", "按钮", 0, QqButton.ACTION_COMMAND, "选 A", List.of(), true, "提示"));
  }
}
