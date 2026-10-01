package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/** 对真实监听器上的全部命令模板做一致性回归。 */
class AllCommandTemplatesTest {

  @Test
  void everyListenerCommandTemplateCompilesAndIsUnique() throws Exception {
    List<Class<?>> groups = scanCommandGroups();
    assertEquals(22, groups.size(), "监听器命令组数量变化，请同步更新本测试的预期");

    Set<String> templates = new HashSet<>();
    List<String> argProblems = new ArrayList<>();
    int commandCount = 0;
    for (Class<?> group : groups) {
      for (Method method : group.getMethods()) {
        Command command = method.getAnnotation(Command.class);
        if (command == null) {
          continue;
        }
        commandCount++;
        assertTrue(
            templates.add(command.value()),
            "重复命令模板: " + command.value() + "（" + method.getName() + "）");

        CommandTemplate template = CommandTemplate.compile(command.value());
        for (Parameter parameter : method.getParameters()) {
          Arg arg = parameter.getAnnotation(Arg.class);
          if (arg != null && !template.argNames().contains(arg.value())) {
            argProblems.add(
                group.getSimpleName()
                    + "#"
                    + method.getName()
                    + " 的 @Arg("
                    + arg.value()
                    + ") 无对应占位符");
          }
        }
      }
    }

    assertEquals(71, commandCount, "命令数量变化，请确认迁移完整性");
    assertTrue(argProblems.isEmpty(), String.join("\n", argProblems));
  }

  @Test
  void templateSemanticsMatchKnownCommands() {
    CommandTemplate cultivation =
        CommandTemplate.compile("炼制\\s*{{itemName,\\S+}}\\s*{{quantity,\\d+}}");
    assertTrue(cultivation.matches("炼制 回气丹 2"));

    CommandTemplate useWithArgs = CommandTemplate.compile("使用\\s*{{itemName,\\S+}}\\s+{{args,.+}}");
    CommandTemplate usePlain = CommandTemplate.compile("使用\\s*{{itemName,\\S+}}");
    assertTrue(useWithArgs.matches("使用 回气丹 张三"));
    assertFalse(usePlain.matches("使用 回气丹 张三"), "带参数的用法不应命中无参模板");
    assertTrue(usePlain.matches("使用 回气丹"));
    assertFalse(useWithArgs.matches("使用 回气丹"));
  }

  private static List<Class<?>> scanCommandGroups() throws ClassNotFoundException {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false) {
          @Override
          protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return true;
          }
        };
    scanner.addIncludeFilter(new AnnotationTypeFilter(CommandGroup.class));
    List<Class<?>> groups = new ArrayList<>();
    for (BeanDefinition definition :
        scanner.findCandidateComponents("top.stillmisty.xiantao.handle.listener")) {
      groups.add(Class.forName(definition.getBeanClassName()));
    }
    return groups;
  }
}
