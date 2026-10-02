package top.stillmisty.xiantao.service.worldevent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventStatus;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldEventGenerator {

  private static final int MIN_ACTIVE_EVENTS = 2;
  private static final int MAX_ACTIVE_EVENTS = 6;
  private static final int MAX_REGIONAL_EVENTS = 2;

  private final WorldEventTemplateRepository templateRepository;
  private final WorldEventRepository worldEventRepository;
  private final MapNodeRepository mapNodeRepository;

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
            .filter(t -> !isOnCooldown(t))
            // 区域模板必须存在满足 valid_region_tags 的地图节点，否则本轮不参与生成
            .filter(this::hasValidRegion)
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
      // 同一模板本轮不重复选取（冷却下限保护）
      available = withoutTemplate(available, selected);
      if (selected.getScope() == WorldEventScope.REGIONAL) currentRegional++;
    }
  }

  private List<WorldEventTemplate> withoutTemplate(
      List<WorldEventTemplate> templates, WorldEventTemplate excluded) {
    return templates.stream().filter(t -> !t.getId().equals(excluded.getId())).toList();
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
    if (template.getScope() == WorldEventScope.REGIONAL) {
      event.setRegionMapNodeId(pickRegionMapNodeId(template));
    }
    event.setTitle(template.getTitle());
    event.setDescription(template.getDescription());
    event.setStatus(status);
    var now = TimeUtil.now();
    event.setStartTime(now);
    event.setEndTime(now.plusHours(template.getDurationHours()));
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

  /**
   * 区域模板的地图过滤：解析 valid_region_tags 并确认存在匹配的地图节点。
   *
   * <p>valid_region_tags 为空表示不限区域（任意地图均可承接）；有标签但无地图匹配时该模板本轮不生成。
   */
  private boolean hasValidRegion(WorldEventTemplate template) {
    if (template.getScope() != WorldEventScope.REGIONAL) return true;
    return !findMatchingMapNodes(template).isEmpty();
  }

  /** 为区域事件挑选一个满足标签约束的地图节点；标签为空时从全部地图中随机。 */
  private @Nullable Long pickRegionMapNodeId(WorldEventTemplate template) {
    List<MapNode> candidates = findMatchingMapNodes(template);
    if (candidates.isEmpty()) return null;
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).getId();
  }

  private List<MapNode> findMatchingMapNodes(WorldEventTemplate template) {
    List<MapNode> nodes = mapNodeRepository.findAll();
    Set<String> requiredTags = template.getValidRegionTags();
    if (requiredTags == null || requiredTags.isEmpty()) {
      return nodes;
    }
    Set<String> normalizedTags =
        requiredTags.stream().map(tag -> tag.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
    return nodes.stream()
        .filter(node -> MapRegionTags.of(node).stream().anyMatch(normalizedTags::contains))
        .toList();
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

  // 模板冷却：同一模板在 cooldown_hours 内生成过事件则跳过。
  // world_event 未记录来源模板，暂以标题近似匹配（模板标题唯一）；补上模板追踪列后应改为按 ID 匹配（见 world-events/design.md）。
  private boolean isOnCooldown(WorldEventTemplate template) {
    LocalDateTime since = TimeUtil.now().minusHours(template.getCooldownHours());
    return worldEventRepository.existsByTitleSince(template.getTitle(), since);
  }
}
