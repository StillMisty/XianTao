package top.stillmisty.xiantao.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import top.stillmisty.qqgateway.QqBotClient;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.qqgateway.QqWebSocketGateway;
import top.stillmisty.qqgateway.QqWebhookHandler;
import top.stillmisty.xiantao.handle.dispatch.CommandDispatcher;

/** QQ 机器人条件装配回归。 */
class QqBotConfigurationTest {

  @Test
  void withoutCredentialsOnlyPropertiesAndLifecycleExist() {
    try (AnnotationConfigApplicationContext context = context(Map.of())) {
      assertEquals(1, context.getBeanNamesForType(QqProperties.class).length);
      assertEquals(1, context.getBeanNamesForType(QqGatewayLifecycle.class).length);
      assertEquals(0, context.getBeanNamesForType(QqBotClient.class).length);
      assertEquals(0, context.getBeanNamesForType(QqMessageSender.class).length);
      assertEquals(0, context.getBeanNamesForType(QqWebSocketGateway.class).length);
      assertEquals(0, context.getBeanNamesForType(QqWebhookHandler.class).length);
    }
  }

  @Test
  void websocketTransportIsDefault() {
    try (AnnotationConfigApplicationContext context =
        context(
            Map.of(
                "xiantao.qq.app-id", "123456",
                "xiantao.qq.client-secret", "secret"))) {
      assertEquals(1, context.getBeanNamesForType(QqBotClient.class).length);
      assertEquals(1, context.getBeanNamesForType(QqMessageSender.class).length);
      assertEquals(1, context.getBeanNamesForType(QqWebSocketGateway.class).length);
      assertEquals(0, context.getBeanNamesForType(QqWebhookHandler.class).length);
    }
  }

  @Test
  void webhookTransportCreatesHandlerOnly() {
    try (AnnotationConfigApplicationContext context =
        context(
            Map.of(
                "xiantao.qq.app-id", "123456",
                "xiantao.qq.client-secret", "secret",
                "xiantao.qq.transport", "webhook"))) {
      assertEquals(1, context.getBeanNamesForType(QqBotClient.class).length);
      assertEquals(1, context.getBeanNamesForType(QqWebhookHandler.class).length);
      assertEquals(0, context.getBeanNamesForType(QqWebSocketGateway.class).length);
    }
  }

  @Test
  void disabledSkipsEverything() {
    try (AnnotationConfigApplicationContext context =
        context(
            Map.of(
                "xiantao.qq.enabled", "false",
                "xiantao.qq.app-id", "123456",
                "xiantao.qq.client-secret", "secret"))) {
      assertEquals(0, context.getBeanNamesForType(QqBotClient.class).length);
    }
  }

  private static AnnotationConfigApplicationContext context(Map<String, Object> properties) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context
        .getEnvironment()
        .getPropertySources()
        .addFirst(new MapPropertySource("test", new HashMap<>(properties)));
    context.register(QqPropertiesConfiguration.class, QqBotConfiguration.class);
    context.register(QqGatewayLifecycle.class);
    context.registerBean(CommandDispatcher.class, () -> mock(CommandDispatcher.class));
    context.refresh();
    return context;
  }
}
