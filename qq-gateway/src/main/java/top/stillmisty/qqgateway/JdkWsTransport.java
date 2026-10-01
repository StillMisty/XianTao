package top.stillmisty.qqgateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 基于 JDK {@link HttpClient} 的 WebSocket 传输实现，零第三方依赖。 */
final class JdkWsTransport implements WsTransport {

  private static final Logger log = LoggerFactory.getLogger(JdkWsTransport.class);
  private static final long CLOSE_TIMEOUT_SECONDS = 2;

  private final java.time.Duration connectTimeout;
  private final AtomicBoolean closed = new AtomicBoolean();

  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  private volatile @Nullable WebSocket webSocket;
  private volatile @Nullable Listener listener;

  JdkWsTransport(java.time.Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  @Override
  public void connect(URI uri, Listener listener) {
    this.listener = listener;
    WebSocket socket =
        httpClient
            .newWebSocketBuilder()
            .connectTimeout(connectTimeout)
            .buildAsync(uri, new ForwardingListener())
            .join();
    this.webSocket = socket;
    if (closed.get()) {
      // 建连期间已被要求关闭：立即中止，避免泄漏一个无人使用的连接
      socket.abort();
    }
  }

  @Override
  public void sendText(String payload) {
    WebSocket socket = webSocket;
    if (socket == null) {
      throw new IllegalStateException("WebSocket 未连接");
    }
    var ignored =
        socket
            .sendText(payload, true)
            .whenComplete(
                (result, error) -> {
                  if (error != null) {
                    log.warn("WebSocket 发送失败: {}", error.toString());
                  }
                });
  }

  @Override
  public void close() {
    closed.set(true);
    WebSocket socket = webSocket;
    if (socket == null) {
      // onOpen 尚未发生：onOpen 回调里会立即 abort
      return;
    }
    try {
      var ignored =
          socket
              .sendClose(WebSocket.NORMAL_CLOSURE, "bye")
              .orTimeout(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
              .exceptionally(
                  error -> {
                    log.debug("关闭 WebSocket 超时: {}", error.toString());
                    return null;
                  })
              .join();
    } catch (RuntimeException e) {
      log.debug("关闭 WebSocket 失败: {}", e.toString());
    } finally {
      socket.abort();
      webSocket = null;
    }
  }

  /** 把 JDK 回调转发给上层；onOpen 里先保存 socket，避免「HELLO 早于 connect 返回」时发送失败。 */
  private final class ForwardingListener implements WebSocket.Listener {

    private final StringBuilder buffer = new StringBuilder();

    @Override
    public void onOpen(WebSocket socket) {
      webSocket = socket;
      if (closed.get()) {
        socket.abort();
        return;
      }
      Listener target = listener;
      if (target != null) {
        target.onOpen();
      }
      socket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
      buffer.append(data);
      if (last) {
        String payload = buffer.toString();
        buffer.setLength(0);
        Listener target = listener;
        if (target != null) {
          try {
            target.onText(payload);
          } catch (RuntimeException e) {
            log.warn("处理 WebSocket 消息异常: {}", e.toString());
          }
        }
      }
      socket.request(1);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
      Listener target = listener;
      if (target != null) {
        target.onClosed(statusCode, reason);
      }
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
      Listener target = listener;
      if (target != null) {
        target.onFailure(error);
      }
    }
  }
}
