package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * 命令遮蔽回归：每条命令的样例消息必须由它自己（而非其它模板）抢先命中。
 *
 * <p>历史上「锻造列表」曾被「锻造 {{input}}」遮蔽（字面量命令按字典序排在参数化命令之后）， 该测试用与 {@link CommandRegistry#matchOrder()}
 * 相同的顺序复现匹配，可系统性发现此类路由缺陷。
 */
class CommandShadowingTest {

  @Test
  void noCommandIsShadowedByAnotherTemplate() throws Exception {
    List<RegisteredCommand> commands = loadCommandsInMatchOrder();
    List<String> problems = new ArrayList<>();
    for (int index = 0; index < commands.size(); index++) {
      RegisteredCommand command = commands.get(index);
      String sample = sampleMessage(command.template());
      if (!command.template().matches(sample)) {
        problems.add(command.describe() + " 的样例「" + sample + "」无法匹配自身模板");
        continue;
      }
      for (int earlier = 0; earlier < index; earlier++) {
        RegisteredCommand candidate = commands.get(earlier);
        if (candidate.template().matches(sample)) {
          problems.add(
              command.describe() + " 的样例「" + sample + "」被 " + candidate.describe() + " 抢先命中");
          break;
        }
      }
    }
    if (!problems.isEmpty()) {
      fail(String.join("\n", problems));
    }
  }

  private static List<RegisteredCommand> loadCommandsInMatchOrder() throws Exception {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false) {
          @Override
          protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return true;
          }
        };
    scanner.addIncludeFilter(new AnnotationTypeFilter(CommandGroup.class));

    List<RegisteredCommand> commands = new ArrayList<>();
    for (BeanDefinition definition :
        scanner.findCandidateComponents("top.stillmisty.xiantao.handle.listener")) {
      Class<?> group = Class.forName(definition.getBeanClassName());
      for (Method method : group.getMethods()) {
        Command annotation = method.getAnnotation(Command.class);
        if (annotation == null) {
          continue;
        }
        commands.add(
            new RegisteredCommand(
                new Object(),
                method,
                CommandTemplate.compile(annotation.value()),
                false,
                false,
                List.of()));
      }
    }
    commands.sort(CommandRegistry.matchOrder());
    return commands;
  }

  /** 由模板生成一条可发送的样例消息（与 tools/e2e/commands.py 的生成规则一致）。 */
  private static String sampleMessage(CommandTemplate template) {
    String source = template.source();
    StringBuilder out = new StringBuilder();
    int index = 0;
    while (index < source.length()) {
      if (source.startsWith("{{", index)) {
        int end = source.indexOf("}}", index + 2);
        String inner = source.substring(index + 2, end);
        int comma = inner.indexOf(',');
        String regex = comma < 0 ? ".+" : inner.substring(comma + 1);
        out.append(regex.contains("\\d") ? "1" : "测试");
        index = end + 2;
        continue;
      }
      char current = source.charAt(index);
      if (current == '\\' && index + 1 < source.length() && source.charAt(index + 1) == 's') {
        out.append(' ');
        index += 2;
        if (index < source.length()
            && (source.charAt(index) == '*' || source.charAt(index) == '+')) {
          index++;
        }
        continue;
      }
      out.append(current);
      index++;
    }
    return out.toString().replaceAll("\\(\\?![^)]*\\)", "").replaceAll("\\s+", " ").trim();
  }
}
