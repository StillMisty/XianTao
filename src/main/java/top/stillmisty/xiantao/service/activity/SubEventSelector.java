package top.stillmisty.xiantao.service.activity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.repository.ActivityEventRepository;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;

/** 子事件选择器 — 共享的加权随机抽取机制 */
@Component
@RequiredArgsConstructor
public class SubEventSelector {

  private final ActivityEventRepository activityEventRepository;

  /**
   * 从活动池中加权随机选择一个子事件
   *
   * @param activityType 活动类型 (TRAVEL / TRAINING / BOUNTY_SIDE)
   * @param ownerId 归属 ID (map_id 或 bounty_id)
   * @param triggerChance 触发概率 (0.0 ~ 1.0)
   * @return 选中的事件，或 null (未触发)
   */
  public ActivityEvent selectSubEvent(String activityType, Long ownerId, double triggerChance) {
    if (ThreadLocalRandom.current().nextDouble() >= triggerChance) return null;

    List<ActivityEvent> subEvents = activityEventRepository.findSubEvents(activityType, ownerId);
    if (subEvents.isEmpty()) return null;

    return WeightedRandom.select(subEvents, ActivityEvent::getWeight, ThreadLocalRandom.current());
  }

  /**
   * 查询该活动的隐藏事件列表
   *
   * @param activityType 活动类型
   * @param ownerId 归属 ID
   */
  public List<ActivityEvent> findHiddenEvents(String activityType, Long ownerId) {
    return activityEventRepository.findHiddenEvents(activityType, ownerId);
  }
}
