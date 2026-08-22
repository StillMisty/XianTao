package top.stillmisty.xiantao.service.player.state;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.combat.TrainingSettler;

/** 定期历练结算处理器 — 每 60 分钟自动执行一次中途结算 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(5)
class TrainingSettlementHandler implements StateHandler {

  private static final long TRAINING_SETTLEMENT_INTERVAL_MINUTES = 60;

  private final MapNodeRepository mapNodeRepository;
  private final TrainingSettler trainingSettler;
  private final GameEventService gameEventService;
  private final UserRepository userRepository;

  @Override
  public boolean tryResolve(Player user) {
    if (user.getStatus() != UserStatus.TRAINING) return false;
    if (user.getActivityType() != ActivityType.TRAINING) return false;
    if (user.getActivityStartTime() == null) return false;

    long minutesElapsed = Duration.between(user.getActivityStartTime(), TimeUtil.now()).toMinutes();
    long lastSettled = user.getLastSettlementMinute();
    if (lastSettled + TRAINING_SETTLEMENT_INTERVAL_MINUTES > minutesElapsed) return false;

    var mapNode = mapNodeRepository.findById(user.getLocationId()).orElse(null);
    if (mapNode == null) return false;

    var settlementResult =
        trainingSettler.settleChunk(user.getId(), user, mapNode, lastSettled, minutesElapsed);
    var combatSummary = settlementResult.combatSummary();

    // 击杀修为一并入账（受存储上限截断）
    long killExp = combatSummary.expGained();
    if (killExp > 0) {
      user.addExp(killExp);
    }

    long durationMinutes = minutesElapsed;
    if (combatSummary.totalEncounters() > 0) {
      gameEventService.save(
          GameEvent.create(user.getId(), GameEventCategory.TRAINING_EVENT)
              .withNarrative(
                  "你在{{mapName}}已修炼 {{duration}} 分钟，期间遭遇 {{encounters}} 场战斗，获得 +{{exp}} 修为，继续精进中。",
                  Map.of(
                      "mapName", mapNode.getName(),
                      "duration", durationMinutes,
                      "encounters", combatSummary.totalEncounters(),
                      "exp", combatSummary.expGained())));
    } else {
      gameEventService.save(
          GameEvent.create(user.getId(), GameEventCategory.TRAINING_EVENT)
              .withNarrative(
                  "你在{{mapName}}已修炼 {{duration}} 分钟，继续精进中。",
                  Map.of("mapName", mapNode.getName(), "duration", durationMinutes)));
    }

    user.setLastSettlementMinute(minutesElapsed);
    userRepository.updateTrainingSettlement(
        user.getId(), user.getHpCurrent(), user.getExp(), minutesElapsed);
    return true;
  }
}
