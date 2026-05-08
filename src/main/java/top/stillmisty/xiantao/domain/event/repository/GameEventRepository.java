package top.stillmisty.xiantao.domain.event.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;

public interface GameEventRepository {

  GameEvent save(GameEvent event);

  List<GameEvent> saveAll(List<GameEvent> events);

  /** 查询指定用户所有未投递事件 (按 occurred_at ASC) */
  List<GameEvent> findUndeliveredByUserId(Long userId);

  /** 批量标记事件为已投递 */
  int markDelivered(List<Long> eventIds);

  /** 删除超过 7 天的已投递事件 */
  int deleteDeliveredBefore(int retentionDays);
}
