package top.stillmisty.xiantao.domain.event.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;

public interface HiddenCompletionRepository {

  /** 检查玩家是否已完成过某个隐藏事件 */
  Optional<HiddenCompletion> findByUserAndEvent(
      Long userId, String activityType, Long ownerId, String code);

  /** 记录隐藏事件完成 */
  HiddenCompletion save(HiddenCompletion completion);

  /** 检查是否已存在 (COUNT) */
  boolean exists(Long userId, String activityType, Long ownerId, String code);

  /** 检查玩家是否完成过某 code 的隐藏事件（跨活动/跨地图，用于事件链前置条件） */
  boolean existsByCode(Long userId, String code);
}
