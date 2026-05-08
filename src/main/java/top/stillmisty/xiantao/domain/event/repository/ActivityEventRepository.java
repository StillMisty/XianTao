package top.stillmisty.xiantao.domain.event.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;

public interface ActivityEventRepository {

  /**
   * 查询指定活动的非隐藏子事件配置
   *
   * @param activityType 活动类型 (TRAVEL / TRAINING / BOUNTY_SIDE)
   * @param ownerId 事件归属 (map_id 或 bounty_id)
   */
  List<ActivityEvent> findSubEvents(String activityType, Long ownerId);

  /**
   * 查询指定活动的隐藏事件配置
   *
   * @param activityType 活动类型
   * @param ownerId 事件归属
   */
  List<ActivityEvent> findHiddenEvents(String activityType, Long ownerId);

  /** 保存 */
  void save(ActivityEvent event);
}
