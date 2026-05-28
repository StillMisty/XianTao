package top.stillmisty.xiantao.service.player.state;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.service.activity.TravelCompleter;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
class TravelCompletionHandler implements StateHandler {

  private final MapNodeRepository mapNodeRepository;
  private final TravelCompleter travelCompleter;

  @Override
  public boolean tryResolve(User user) {
    if (user.getStatus() != UserStatus.TRAVELING) return false;
    if (user.getActivityType() != ActivityType.TRAVEL) return false;
    if (user.getActivityTargetId() == null) return false;

    var startTime = user.getActivityStartTime();
    if (startTime == null) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      return true;
    }

    var currentMap = mapNodeRepository.findById(user.getLocationId());
    if (currentMap.isEmpty()) return false;

    var destinationMap = mapNodeRepository.findById(user.getActivityTargetId());
    if (destinationMap.isEmpty()) return false;

    Integer travelTime = currentMap.get().getTravelTimeTo(destinationMap.get().getId());
    if (travelTime == null) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      log.warn(
          "玩家 {} 旅行卡死检测，无路径 {} → {}，已清除状态",
          user.getId(),
          currentMap.get().getName(),
          destinationMap.get().getName());
      return true;
    }

    LocalDateTime arrivalTime = startTime.plusMinutes(travelTime);
    if (LocalDateTime.now().isBefore(arrivalTime)) return false;

    log.info(
        "玩家 {} 旅行自动结算：{} → {}",
        user.getId(),
        currentMap.get().getName(),
        destinationMap.get().getName());

    user.setStatus(UserStatus.IDLE);
    user.setLocationId(user.getActivityTargetId());
    user.clearActivity();

    travelCompleter.completeTravel(user.getId(), user, currentMap.get(), destinationMap.get());
    return true;
  }
}
