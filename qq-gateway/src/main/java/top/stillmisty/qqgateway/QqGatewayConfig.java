package top.stillmisty.qqgateway;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * QQ 开放平台接入配置。
 *
 * <p>{@code appBaseUri} 用于获取 App Access Token（{@code POST /app/getAppAccessToken}）， {@code
 * apiBaseUri} 用于 OpenAPI（网关地址、消息发送）。
 *
 * <p>Intents 采用位标记，默认 {@code GROUP_AND_C2C_EVENT (1 << 25)}，与线上机器人申请的权限一致。
 *
 * <p>WebSocket 鉴权 token 存在两种官方口径：新版文档为 {@code QQBot {AccessToken}}，旧版为 {@code Bot
 * {appId}.{appToken}}。通过 {@link WsTokenMode} 切换，默认使用新版口径；若线上握手被拒可改为旧版并配置 {@code appToken}。
 *
 * @param appId 机器人 AppID
 * @param clientSecret 机器人 AppSecret
 * @param appToken 机器人 Token（控制台可见），仅 {@link WsTokenMode#APP_TICKET} 需要
 * @param wsTokenMode WebSocket 鉴权 token 生成方式
 * @param appBaseUri AppID 凭证服务地址，默认 {@code https://api.bot.qq.com}
 * @param apiBaseUri OpenAPI 地址，默认 {@code https://api.bot.qq.com}
 * @param intents 事件订阅位标记
 * @param connectTimeout WebSocket 建连与 HTTP 建连超时
 * @param requestTimeout HTTP 请求超时
 * @param tokenRefreshAhead Access Token 提前刷新窗口
 */
public record QqGatewayConfig(
    String appId,
    String clientSecret,
    @Nullable String appToken,
    WsTokenMode wsTokenMode,
    URI appBaseUri,
    URI apiBaseUri,
    int intents,
    Duration connectTimeout,
    Duration requestTimeout,
    Duration tokenRefreshAhead) {

  /** {@code GROUP_AND_C2C_EVENT}：群聊 @ 消息、单聊消息等群与单聊事件。 */
  public static final int INTENT_GROUP_AND_C2C = 1 << 25;

  private static final URI DEFAULT_APP_BASE = URI.create("https://api.bot.qq.com");
  private static final URI DEFAULT_API_BASE = URI.create("https://api.bot.qq.com");
  private static final URI SANDBOX_API_BASE = URI.create("https://sandbox.api.bot.qq.com");

  /** WebSocket 鉴权 token 生成方式。 */
  public enum WsTokenMode {
    /** {@code QQBot {AccessToken}}，由 AppID + AppSecret 换取。 */
    ACCESS_TOKEN,
    /** {@code Bot {appId}.{appToken}}，使用控制台分配的机器人 Token。 */
    APP_TICKET
  }

  public QqGatewayConfig {
    Objects.requireNonNull(appId, "appId");
    Objects.requireNonNull(clientSecret, "clientSecret");
    Objects.requireNonNull(wsTokenMode, "wsTokenMode");
    Objects.requireNonNull(appBaseUri, "appBaseUri");
    Objects.requireNonNull(apiBaseUri, "apiBaseUri");
    Objects.requireNonNull(connectTimeout, "connectTimeout");
    Objects.requireNonNull(requestTimeout, "requestTimeout");
    Objects.requireNonNull(tokenRefreshAhead, "tokenRefreshAhead");
    if (appId.isBlank()) {
      throw new IllegalArgumentException("appId 不能为空");
    }
    if (clientSecret.isBlank()) {
      throw new IllegalArgumentException("clientSecret 不能为空");
    }
    if (wsTokenMode == WsTokenMode.APP_TICKET && (appToken == null || appToken.isBlank())) {
      throw new IllegalArgumentException("APP_TICKET 模式需要配置 appToken");
    }
    if (intents <= 0) {
      throw new IllegalArgumentException("intents 必须为正数");
    }
  }

  /** 生产环境默认配置。 */
  public static QqGatewayConfig of(String appId, String clientSecret) {
    return new QqGatewayConfig(
        appId,
        clientSecret,
        null,
        WsTokenMode.ACCESS_TOKEN,
        DEFAULT_APP_BASE,
        DEFAULT_API_BASE,
        INTENT_GROUP_AND_C2C,
        Duration.ofSeconds(10),
        Duration.ofSeconds(15),
        Duration.ofMinutes(5));
  }

  /** 沙箱环境默认配置。 */
  public static QqGatewayConfig sandbox(String appId, String clientSecret) {
    return of(appId, clientSecret).withApiBaseUri(SANDBOX_API_BASE);
  }

  public QqGatewayConfig withApiBaseUri(URI apiBaseUri) {
    return new QqGatewayConfig(
        appId,
        clientSecret,
        appToken,
        wsTokenMode,
        appBaseUri,
        apiBaseUri,
        intents,
        connectTimeout,
        requestTimeout,
        tokenRefreshAhead);
  }

  public QqGatewayConfig withAppBaseUri(URI appBaseUri) {
    return new QqGatewayConfig(
        appId,
        clientSecret,
        appToken,
        wsTokenMode,
        appBaseUri,
        apiBaseUri,
        intents,
        connectTimeout,
        requestTimeout,
        tokenRefreshAhead);
  }

  public QqGatewayConfig withIntents(int intents) {
    return new QqGatewayConfig(
        appId,
        clientSecret,
        appToken,
        wsTokenMode,
        appBaseUri,
        apiBaseUri,
        intents,
        connectTimeout,
        requestTimeout,
        tokenRefreshAhead);
  }

  public QqGatewayConfig withWsTokenMode(WsTokenMode mode, @Nullable String appToken) {
    return new QqGatewayConfig(
        appId,
        clientSecret,
        appToken,
        mode,
        appBaseUri,
        apiBaseUri,
        intents,
        connectTimeout,
        requestTimeout,
        tokenRefreshAhead);
  }
}
