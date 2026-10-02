package top.stillmisty.xiantao.service.player.state;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.UserBountyRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.activity.BountyCompleter;

/**
 * 悬赏自动完成 — 时长已满时把 user_bounty 标记为 COMPLETED 并产出 {@code BOUNTY_READY} 提醒，
 * 但不改变玩家状态：奖励仍需手动发送「悬赏结算」领取（保留领奖仪式感）。
 */
@Component
@RequiredArgsConstructor
@Order(7)
class BountyReadyHandler implements StateHandler {

  private final UserBountyRepository userBountyRepository;
  private final BountyCompleter bountyCompleter;

  @Override
  public boolean tryResolve(Player user) {
    if (user.getStatus() != UserStatus.BOUNTY) return false;
    if (user.getActivityType() != ActivityType.BOUNTY) return false;
    if (user.getActivityTargetId() == null) return false;

    UserBounty record = userBountyRepository.findById(user.getActivityTargetId()).orElse(null);
    if (record == null || record.getStatus() != BountyStatus.ACTIVE) return false;
    if (record.getStartTime() == null || record.getDurationMinutes() == null) return false;

    long minutesElapsed = Duration.between(record.getStartTime(), TimeUtil.now()).toMinutes();
    if (minutesElapsed < record.getDurationMinutes()) return false;

    record.setStatus(BountyStatus.COMPLETED);
    userBountyRepository.save(record);
    bountyCompleter.produceReadyEvent(user.getId(), record.getBountyName());
    return false;
  }
}
