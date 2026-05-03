package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.fudi.enums.FudiEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 福地事件生成器
 * 对话时懒生成，不存库
 */
@Component
@Slf4j
public class FudiEventGenerator {

    private static final int MIN_EVENTS = 1;
    private static final int MAX_EVENTS = 6;
    private static final int MIN_HOURS_BETWEEN_EVENTS = 4;
    private static final int MAX_HOURS_BETWEEN_EVENTS = 8;

    /**
     * 生成福地事件列表
     * 
     * @param lastEventTime 上次事件时间
     * @return 事件列表（最多6条）
     */
    public List<FudiEvent> generateEvents(LocalDateTime lastEventTime) {
        LocalDateTime now = LocalDateTime.now();
        
        if (lastEventTime != null) {
            long hoursSinceLastEvent = ChronoUnit.HOURS.between(lastEventTime, now);
            if (hoursSinceLastEvent < MIN_HOURS_BETWEEN_EVENTS) {
                return List.of(); // 时间间隔太短，不生成事件
            }
        }

        // 计算应该生成的事件数量
        int eventCount = calculateEventCount(lastEventTime, now);
        
        // 随机选择事件
        List<FudiEvent> allEvents = List.of(FudiEvent.values());
        List<FudiEvent> selectedEvents = new ArrayList<>();
        
        for (int i = 0; i < eventCount && i < MAX_EVENTS; i++) {
            FudiEvent event = allEvents.get(ThreadLocalRandom.current().nextInt(allEvents.size()));
            // 避免重复事件
            if (!selectedEvents.contains(event)) {
                selectedEvents.add(event);
            }
        }

        log.debug("生成福地事件 {} 条", selectedEvents.size());
        return selectedEvents;
    }

    /**
     * 计算事件数量
     * 根据距离上次事件的时间间隔决定
     */
    private int calculateEventCount(LocalDateTime lastEventTime, LocalDateTime now) {
        if (lastEventTime == null) {
            // 首次对话，生成1-2个事件
            return MIN_EVENTS + ThreadLocalRandom.current().nextInt(2);
        }

        long hoursSinceLastEvent = ChronoUnit.HOURS.between(lastEventTime, now);
        
        // 每4-8小时生成一个事件
        int baseCount = (int) (hoursSinceLastEvent / (MIN_HOURS_BETWEEN_EVENTS + ThreadLocalRandom.current().nextInt(5)));
        
        // 限制在1-6之间
        return Math.clamp(baseCount, MIN_EVENTS, MAX_EVENTS);
    }

    /**
     * 获取事件描述列表（用于注入到Prompt中）
     */
    public List<String> getEventDescriptions(List<FudiEvent> events) {
        return events.stream()
                .map(event -> event.getName() + "：" + event.getDescription())
                .toList();
    }
}
