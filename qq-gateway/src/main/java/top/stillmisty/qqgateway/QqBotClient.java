package top.stillmisty.qqgateway;

import tools.jackson.databind.ObjectMapper;

/**
 * QQ 机器人客户端：Token 管理 + OpenAPI 调用 + 消息发送。
 *
 * <p>生命周期与整个应用一致；WebSocket 长连接见 {@link QqWebSocketGateway}，Webhook 接入见 {@link
 * QqWebhookHandler}。二者共用本客户端的 Token 与发送能力。
 *
 * <p>本类线程安全，可在任意线程（含虚拟线程）调用。
 */
public final class QqBotClient implements AutoCloseable {

  private final QqGatewayConfig config;
  private final ObjectMapper mapper;
  private final QqHttpApi api;
  private final AccessTokenManager tokens;
  private final QqMessageSender sender;

  public QqBotClient(QqGatewayConfig config) {
    this.config = config;
    this.mapper = new ObjectMapper();
    this.api = new QqHttpApi(config, mapper);
    this.tokens = new AccessTokenManager(config, api, mapper);
    this.sender = new QqMessageSenderImpl(api, tokens, new MessageSeqAllocator(), mapper);
  }

  /** 消息发送接口。 */
  public QqMessageSender sender() {
    return sender;
  }

  /** 当前配置。 */
  public QqGatewayConfig config() {
    return config;
  }

  QqHttpApi api() {
    return api;
  }

  AccessTokenManager tokens() {
    return tokens;
  }

  ObjectMapper mapper() {
    return mapper;
  }

  @Override
  public void close() {
    api.close();
  }
}
