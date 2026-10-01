package top.stillmisty.xiantao.config;

import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import top.stillmisty.qqgateway.QqBotClient;
import top.stillmisty.qqgateway.QqGatewayConfig;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.qqgateway.QqWebSocketGateway;
import top.stillmisty.qqgateway.QqWebhookHandler;
import top.stillmisty.xiantao.handle.dispatch.CommandDispatcher;

/** QQ 机器人装配：客户端 + 事件通道（WebSocket 或 Webhook）。凭证缺失时整体不装配。 */
@Configuration
@Conditional(QqBotConfiguration.QqConfiguredCondition.class)
public class QqBotConfiguration {

  @Bean(destroyMethod = "close")
  public QqBotClient qqBotClient(QqProperties properties) {
    return new QqBotClient(toGatewayConfig(properties));
  }

  /** 暴露消息发送能力：应用侧（PlatformHandler）通过该 bean 回复。 */
  @Bean
  public QqMessageSender qqMessageSender(QqBotClient client) {
    return client.sender();
  }

  @Bean(destroyMethod = "close")
  @ConditionalOnProperty(
      prefix = "xiantao.qq",
      name = "transport",
      havingValue = "websocket",
      matchIfMissing = true)
  public QqWebSocketGateway qqWebSocketGateway(QqBotClient client, CommandDispatcher dispatcher) {
    return new QqWebSocketGateway(client, dispatcher);
  }

  @Bean(destroyMethod = "close")
  @ConditionalOnProperty(prefix = "xiantao.qq", name = "transport", havingValue = "webhook")
  public QqWebhookHandler qqWebhookHandler(
      QqBotClient client, CommandDispatcher dispatcher, QqProperties properties) {
    return new QqWebhookHandler(client, dispatcher, properties.webhookMaxEventAge());
  }

  static QqGatewayConfig toGatewayConfig(QqProperties properties) {
    QqGatewayConfig config =
        Boolean.TRUE.equals(properties.sandbox())
            ? QqGatewayConfig.sandbox(properties.appId(), properties.clientSecret())
            : QqGatewayConfig.of(properties.appId(), properties.clientSecret());
    if (!properties.apiBaseUrl().isBlank()) {
      config = config.withApiBaseUri(URI.create(properties.apiBaseUrl()));
    }
    if (!properties.appBaseUrl().isBlank()) {
      config = config.withAppBaseUri(URI.create(properties.appBaseUrl()));
    }
    if (properties.intents() != QqGatewayConfig.INTENT_GROUP_AND_C2C) {
      config = config.withIntents(properties.intents());
    }
    if (properties.wsTokenMode() == QqGatewayConfig.WsTokenMode.APP_TICKET) {
      config = config.withWsTokenMode(properties.wsTokenMode(), properties.appToken());
    }
    return config;
  }

  /** 凭证齐备时才装配 QQ 机器人。 */
  public static final class QqConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      Environment environment = context.getEnvironment();
      if (!environment.getProperty("xiantao.qq.enabled", Boolean.class, Boolean.TRUE)) {
        return false;
      }
      return hasText(environment.getProperty("xiantao.qq.app-id"))
          && hasText(environment.getProperty("xiantao.qq.client-secret"));
    }

    private static boolean hasText(@Nullable String value) {
      return value != null && !value.isBlank();
    }
  }
}
