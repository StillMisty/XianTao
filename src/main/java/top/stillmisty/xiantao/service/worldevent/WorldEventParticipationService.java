package top.stillmisty.xiantao.service.worldevent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldEventParticipationService {

  private final WorldEventRepository worldEventRepository;
  private final WorldEventEffectApplier worldEventEffectApplier;
  private final GameEventService gameEventService;
  private final UserStateService userStateService;

  @Authenticated
  public ServiceResult<String> participate(PlatformType platform, String openId, Long eventId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(participate(userId, eventId));
  }

  @Transactional
  public String participate(Long userId, Long eventId) {
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

    User user = userStateService.loadUser(userId);

    List<Map<String, Object>> participationEffects = event.getParticipationEffects();
    String effectDesc = "";
    if (participationEffects != null && !participationEffects.isEmpty()) {
      Map<String, Object> result =
          worldEventEffectApplier.applyEffectsFromConfig(participationEffects, userId, user);
      effectDesc = buildEffectDescription(result);
    }

    GameEvent gameEvent =
        GameEvent.create(userId, GameEventCategory.WORLD_EVENT_PARTICIPATION)
            .withNarrative(
                "参与世界事件",
                Map.of("eventTitle", event.getTitle(), "eventDescription", event.getDescription()));

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
    result.forEach(
        (key, value) -> {
          if (value instanceof Number num && num.intValue() > 0) {
            if (key.contains("exp") || key.contains("EXP")) {
              parts.add("获得修为 +" + num.intValue());
            } else if (key.contains("spirit_stone")) {
              parts.add("获得灵石 +" + num.intValue());
            } else if (key.contains("heal") || key.contains("hp")) {
              parts.add("恢复生命 +" + num.intValue());
            } else if (key.contains("item")) {
              parts.add("获得物品：" + value);
            } else {
              parts.add(key + ": +" + num.intValue());
            }
          } else if (value instanceof String s && !s.isBlank()) {
            parts.add(s);
          }
        });
    return String.join("，", parts);
  }
}
