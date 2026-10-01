package top.stillmisty.xiantao.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.stillmisty.qqgateway.QqGatewayConfig;
import top.stillmisty.qqgateway.QqWebhookHandler;

/** QQ 机器人接入配置（xiantao.qq.*）。 */
@ConfigurationProperties(prefix = "xiantao.qq")
public record QqProperties(
    Boolean enabled,
    String appId,
    String clientSecret,
    String appToken,
    QqGatewayConfig.WsTokenMode wsTokenMode,
    Transport transport,
    String apiBaseUrl,
    String appBaseUrl,
    Integer intents,
    String webhookPath,
    Duration webhookMaxEventAge,
    Boolean choiceButtons,
    Boolean suggestionButtons,
    Boolean sandbox) {

  /** 事件通道。 */
  public enum Transport {
    WEBSOCKET,
    WEBHOOK
  }

  public QqProperties {
    enabled = enabled == null ? Boolean.TRUE : enabled;
    appId = appId == null ? "" : appId.trim();
    clientSecret = clientSecret == null ? "" : clientSecret.trim();
    appToken = appToken == null ? "" : appToken.trim();
    wsTokenMode = wsTokenMode == null ? QqGatewayConfig.WsTokenMode.ACCESS_TOKEN : wsTokenMode;
    transport = transport == null ? Transport.WEBSOCKET : transport;
    apiBaseUrl = apiBaseUrl == null ? "" : apiBaseUrl.trim();
    appBaseUrl = appBaseUrl == null ? "" : appBaseUrl.trim();
    intents = intents == null ? QqGatewayConfig.INTENT_GROUP_AND_C2C : intents;
    webhookPath = webhookPath == null || webhookPath.isBlank() ? "/qq/webhook" : webhookPath.trim();
    webhookMaxEventAge =
        webhookMaxEventAge == null ? QqWebhookHandler.DEFAULT_MAX_EVENT_AGE : webhookMaxEventAge;
    choiceButtons = choiceButtons == null ? Boolean.TRUE : choiceButtons;
    suggestionButtons = suggestionButtons == null ? Boolean.TRUE : suggestionButtons;
    sandbox = sandbox == null ? Boolean.FALSE : sandbox;
  }

  /** 凭证是否齐备且已启用。 */
  public boolean configured() {
    return Boolean.TRUE.equals(enabled) && !appId.isBlank() && !clientSecret.isBlank();
  }
}
