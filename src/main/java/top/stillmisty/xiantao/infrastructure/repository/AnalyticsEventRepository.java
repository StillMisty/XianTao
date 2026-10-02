package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.analytics.entity.AnalyticsEvent;
import top.stillmisty.xiantao.infrastructure.mapper.AnalyticsEventMapper;

@Repository
@RequiredArgsConstructor
public class AnalyticsEventRepository {

  private final AnalyticsEventMapper analyticsEventMapper;

  /** 批量写入事件（分析数据 best-effort，不阻塞业务事务）。 */
  @Transactional
  public int insertAll(List<AnalyticsEvent> events) {
    int affected = 0;
    for (AnalyticsEvent event : events) {
      affected += analyticsEventMapper.insert(event);
    }
    return affected;
  }

  /** 全量玩家每日快照（upsert）。 */
  @Transactional
  public int snapshotAllPlayers() {
    return analyticsEventMapper.snapshotAllPlayers();
  }
}
