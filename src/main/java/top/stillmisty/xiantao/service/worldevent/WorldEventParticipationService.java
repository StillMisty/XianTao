package top.stillmisty.xiantao.service.worldevent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldEventParticipationService {

  private final WorldEventRepository worldEventRepository;
  private final WorldEventEffectApplier worldEventEffectApplier;
  private final GameEventService gameEventService;
  private final PlayerLoader playerLoader;

  public ServiceResult<String> participate(Long userId, Long eventId) {
    return new ServiceResult.Success<>(participateInternal(userId, eventId));
  }

  @Transactional
  public String participateInternal(Long userId, Long eventId) {
    WorldEvent event =
        worldEventRepository
            .findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORLD_EVENT_NOT_FOUND));

    if (!event.isActive()) {
      throw new BusinessException(ErrorCode.WORLD_EVENT_EXPIRED);
    }

    if (event.getCategory() != WorldEventCategory.PARTICIPATORY) {
      throw new BusinessException(ErrorCode.WORLD_EVENT_NOT_PARTICIPATORY);
    }

    if (!event.canParticipate()) {
      throw new BusinessException(ErrorCode.WORLD_EVENT_PARTICIPATION_FULL);
    }

    int updated = worldEventRepository.incrementParticipationCount(eventId);
    if (updated == 0) {
      throw new BusinessException(ErrorCode.WORLD_EVENT_PARTICIPATION_FULL);
    }

    Player user = playerLoader.load(userId);

    List<Map<String, Object>> participationEffects = event.getParticipationEffects();
    String effectDesc = "";
    if (participationEffects != null && !participationEffects.isEmpty()) {
      Map<String, Object> result =
          worldEventEffectApplier.applyEffectsFromConfig(participationEffects, userId, user);
      effectDesc = buildEffectDescription(result);
    }

    GameEvent gameEvent =
        GameEvent.create(userId, GameEventCategory.WORLD_EVENT_PARTICIPATION)
            .withNarrative("{{eventTitle}}：参与了世界事件", Map.of("eventTitle", event.getTitle()));

    gameEventService.save(gameEvent);

    StringBuilder sb = new StringBuilder("你参与了【").append(event.getTitle()).append("】！");
    if (!effectDesc.isEmpty()) {
      sb.append("\n").append(effectDesc);
    }
    return sb.toString();
  }

  private String buildEffectDescription(Map<String, Object> result) {
    if (result.isEmpty()) return "";
    List<String> parts = new ArrayList<>();
    String itemName = null;
    int itemCount = 0;
    for (Map.Entry<String, Object> entry : result.entrySet()) {
      // 效果键来自内部实现（如 spiritStones），归一化后再匹配，避免内部字段名泄漏给玩家
      String key = entry.getKey().toLowerCase(Locale.ROOT).replace("_", "");
      Object value = entry.getValue();
      if (value instanceof Number num) {
        int amount = num.intValue();
        if (amount <= 0) continue;
        if (key.contains("exp")) {
          addOnce(parts, "获得修为 +" + amount);
        } else if (key.contains("spirit") && key.contains("stone")) {
          addOnce(parts, "获得灵石 +" + amount);
        } else if (key.contains("heal") || key.contains("hp")) {
          addOnce(parts, "气血 +" + amount);
        } else if (key.contains("count")) {
          itemCount = amount;
        }
        // 其余数值键（damage 等内部字段）不展示
      } else if (value instanceof String s && !s.isBlank()) {
        if (key.contains("item") || key.contains("herb")) {
          if (itemName == null) itemName = s;
        } else {
          addOnce(parts, s);
        }
      }
    }
    if (itemName != null) {
      parts.add(itemCount > 1 ? "获得物品：" + itemName + " x" + itemCount : "获得物品：" + itemName);
    }
    return String.join("，", parts);
  }

  private static void addOnce(List<String> parts, String text) {
    if (!parts.contains(text)) {
      parts.add(text);
    }
  }
}
