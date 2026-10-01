package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.interceptor.RequireGm;

class CommandRegistryTest {

  @CommandGroup
  static class ValidCommands {

    @Command("回答\\s*{{value}}")
    @RequireAuth
    public void withAuth(QqIncomingMessage message, @Arg("value") String value) {}

    @Command("开火")
    public void noAuth(QqIncomingMessage message) {}

    @Command("管理")
    @RequireAuth
    @RequireGm
    public void gm(QqIncomingMessage message) {}
  }

  @CommandGroup
  static class NonVoidCommands {
    @Command("坏命令")
    public String bad(QqIncomingMessage message) {
      return "x";
    }
  }

  @CommandGroup
  static class BadParameterCommands {
    @Command("坏参数")
    public void bad(QqIncomingMessage message, Integer value) {}
  }

  @CommandGroup
  static class UnknownArgCommands {
    @Command("未知参数")
    public void bad(QqIncomingMessage message, @Arg("missing") String value) {}
  }

  @CommandGroup
  static class GmWithoutAuthCommands {
    @Command("缺认证")
    @RequireGm
    public void bad(QqIncomingMessage message) {}
  }

  @CommandGroup
  static class DuplicateCommands {
    @Command("重复")
    public void first(QqIncomingMessage message) {}

    @Command("重复")
    public void second(QqIncomingMessage message) {}
  }

  @CommandGroup
  static class UnorderedCommands {
    @Command("b命令")
    public void second(QqIncomingMessage message) {}

    @Command("a命令")
    public void first(QqIncomingMessage message) {}
  }

  @Test
  void matchOrderIsDeterministic() {
    CommandRegistry registry = registryOf(new UnorderedCommands());
    assertEquals(
        java.util.List.of("a命令", "b命令"),
        registry.commands().stream().map(command -> command.template().source()).toList());
  }

  @Test
  void registersCommandsWithInterceptorFlags() {
    CommandRegistry registry = registryOf(new ValidCommands());

    assertEquals(3, registry.size());
    RegisteredCommand withAuth =
        registry.commands().stream()
            .filter(command -> command.template().source().equals("回答\\s*{{value}}"))
            .findFirst()
            .orElseThrow();
    assertTrue(withAuth.requireAuth());
    assertTrue(!withAuth.requireGm());

    RegisteredCommand gm =
        registry.commands().stream()
            .filter(command -> command.template().source().equals("管理"))
            .findFirst()
            .orElseThrow();
    assertTrue(gm.requireAuth());
    assertTrue(gm.requireGm());

    RegisteredCommand noAuth =
        registry.commands().stream()
            .filter(command -> command.template().source().equals("开火"))
            .findFirst()
            .orElseThrow();
    assertTrue(!noAuth.requireAuth());
  }

  @Test
  void rejectsInvalidDeclarations() {
    assertThrows(IllegalStateException.class, () -> registryOf(new NonVoidCommands()));
    assertThrows(IllegalStateException.class, () -> registryOf(new BadParameterCommands()));
    assertThrows(IllegalStateException.class, () -> registryOf(new UnknownArgCommands()));
    assertThrows(IllegalStateException.class, () -> registryOf(new GmWithoutAuthCommands()));
    assertThrows(IllegalStateException.class, () -> registryOf(new DuplicateCommands()));
  }

  private static CommandRegistry registryOf(Object bean) {
    GenericApplicationContext context = new GenericApplicationContext();
    register(context, bean.getClass());
    context.refresh();
    try {
      return new CommandRegistry(context);
    } finally {
      context.close();
    }
  }

  @SuppressWarnings("unchecked")
  private static void register(GenericApplicationContext context, Class<?> type) {
    context.registerBean((Class<Object>) type);
  }
}
