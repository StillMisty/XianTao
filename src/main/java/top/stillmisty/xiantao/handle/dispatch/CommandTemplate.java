package top.stillmisty.xiantao.handle.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令模板编译器。
 *
 * <p>与 SimBot quantcat 语义逐条对齐（已对现有 71 个模板核验）：
 *
 * <ul>
 *   <li>{@code {{name}}} → {@code (?<name>.+)}
 *   <li>{@code {{name,正则}}} → {@code (?<name>正则)}，参数内允许嵌套花括号
 *   <li>模板不含 {@code {{}}} 时整体按字面量（{@link Pattern#quote}）
 *   <li>模板含 {@code {{}}} 时，占位符之外的文本按原样作为正则
 *   <li>匹配使用 {@link Matcher#matches()}（全匹配）
 * </ul>
 */
public final class CommandTemplate {

  private static final Pattern VALID_GROUP_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
  private static final String DEFAULT_VALUE_PATTERN = ".+";

  private final String source;
  private final Pattern pattern;
  private final List<String> argNames;
  private final String barePrefix;

  private CommandTemplate(String source, Pattern pattern, List<String> argNames) {
    this.source = source;
    this.pattern = pattern;
    this.argNames = argNames;
    this.barePrefix = computeBarePrefix(source);
  }

  /** 编译命令模板。 */
  public static CommandTemplate compile(String template) {
    Objects.requireNonNull(template, "template");
    if (template.isBlank()) {
      throw new IllegalArgumentException("命令模板不能为空");
    }
    if (!template.contains("{{")) {
      return new CommandTemplate(template, Pattern.compile(Pattern.quote(template)), List.of());
    }

    StringBuilder regex = new StringBuilder(template.length() + 16);
    List<String> names = new ArrayList<>();
    int index = 0;
    while (index < template.length()) {
      char current = template.charAt(index);
      if (current == '{' && index + 1 < template.length() && template.charAt(index + 1) == '{') {
        int close = findPlaceholderEnd(template, index + 2);
        if (close < 0) {
          throw new IllegalArgumentException("命令模板缺少 '}}' 结束符: " + template);
        }
        String inner = template.substring(index + 2, close);
        int comma = inner.indexOf(',');
        String name = (comma < 0 ? inner : inner.substring(0, comma)).trim();
        String valuePattern = comma < 0 ? DEFAULT_VALUE_PATTERN : inner.substring(comma + 1).trim();
        if (!VALID_GROUP_NAME.matcher(name).matches()) {
          throw new IllegalArgumentException("非法参数名 '" + name + "': " + template);
        }
        if (valuePattern.isEmpty()) {
          throw new IllegalArgumentException("参数 '" + name + "' 缺少正则: " + template);
        }
        if (names.contains(name)) {
          throw new IllegalArgumentException("重复参数名 '" + name + "': " + template);
        }
        names.add(name);
        regex.append("(?<").append(name).append('>').append(valuePattern).append(')');
        index = close + 2;
        continue;
      }
      regex.append(current);
      index++;
    }
    return new CommandTemplate(template, Pattern.compile(regex.toString()), List.copyOf(names));
  }

  /** 找到占位符结束的 {@code }} 位置（支持参数正则内的嵌套花括号）。 */
  private static int findPlaceholderEnd(String template, int start) {
    int depth = 0;
    for (int i = start; i < template.length(); i++) {
      char current = template.charAt(i);
      if (current == '{') {
        depth++;
      } else if (current == '}') {
        if (depth > 0) {
          depth--;
        } else if (i + 1 < template.length() && template.charAt(i + 1) == '}') {
          return i;
        }
      }
    }
    return -1;
  }

  /** 原始模板。 */
  public String source() {
    return source;
  }

  /** 是否全匹配。 */
  public boolean matches(String text) {
    return pattern.matcher(text).matches();
  }

  /** 捕获组名列表（按声明顺序）。 */
  public List<String> argNames() {
    return argNames;
  }

  /**
   * 字面前缀：第一个占位符之前的命令名（如 {@code 前往\s*{{mapName}}} → {@code 前往}）。
   *
   * <p>纯字面量命令返回空串（它们能直接匹配，不需要缺参兜底）。
   */
  public String barePrefix() {
    return barePrefix;
  }

  /** 从模板源码提取字面前缀，去掉结尾的正则修饰（\s*、\s+、负向前瞻）。 */
  private static String computeBarePrefix(String template) {
    int placeholder = template.indexOf("{{");
    if (placeholder < 0) {
      return "";
    }
    String prefix = template.substring(0, placeholder);
    prefix = prefix.replaceAll("\\\\s[*+]$", "");
    prefix = prefix.replaceAll("\\(\\?![^)]*\\)$", "");
    return prefix.strip();
  }

  /**
   * 提取参数值。
   *
   * @param text 已确认匹配的文本
   * @return 参数名 → 匹配值（未参与匹配的组为空串）
   */
  public java.util.Map<String, String> extract(String text) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("文本与模板不匹配: " + source);
    }
    java.util.Map<String, String> args = new java.util.HashMap<>(argNames.size() * 2);
    for (String name : argNames) {
      String value = matcher.group(name);
      args.put(name, value == null ? "" : value);
    }
    return args;
  }
}
