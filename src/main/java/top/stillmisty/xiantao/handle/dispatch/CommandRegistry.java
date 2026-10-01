package top.stillmisty.xiantao.handle.dispatch;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.interceptor.RequireGm;

/**
 * 命令注册表：启动时扫描所有 {@link CommandGroup} bean 的 {@link Command} 方法， 编译模板并校验参数绑定；重复模板或非法签名直接启动失败。
 *
 * <p>匹配顺序按模板字典序固定，与 JVM 无关。
 */
@Component
@Slf4j
public class CommandRegistry {

  private final List<RegisteredCommand> commands;

  public CommandRegistry(ApplicationContext context) {
    List<RegisteredCommand> discovered = new ArrayList<>();
    Set<String> templates = new HashSet<>();
    Map<String, Object> groups = context.getBeansWithAnnotation(CommandGroup.class);
    for (Object bean : groups.values()) {
      Class<?> type = AopUtils.getTargetClass(bean);
      int registeredForBean = 0;
      for (Method method : type.getMethods()) {
        Command command = method.getAnnotation(Command.class);
        if (command == null) {
          continue;
        }
        if (!templates.add(command.value())) {
          throw new IllegalStateException(
              "重复的命令模板: " + command.value() + "（" + describe(type, method) + "）");
        }
        discovered.add(build(bean, method, command));
        registeredForBean++;
      }
      if (registeredForBean == 0) {
        throw new IllegalStateException(
            "@CommandGroup 没有任何 public @Command 方法: " + type.getSimpleName());
      }
    }
    if (discovered.isEmpty()) {
      log.warn("未发现任何 @Command 命令，机器人将不会响应任何指令");
    } else {
      discovered.sort(matchOrder());
      log.info("命令注册完成: {} 条，来自 {} 个命令组", discovered.size(), groups.size());
    }
    this.commands = List.copyOf(discovered);
  }

  /** 已注册命令（按匹配顺序）。 */
  public List<RegisteredCommand> commands() {
    return commands;
  }

  /**
   * 匹配顺序：无占位符的字面量命令优先于参数化命令（避免「锻造列表」被「锻造 {{input}}」遮蔽），同级按模板字典序。
   *
   * <p>{@code Class#getMethods} 顺序未定义，显式排序保证跨 JVM 行为一致；{@code CommandShadowingTest} 会校验
   * 每条命令的样例消息不会被其它模板抢先命中。
   */
  static Comparator<RegisteredCommand> matchOrder() {
    return Comparator.comparingInt(
            (RegisteredCommand command) -> command.template().argNames().isEmpty() ? 0 : 1)
        .thenComparing(command -> command.template().source());
  }

  public int size() {
    return commands.size();
  }

  private static RegisteredCommand build(Object bean, Method method, Command command) {
    if (!Void.TYPE.equals(method.getReturnType())) {
      throw new IllegalStateException("@Command 方法必须返回 void: " + describe(method));
    }
    CommandTemplate template = CommandTemplate.compile(command.value());
    List<RegisteredCommand.ParameterBinding> bindings = new ArrayList<>();
    for (Parameter parameter : method.getParameters()) {
      Arg arg = parameter.getAnnotation(Arg.class);
      if (arg != null) {
        if (!String.class.equals(parameter.getType())) {
          throw new IllegalStateException("@Arg 参数必须是 String: " + describe(method));
        }
        if (!template.argNames().contains(arg.value())) {
          throw new IllegalStateException("模板中没有参数 '" + arg.value() + "': " + describe(method));
        }
        bindings.add(RegisteredCommand.argBinding(arg.value()));
      } else if (QqIncomingMessage.class.equals(parameter.getType())) {
        bindings.add(RegisteredCommand.messageBinding());
      } else {
        throw new IllegalStateException(
            "不支持的参数类型 " + parameter.getType().getName() + ": " + describe(method));
      }
    }

    Class<?> declaring = method.getDeclaringClass();
    boolean requireAuth =
        method.isAnnotationPresent(RequireAuth.class)
            || declaring.isAnnotationPresent(RequireAuth.class);
    boolean requireGm =
        method.isAnnotationPresent(RequireGm.class)
            || declaring.isAnnotationPresent(RequireGm.class);
    if (requireGm && !requireAuth) {
      throw new IllegalStateException("@RequireGm 必须与 @RequireAuth 一起使用: " + describe(method));
    }
    return new RegisteredCommand(bean, method, template, requireAuth, requireGm, bindings);
  }

  private static String describe(Class<?> type, Method method) {
    return type.getSimpleName() + "#" + method.getName();
  }

  private static String describe(Method method) {
    return describe(method.getDeclaringClass(), method);
  }
}
