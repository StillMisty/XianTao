package top.stillmisty.xiantao.service.fudi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.activity.SubEventEffectExecutor;
import top.stillmisty.xiantao.service.activity.effect.EffectEntry;
import top.stillmisty.xiantao.service.player.PlayerLoader;

/**
 * 福地事件效果结算器 — 把模板机制效果交给 {@link SubEventEffectExecutor} 执行， 结果产出 {@link
 * GameEventCategory#WORLD_EVENT} 通知随回复被动投递。
 *
 * <p>纯叙事事件（无机制效果）不产生通知；单个事件结算失败只记录日志，不阻断地灵对话。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FudiEventApplier {

  private final SubEventEffectExecutor subEventEffectExecutor;
  private final PlayerLoader playerLoader;
  private final UserRepository userRepository;
  private final GameEventService gameEventService;

  /** 结算一批福地事件的机制效果并写入通知队列（幂等由生成时占用的 last_event_time 保证）。 */
  @Transactional
  public void applyFudiEventEffects(Long userId, List<FudiEventTemplate> events) {
    List<FudiEventTemplate> withEffects =
        events.stream().filter(t -> t.getEffects() != null && !t.getEffects().isEmpty()).toList();
    if (withEffects.isEmpty()) {
      return;
    }

    Player user = playerLoader.load(userId);
    boolean userChanged = false;
    for (FudiEventTemplate template : withEffects) {
      try {
        List<Map<String, Object>> effectMaps = new ArrayList<>(template.getEffects().size());
        for (EffectEntry entry : template.getEffects()) {
          effectMaps.add(entry.toEffectMap());
        }
        Map<String, Object> result =
            subEventEffectExecutor.executeEffects(effectMaps, userId, user, EventContext.empty());
        if (result.isEmpty()) {
          continue;
        }
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.WORLD_EVENT)
                .withNarrative(template.getName() + "：" + template.getDescription(), result));
        userChanged = true;
      } catch (Exception e) {
        log.warn(
            "福地事件结算失败 - userId: {}, event: {}, error: {}",
            userId,
            template.getName(),
            e.getMessage());
      }
    }
    if (userChanged) {
      userRepository.save(user);
    }
  }
}
