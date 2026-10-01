package top.stillmisty.xiantao.handle.dispatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

/**
 * 按 key 串行、跨 key 并行的执行器。
 *
 * <p>用途：同一玩家（{@code platform:openId}）的命令按到达顺序串行执行，避免并发指令竞态（双击、连点、重复注册等）； 不同玩家之间互不阻塞。
 *
 * <p>实现为每个 key 维护一条 {@link CompletableFuture} 尾链：新任务挂到当前链尾，跨 key 直接并行。队列排空时链尾的 {@code whenComplete}
 * 会移除自身条目，内存只与「正在执行/排队中的玩家数」成正比。
 *
 * <p>任务异常会被捕获并记录，不会打断后续任务的执行。
 */
@Slf4j
final class PerKeySerialExecutor {

  private final ConcurrentHashMap<String, CompletableFuture<Void>> tails =
      new ConcurrentHashMap<>();
  private final Executor delegate;

  PerKeySerialExecutor(Executor delegate) {
    this.delegate = delegate;
  }

  /** 提交任务：同一 key 上的任务严格按提交顺序串行执行。 */
  void execute(String key, Runnable task) {
    CompletableFuture<Void> next =
        tails.compute(
            key,
            (ignored, tail) -> {
              CompletableFuture<Void> base =
                  tail == null ? CompletableFuture.completedFuture(null) : tail;
              return base.thenRunAsync(
                  () -> {
                    try {
                      task.run();
                    } catch (Throwable e) {
                      log.error("串行车道任务异常: key={}", key, e);
                    }
                  },
                  delegate);
            });
    // 注册必须放在 compute 之外：任务可能已即时完成，在锁内回调会与并发提交互相干扰
    var ignored = next.whenComplete((ignoredResult, error) -> tails.remove(key, next));
  }

  /** 当前仍有尾链的 key 数（测试/观测用）。 */
  int activeLanes() {
    return tails.size();
  }
}
