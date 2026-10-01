package top.stillmisty.qqgateway;

import java.net.URI;

/** WebSocket 传输抽象，便于对连接状态机做无网络单测。 */
interface WsTransport {

  /** 传输层回调。 */
  interface Listener {

    void onOpen();

    void onText(String payload);

    void onClosed(int statusCode, String reason);

    void onFailure(Throwable error);
  }

  /** 建立连接，返回时握手已完成。 */
  void connect(URI uri, Listener listener) throws Exception;

  /** 发送一条文本消息；失败由上层心跳/重连机制兜底。 */
  void sendText(String payload);

  /** 主动关闭连接（幂等）。 */
  void close();
}

/** 传输层工厂（测试注入假实现）。 */
@FunctionalInterface
interface WsTransportFactory {

  WsTransport create();
}
