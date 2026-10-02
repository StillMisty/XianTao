package top.stillmisty.xiantao.service.fudi;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.infrastructure.repository.FudiEventTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/**
 * 福地事件生成器 — 地灵对话触发的懒生成。
 *
 * <p>距上次事件不足 {@link #MIN_INTERVAL_HOURS} 小时返回空列表；否则从启用的模板池按权重去重抽取 1~2 条。 生成时以条件 UPDATE 原子占用 {@code
 * fudi.last_event_time}：同一批事件只结算一次（幂等）， 并发对话时也只有一条线程能拿到生成权。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FudiEventGenerator {

  private static final int MIN_INTERVAL_HOURS = 4;
  private static final int MIN_EVENT_COUNT = 1;
  private static final int MAX_EVENT_COUNT = 2;

  private final FudiEventTemplateRepository fudiEventTemplateRepository;
  private final FudiRepository fudiRepository;

  /** 生成一批福地事件（可能为空）。生成成功即记录 last_event_time，后续 4 小时内不再生成。 */
  @Transactional
  public List<FudiEventTemplate> generateEvents(Fudi fudi) {
    var lastEventTime = fudi.getLastEventTime();
    if (lastEventTime != null
        && Duration.between(lastEventTime, TimeUtil.now()).toHours() < MIN_INTERVAL_HOURS) {
      return List.of();
    }

    List<FudiEventTemplate> templates = fudiEventTemplateRepository.findEnabled();
    if (templates.isEmpty()) {
      return List.of();
    }

    // 条件 UPDATE 原子占用生成时点：并发对话时只有一条线程能生成（同一玩家消息已串行，此处兜底）
    boolean claimed =
        fudiRepository.tryClaimEventGeneration(
            fudi.getId(), TimeUtil.now().minusHours(MIN_INTERVAL_HOURS));
    if (!claimed) {
      return List.of();
    }
    fudi.touchEventTime();

    int count =
        Math.min(
            ThreadLocalRandom.current().nextInt(MIN_EVENT_COUNT, MAX_EVENT_COUNT + 1),
            templates.size());
    List<FudiEventTemplate> selected = new ArrayList<>(count);
    List<FudiEventTemplate> pool = new ArrayList<>(templates);
    for (int i = 0; i < count; i++) {
      FudiEventTemplate template = weightedRandomSelect(pool);
      if (template == null) break;
      selected.add(template);
      pool.remove(template);
    }
    log.info("玩家 {} 福地生成事件 {} 条", fudi.getUserId(), selected.size());
    return selected;
  }

  private @Nullable FudiEventTemplate weightedRandomSelect(List<FudiEventTemplate> templates) {
    if (templates.isEmpty()) return null;
    int totalWeight = templates.stream().mapToInt(FudiEventTemplate::getSelectionWeightInt).sum();
    if (totalWeight <= 0) {
      return templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
    }
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (FudiEventTemplate template : templates) {
      cumulative += template.getSelectionWeightInt();
      if (roll < cumulative) {
        return template;
      }
    }
    return templates.getLast();
  }
}
