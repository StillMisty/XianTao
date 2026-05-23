package top.stillmisty.xiantao.service.worldevent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventStatus;
import top.stillmisty.xiantao.domain.worldevent.repository.WorldEventRepository;
import top.stillmisty.xiantao.domain.worldevent.repository.WorldEventTemplateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldEventGenerator {

  private static final int MIN_ACTIVE_EVENTS = 2;
  private static final int MAX_ACTIVE_EVENTS = 6;
  private static final int MAX_REGIONAL_EVENTS = 2;

  private final WorldEventTemplateRepository templateRepository;
  private final WorldEventRepository worldEventRepository;

  @Scheduled(fixedRate = 3600000)
  @Transactional
  public void scheduledGeneration() {
    try {
      int totalActive = worldEventRepository.findActiveEvents().size();
      if (totalActive < MIN_ACTIVE_EVENTS) {
        generateNewEvents();
      }
    } catch (Exception e) {
      log.warn("世界事件定时生成失败: {}", e.getMessage());
    }
  }

  /** 从模板池加权选取生成事件（含事件链） */
  @Transactional
  public void generateNewEvents() {
    List<WorldEventTemplate> templates = templateRepository.findAll();
    if (templates.isEmpty()) {
      return;
    }

    int currentActive = worldEventRepository.findActiveEvents().size();
    int currentRegional =
        (int) worldEventRepository.findActiveByScope(WorldEventScope.REGIONAL).stream().count();
    int needed = MAX_ACTIVE_EVENTS - currentActive;
    if (needed <= 0) return;

    List<WorldEventTemplate> available =
        templates.stream()
            .filter(t -> !isCategoryOverrepresented(t.getCategory(), currentActive))
            .toList();

    if (available.isEmpty()) return;

    for (int i = 0; i < Math.min(needed, 3) && !available.isEmpty(); i++) {
      WorldEventTemplate selected = weightedRandomSelect(available);
      if (selected == null) break;

      if (selected.getScope() == WorldEventScope.REGIONAL
          && currentRegional >= MAX_REGIONAL_EVENTS) {
        available = available.stream().filter(t -> t.getScope() == WorldEventScope.GLOBAL).toList();
        if (available.isEmpty()) break;
        selected = weightedRandomSelect(available);
        if (selected == null) break;
      }

      createFromTemplate(selected);
      if (selected.getScope() == WorldEventScope.REGIONAL) currentRegional++;
    }
  }

  /** 从模板创建事件（含事件链：子事件以 UPCOMING 状态创建） */
  public WorldEvent createFromTemplate(WorldEventTemplate template) {
    WorldEvent event = buildEvent(template, WorldEventStatus.ACTIVE);
    worldEventRepository.save(event);

    if (template.getChainedTemplateId() != null) {
      templateRepository
          .findById(template.getChainedTemplateId())
          .ifPresent(
              chainedTemplate -> {
                WorldEvent childEvent = buildEvent(chainedTemplate, WorldEventStatus.UPCOMING);
                childEvent.setParentEventId(event.getId());
                childEvent.setChainOrder(2);
                childEvent.setStartTime(event.getEndTime());
                childEvent.setEndTime(
                    event.getEndTime().plusHours(chainedTemplate.getDurationHours()));
                worldEventRepository.save(childEvent);
                log.info("创建事件链: {} → {}", template.getTitle(), chainedTemplate.getTitle());
              });
    }

    return event;
  }

  private WorldEvent buildEvent(WorldEventTemplate template, WorldEventStatus status) {
    WorldEvent event = new WorldEvent();
    event.setCategory(template.getCategory());
    event.setScope(template.getScope());
    event.setTitle(template.getTitle());
    event.setDescription(template.getDescription());
    event.setStatus(status);
    event.setStartTime(LocalDateTime.now());
    event.setEndTime(LocalDateTime.now().plusHours(template.getDurationHours()));
    event.setAffectedTags(template.getAffectedTags());
    event.setGlobalMultiplier(template.getGlobalMultiplier());
    event.setEffects(template.getEffects());
    event.setParticipationEnabled(template.getParticipationEnabled());
    event.setParticipationLimit(template.getParticipationLimit());
    event.setParticipationCount(0);
    event.setParticipationEffects(template.getParticipationEffects());
    event.setCreatedBy("SYSTEM");
    return event;
  }

  private WorldEventTemplate weightedRandomSelect(List<WorldEventTemplate> templates) {
    int totalWeight = templates.stream().mapToInt(WorldEventTemplate::getSelectionWeightInt).sum();
    if (totalWeight <= 0)
      return templates.get(ThreadLocalRandom.current().nextInt(templates.size()));

    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (WorldEventTemplate template : templates) {
      cumulative += template.getSelectionWeightInt();
      if (roll < cumulative) {
        return template;
      }
    }
    return templates.getLast();
  }

  private boolean isCategoryOverrepresented(WorldEventCategory category, int totalActive) {
    if (totalActive <= 3) return false;
    long count =
        worldEventRepository.findActiveEvents().stream()
            .filter(e -> e.getCategory() == category)
            .count();
    int maxPerCategory = Math.max(2, totalActive / 3);
    return count >= maxPerCategory;
  }
}
