package top.stillmisty.xiantao.handle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 下一步操作建议：命令处理器在生成回复时登记快捷按钮（点击即发送对应指令文本），由 ReplyHelper 统一附加。
 *
 * <p>刻意保持克制——只在「下一步很明确」的场景登记（如地图 → 可前往的地区），不逐条命令堆按钮。
 */
public final class NextActions {

  /** 一条建议：label 为按钮文字，command 为点击后发送的指令文本。 */
  public record Suggestion(String label, String command) {}

  /** 与 QQ 键盘单条消息上限一致（5×5）；建议实际保持 5 条以内。 */
  public static final int MAX_SUGGESTIONS = 5;

  private static final ScopedValue<List<Suggestion>> CURRENT = ScopedValue.newInstance();

  private NextActions() {}

  /** 登记一条建议；无收集上下文（如单测直接调用处理器）时静默忽略。 */
  public static void suggest(String label, String command) {
    if (!CURRENT.isBound()) {
      return;
    }
    List<Suggestion> list = CURRENT.get();
    if (list.size() >= MAX_SUGGESTIONS) {
      return;
    }
    String commandText = command.strip();
    String labelText = label.isBlank() ? commandText : label.strip();
    list.add(new Suggestion(labelText, commandText));
  }

  /** 在收集上下文中执行动作，返回动作结果与收集到的建议。 */
  public static <T> Collected<T> collect(Supplier<T> action) {
    List<Suggestion> list = new ArrayList<>();
    T value = ScopedValue.where(CURRENT, list).call(action::get);
    return new Collected<>(value, List.copyOf(list));
  }

  /** 收集结果：动作返回值 + 收集到的建议。 */
  public record Collected<T>(T value, List<Suggestion> suggestions) {}
}
