package top.stillmisty.xiantao.handle.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PerKeySerialExecutorTest {

  private final ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();
  private final PerKeySerialExecutor lanes = new PerKeySerialExecutor(delegate);

  @AfterEach
  void shutdown() {
    delegate.shutdownNow();
  }

  @Test
  void ordersTasksPerKey() throws Exception {
    List<Integer> order = new CopyOnWriteArrayList<>();
    CountDownLatch done = new CountDownLatch(5);
    for (int i = 1; i <= 5; i++) {
      int value = i;
      lanes.execute(
          "k",
          () -> {
            order.add(value);
            sleepQuietly(10);
            done.countDown();
          });
    }

    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertEquals(List.of(1, 2, 3, 4, 5), order);
  }

  @Test
  void runsDifferentKeysInParallel() throws Exception {
    CountDownLatch aStarted = new CountDownLatch(1);
    CountDownLatch releaseA = new CountDownLatch(1);
    CountDownLatch aDone = new CountDownLatch(1);
    CountDownLatch bDone = new CountDownLatch(1);

    lanes.execute(
        "a",
        () -> {
          aStarted.countDown();
          awaitQuietly(releaseA);
          aDone.countDown();
        });
    assertTrue(aStarted.await(2, TimeUnit.SECONDS));

    lanes.execute("b", bDone::countDown);
    assertTrue(bDone.await(2, TimeUnit.SECONDS), "不同 key 不应被阻塞");
    assertEquals(1, aDone.getCount(), "key=a 仍在执行");

    releaseA.countDown();
    assertTrue(aDone.await(2, TimeUnit.SECONDS));
  }

  @Test
  void continuesAfterTaskFailure() throws Exception {
    CountDownLatch second = new CountDownLatch(1);
    lanes.execute(
        "k",
        () -> {
          throw new IllegalStateException("boom");
        });
    lanes.execute("k", second::countDown);

    assertTrue(second.await(2, TimeUnit.SECONDS), "任务异常不应阻断同一 key 的后续任务");
  }

  @Test
  void releasesIdleLanes() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    lanes.execute("k", done::countDown);
    assertTrue(done.await(2, TimeUnit.SECONDS));

    awaitTrue(() -> lanes.activeLanes() == 0, "空闲车道应被回收");
  }

  private static void awaitTrue(BooleanSupplier condition, String message)
      throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail(message);
      }
      Thread.sleep(10);
    }
  }

  private static void sleepQuietly(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
