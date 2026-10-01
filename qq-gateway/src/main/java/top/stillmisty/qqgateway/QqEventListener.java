package top.stillmisty.qqgateway;

/**
 * 消息事件监听接口。
 *
 * <p>回调在网关持有的虚拟线程上逐事件调用；实现方可以阻塞（数据库、模型调用）， 但不应永久阻塞，否则该事件的被动回复将超出 QQ 的 5 分钟时效窗口。
 */
@FunctionalInterface
public interface QqEventListener {

  /** 收到一条消息事件。 */
  void onMessage(QqIncomingMessage message);
}
