package top.stillmisty.xiantao.handle.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import top.stillmisty.qqgateway.QqWebhookHandler;
import top.stillmisty.xiantao.config.QqBotConfiguration;
import top.stillmisty.xiantao.config.QqProperties;

/**
 * QQ Webhook 回调端点。
 *
 * <p>平台要求 HTTPS 公网地址与 80/443/8080/8443 端口；端点在 3 秒内返回 op 12 回执，事件在虚拟线程中异步处理。
 */
@RestController
@ConditionalOnProperty(prefix = "xiantao.qq", name = "transport", havingValue = "webhook")
@Conditional(QqBotConfiguration.QqConfiguredCondition.class)
@Slf4j
public class QqWebhookController {

  private final QqWebhookHandler handler;
  private final String path;

  public QqWebhookController(QqWebhookHandler handler, QqProperties properties) {
    this.handler = handler;
    this.path = properties.webhookPath();
  }

  /** 接收平台事件推送。 */
  @PostMapping(
      path = "${xiantao.qq.webhook-path:/qq/webhook}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> handle(
      @RequestBody byte[] body,
      @RequestHeader(value = "X-Signature-Ed25519", required = false) String signature) {
    QqWebhookHandler.WebhookResult result = handler.handle(body, signature);
    if (result.status() != 200) {
      log.warn("QQ Webhook 请求被拒绝: status={}, path={}", result.status(), path);
    }
    return ResponseEntity.status(result.status())
        .contentType(MediaType.APPLICATION_JSON)
        .body(result.body());
  }
}
