package top.stillmisty.xiantao.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqWebSocketGateway;

/** QQ 机器人启动编排与状态日志。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QqGatewayLifecycle {

  private final QqProperties properties;
  private final ObjectProvider<QqWebSocketGateway> webSocketGatewayProvider;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    if (!properties.configured()) {
      log.warn("QQ 机器人未启用：请在 xiantao.qq.app-id / xiantao.qq.client-secret 配置平台凭证");
      return;
    }
    if (properties.transport() == QqProperties.Transport.WEBSOCKET) {
      webSocketGatewayProvider.ifAvailable(QqWebSocketGateway::start);
      log.info("QQ 机器人已启用（WebSocket 长连接）");
    } else {
      log.info("QQ 机器人已启用（Webhook 回调），路径 {}", properties.webhookPath());
    }
  }
}
