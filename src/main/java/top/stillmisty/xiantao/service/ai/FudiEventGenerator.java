package top.stillmisty.xiantao.service.ai;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.infrastructure.repository.FudiEventTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/** 福地事件生成器 对话时从模板池懒生成 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FudiEventGenerator {

  private static final int MIN_EVENTS = 1;
  private static final int MAX_EVENTS = 6;
  private static final int MIN_HOURS_BETWEEN_EVENTS = 4;

  private final FudiEventTemplateRepository fudiEventTemplateRepository;

  /**
   * 生成福地事件列表
   *
   * @param lastEventTime 上次事件时间
   * @return 事件列表（最多6条，无重复）
   */
  public List<FudiEventTemplate> generateEvents(LocalDateTime lastEventTime) {
    LocalDateTime now = TimeUtil.now();

    if (lastEventTime != null) {
      long hoursSinceLastEvent = ChronoUnit.HOURS.between(lastEventTime, now);
      if (hoursSinceLastEvent < MIN_HOURS_BETWEEN_EVENTS) {
        return List.of();
      }
    }

    List<FudiEventTemplate> allTemplates = fudiEventTemplateRepository.findAll();
    if (allTemplates.isEmpty()) {
      return List.of();
    }

    int eventCount = calculateEventCount(lastEventTime, now);

    List<FudiEventTemplate> shuffled = new ArrayList<>(allTemplates);
    Collections.shuffle(shuffled);
    int limit = Math.min(eventCount, shuffled.size());
    List<FudiEventTemplate> selectedEvents = shuffled.subList(0, limit);

    log.debug("生成福地事件 {} 条", selectedEvents.size());
    return selectedEvents;
  }

  private int calculateEventCount(LocalDateTime lastEventTime, LocalDateTime now) {
    if (lastEventTime == null) {
      return MIN_EVENTS + ThreadLocalRandom.current().nextInt(2);
    }

    long hoursSinceLastEvent = ChronoUnit.HOURS.between(lastEventTime, now);

    int baseCount =
        (int)
            (hoursSinceLastEvent
                / (MIN_HOURS_BETWEEN_EVENTS + ThreadLocalRandom.current().nextInt(5)));

    return Math.clamp(baseCount, MIN_EVENTS, MAX_EVENTS);
  }

  public List<String> getEventDescriptions(List<FudiEventTemplate> events) {
    return events.stream().map(event -> event.getName() + "：" + event.getDescription()).toList();
  }

  /** 提取有机制效果的事件列表 */
  public List<FudiEventTemplate> getEventsWithEffects(List<FudiEventTemplate> events) {
    return events.stream().filter(FudiEventTemplate::hasEffects).toList();
  }

  /** 收集所有事件的效果摘要（用于通知文本描述） */
  public Map<String, List<Map<String, Object>>> collectEffects(List<FudiEventTemplate> events) {
    Map<String, List<Map<String, Object>>> result = new HashMap<>();
    for (FudiEventTemplate event : events) {
      if (event.hasEffects()) {
        result.put(event.getName(), event.getEffects());
      }
    }
    return result;
  }
}
