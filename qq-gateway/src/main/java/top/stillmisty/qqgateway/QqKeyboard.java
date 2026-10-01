package top.stillmisty.qqgateway;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * QQ 消息按钮键盘（自定义按钮）。
 *
 * <p>平台限制：最多 5 行，每行最多 5 个按钮；按钮 ID 在同一键盘内唯一。
 *
 * <p>自定义按钮已全量开放（与 Markdown 一致）；若平台因兼容性或权限原因拒绝携带 keyboard 的请求， {@link QqMessageSender} 会降级为不带按钮重发。
 */
public record QqKeyboard(List<QqButtonRow> rows) {

  public static final int MAX_ROWS = 5;
  public static final int MAX_BUTTONS_PER_ROW = 5;
  public static final int MAX_BUTTONS = MAX_ROWS * MAX_BUTTONS_PER_ROW;

  public QqKeyboard {
    Objects.requireNonNull(rows, "rows");
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("按钮键盘至少需要一行");
    }
    if (rows.size() > MAX_ROWS) {
      throw new IllegalArgumentException("按钮键盘最多 " + MAX_ROWS + " 行，实际 " + rows.size());
    }
    Set<String> ids = new HashSet<>();
    for (QqButtonRow row : rows) {
      for (QqButton button : row.buttons()) {
        if (!ids.add(button.id())) {
          throw new IllegalArgumentException("按钮 ID 在同一键盘内必须唯一: " + button.id());
        }
      }
    }
  }

  /** 单行按钮。 */
  public record QqButtonRow(List<QqButton> buttons) {

    public QqButtonRow {
      Objects.requireNonNull(buttons, "buttons");
      if (buttons.isEmpty()) {
        throw new IllegalArgumentException("按钮行不能为空");
      }
      if (buttons.size() > MAX_BUTTONS_PER_ROW) {
        throw new IllegalArgumentException(
            "每行最多 " + MAX_BUTTONS_PER_ROW + " 个按钮，实际 " + buttons.size());
      }
    }
  }

  /** 按每行 {@value #MAX_BUTTONS_PER_ROW} 个自动分行。 */
  public static QqKeyboard commandGrid(List<QqButton> buttons) {
    Objects.requireNonNull(buttons, "buttons");
    if (buttons.isEmpty()) {
      throw new IllegalArgumentException("按钮列表不能为空");
    }
    if (buttons.size() > MAX_BUTTONS) {
      throw new IllegalArgumentException("按钮总数最多 " + MAX_BUTTONS + "，实际 " + buttons.size());
    }
    List<QqButtonRow> rows = new ArrayList<>();
    for (int index = 0; index < buttons.size(); index += MAX_BUTTONS_PER_ROW) {
      rows.add(
          new QqButtonRow(
              List.copyOf(
                  buttons.subList(index, Math.min(index + MAX_BUTTONS_PER_ROW, buttons.size())))));
    }
    return new QqKeyboard(rows);
  }

  /** 按钮总数。 */
  public int buttonCount() {
    return rows.stream().mapToInt(row -> row.buttons().size()).sum();
  }
}
