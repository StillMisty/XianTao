package top.stillmisty.xiantao.handle.dispatch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令方法标记。
 *
 * <p>模板语法与 SimBot 的 {@code @Filter} 保持一致：
 *
 * <ul>
 *   <li>{@code {{name}}} 等价于 {@code (?<name>.+)}（贪婪）
 *   <li>{@code {{name,正则}}} 使用自定义正则，例如 {@code {{itemName,\S+}}}
 *   <li>不含 {@code {{}}} 的模板按字面量全匹配；其余部分按原样作为正则
 * </ul>
 *
 * <p>匹配为全匹配（{@code Matcher.matches()}），消息在匹配前会先做 trim。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

  /** 命令模板。 */
  String value();
}
