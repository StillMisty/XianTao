package top.stillmisty.xiantao.service.player.state;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.combat.TrainingSettler;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(5)
class TrainingSettlementHandler implements StateHandler {

  private static final long TRAINING_SETTLEMENT_INTERVAL_MINUTES = 10;

  private final MapNodeRepository mapNodeRepository;
  private final TrainingSettler trainingSettler;
  private final GameEventService gameEventService;
  private final UserRepository userRepository;

  @Override
  public boolean tryResolve(User user) {
    if (user.getStatus() != UserStatus.TRAINING) return false;
    if (user.getActivityType() != ActivityType.TRAINING) return false;
    if (user.getActivityStartTime() == null) return false;

    long minutesElapsed =
        Duration.between(user.getActivityStartTime(), LocalDateTime.now()).toMinutes();
    long lastSettled = user.getLastSettlementMinute() != null ? user.getLastSettlementMinute() : 0;
    long interval = TRAINING_SETTLEMENT_INTERVAL_MINUTES;
    if (lastSettled + interval > minutesElapsed) return false;

    var mapNode = mapNodeRepository.findById(user.getLocationId()).orElse(null);
    if (mapNode == null) return false;

    long nextSettled = lastSettled + interval;
    while (nextSettled <= minutesElapsed) {
      if (user.getStatus() == UserStatus.DYING) break;
      trainingSettler.settleChunk(user.getId(), user, mapNode, lastSettled, nextSettled);
      gameEventService.save(
          GameEvent.create(user.getId(), GameEventCategory.TRAINING_EVENT)
              .withNarrative(
                  "历练进行中：已修炼 {{from}}~{{to}} 分钟，在 {{mapName}} 继续精进。",
                  Map.of("from", lastSettled, "to", nextSettled, "mapName", mapNode.getName())));
      lastSettled = nextSettled;
      nextSettled += interval;
    }

    user.setLastSettlementMinute(lastSettled);
    userRepository.updateTrainingSettlement(
        user.getId(),
        user.getHpCurrent(),
        user.getExp(),
        user.getLastSettlementMinute() != null ? user.getLastSettlementMinute() : 0);
    return true;
  }
}
