package top.stillmisty.xiantao.service.pill;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;

/** 过期 Buff 全局清理 — 玩法侧已按 expires_at 过滤，此处仅做存储卫生。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerBuffCleanupTask {

  private final PlayerBuffRepository playerBuffRepository;

  /** 每小时清理全库过期 Buff（玩家懒清理之外的兜底，覆盖长期不上线的历史数据） */
  @Scheduled(cron = "0 20 * * * ?", zone = "Asia/Shanghai")
  @Transactional
  public void cleanupExpiredBuffs() {
    int deleted = playerBuffRepository.deleteExpired();
    if (deleted > 0) {
      log.info("清理过期 Buff {} 条", deleted);
    }
  }
}
