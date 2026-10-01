package top.stillmisty.xiantao.handle.dispatch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 命令方法参数绑定：从模板的命名捕获组取值。 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {

  /** 捕获组名（对应模板中的 {@code {{name}}}）。 */
  String value();
}
