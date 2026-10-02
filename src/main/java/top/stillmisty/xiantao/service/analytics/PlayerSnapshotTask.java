package top.stillmisty.xiantao.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.infrastructure.repository.AnalyticsEventRepository;

/** 玩家每日快照任务：每小时 upsert 当日快照（当日最后一次写入即日终态）。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerSnapshotTask {

  private final AnalyticsEventRepository analyticsEventRepository;

  @Scheduled(cron = "0 5 * * * ?", zone = "Asia/Shanghai")
  public void snapshotHourly() {
    try {
      int rows = analyticsEventRepository.snapshotAllPlayers();
      log.debug("玩家每日快照 upsert {} 行", rows);
    } catch (RuntimeException e) {
      log.warn("玩家每日快照失败: {}", e.getMessage());
    }
  }
}
