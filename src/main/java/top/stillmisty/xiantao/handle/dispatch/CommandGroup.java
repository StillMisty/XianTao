package top.stillmisty.xiantao.handle.dispatch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 命令组标记：标注在监听器类上，供 {@link CommandRegistry} 扫描其 {@link Command} 方法。 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandGroup {}
