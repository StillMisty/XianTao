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

    // 与最终结算共用同一入口：基础修为、物品、事件循环与进度推进都在 settle 内完成
    var settlement =
        trainingSettler.settle(user.getId(), user, mapNode, lastSettled, minutesElapsed);
    var combatSummary = settlement.combatSummary();

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
                      "exp", settlement.totalExp())));
    } else {
      gameEventService.save(
          GameEvent.create(user.getId(), GameEventCategory.TRAINING_EVENT)
              .withNarrative(
                  "你在{{mapName}}已修炼 {{duration}} 分钟，继续精进中。",
                  Map.of("mapName", mapNode.getName(), "duration", durationMinutes)));
    }

    // settle 已推进 lastSettlementMinute；返回 true 由结算中枢整行落库
    return true;
  }
}
