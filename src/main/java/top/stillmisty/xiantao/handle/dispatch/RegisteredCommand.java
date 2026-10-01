package top.stillmisty.xiantao.handle.dispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import top.stillmisty.qqgateway.QqIncomingMessage;

/** 已注册的命令：bean + 方法 + 模板 + 拦截语义。 */
public final class RegisteredCommand {

  /** 参数绑定：{@code argName} 为 null 表示注入原始消息对象。 */
  record ParameterBinding(@Nullable String argName) {}

  private final Object bean;
  private final Method method;
  private final CommandTemplate template;
  private final boolean requireAuth;
  private final boolean requireGm;
  private final List<ParameterBinding> bindings;

  RegisteredCommand(
      Object bean,
      Method method,
      CommandTemplate template,
      boolean requireAuth,
      boolean requireGm,
      List<ParameterBinding> bindings) {
    this.bean = bean;
    this.method = method;
    this.template = template;
    this.requireAuth = requireAuth;
    this.requireGm = requireGm;
    this.bindings = List.copyOf(bindings);
  }

  /** 创建参数绑定（供注册表构造方法参数）。 */
  static ParameterBinding messageBinding() {
    return new ParameterBinding(null);
  }

  /** 创建参数绑定。 */
  static ParameterBinding argBinding(String argName) {
    return new ParameterBinding(argName);
  }

  public CommandTemplate template() {
    return template;
  }

  public boolean requireAuth() {
    return requireAuth;
  }

  public boolean requireGm() {
    return requireGm;
  }

  /** 调用命令方法。 */
  public void invoke(QqIncomingMessage message, Map<String, String> args)
      throws InvocationTargetException, IllegalAccessException {
    Object[] parameters = new Object[bindings.size()];
    for (int i = 0; i < bindings.size(); i++) {
      ParameterBinding binding = bindings.get(i);
      parameters[i] =
          binding.argName() == null ? message : args.getOrDefault(binding.argName(), "");
    }
    method.invoke(bean, parameters);
  }

  /** 日志用描述。 */
  public String describe() {
    return method.getDeclaringClass().getSimpleName()
        + "#"
        + method.getName()
        + " "
        + template.source();
  }
}
