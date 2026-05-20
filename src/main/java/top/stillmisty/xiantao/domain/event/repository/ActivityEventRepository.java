package top.stillmisty.xiantao.domain.event.repository;

import java.util.List;
import java.util.Map;
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

  /** 按事件类型查询 */
  List<ActivityEvent> findByType(String activityType, Long ownerId, String eventType);

  /** 批量按类型查询（按 ownerId 分组） */
  Map<Long, List<ActivityEvent>> findByOwnerIdsAndType(
      String activityType, List<Long> ownerIds, String eventType);
}
