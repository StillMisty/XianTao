package top.stillmisty.xiantao.service.analytics;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.analytics.entity.AnalyticsEvent;
import top.stillmisty.xiantao.infrastructure.repository.AnalyticsEventRepository;

/**
 * 分析事件采集服务（异步批写，best-effort）。
 *
 * <p>业务线程只做入队（微秒级），独立虚拟线程每 2 秒或积满 200 条批量落库； 队列满时丢弃并计数——分析数据不参与任何运行时逻辑，丢失不影响游戏。事件字典见
 * tools/analytics/README.md。
 */
@Slf4j
@Service
public class AnalyticsService {

  private static final int MAX_QUEUE = 20_000;
  private static final int BATCH_SIZE = 200;
  private static final long FLUSH_INTERVAL_MS = 2_000;

  private final AnalyticsEventRepository repository;
  private final BlockingQueue<AnalyticsEvent> queue = new LinkedBlockingQueue<>(MAX_QUEUE);
  private final AtomicLong dropped = new AtomicLong();
  private final Thread flusher;

  public AnalyticsService(AnalyticsEventRepository repository) {
    this.repository = repository;
    this.flusher = Thread.ofVirtual().name("analytics-flusher").start(this::flushLoop);
  }

  /** 记录事件（无扩展字段）。 */
  public void record(
      String kind, @Nullable Long userId, @Nullable String subject, @Nullable Long value) {
    record(kind, userId, subject, value, Map.of());
  }

  /** 记录事件；payload 会拷贝为可变 Map 后入库。 */
  public void record(
      String kind,
      @Nullable Long userId,
      @Nullable String subject,
      @Nullable Long value,
      Map<String, Object> payload) {
    AnalyticsEvent event = new AnalyticsEvent();
    event.setUserId(userId);
    event.setKind(kind);
    event.setSubject(subject);
    event.setValue(value);
    event.setPayload(new LinkedHashMap<>(payload));
    if (!queue.offer(event)) {
      long total = dropped.incrementAndGet();
      if (total == 1 || total % 100 == 0) {
        log.warn("分析事件队列已满，累计丢弃 {} 条（total dropped={}）", total, dropped.get());
      }
    }
  }

  private void flushLoop() {
    List<AnalyticsEvent> batch = new ArrayList<>(BATCH_SIZE);
    while (!Thread.currentThread().isInterrupted()) {
      try {
        AnalyticsEvent first = queue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        if (first != null) {
          batch.add(first);
        }
        queue.drainTo(batch, BATCH_SIZE - batch.size());
        if (!batch.isEmpty()) {
          persist(batch);
          batch.clear();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void persist(List<AnalyticsEvent> batch) {
    try {
      repository.insertAll(batch);
    } catch (RuntimeException e) {
      log.warn("分析事件写入失败，丢弃 {} 条: {}", batch.size(), e.getMessage());
    }
  }

  @PreDestroy
  void flushOnShutdown() {
    flusher.interrupt();
    List<AnalyticsEvent> rest = new ArrayList<>();
    queue.drainTo(rest);
    if (!rest.isEmpty()) {
      persist(rest);
      log.info("分析事件停机刷写 {} 条", rest.size());
    }
  }
}
