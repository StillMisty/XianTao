package top.stillmisty.xiantao.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

/** 通知追加器 — 在每条回复发送前查询未投递事件，格式化后追加到回复尾部 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAppender {

  private final GameEventService gameEventService;
  private final AuthenticationService authenticationService;

  /** 根据平台和 openId 解析 userId，查询 events，格式化拼接 */
  public AppendResult prepareAppend(PlatformType platform, String openId, String response) {
    ServiceResult<Long> authResult = authenticationService.authenticate(platform, openId);
    if (authResult instanceof ServiceResult.Failure) {
      return new AppendResult(response, List.of());
    }
    Long userId = ((ServiceResult.Success<Long>) authResult).data();

    List<GameEvent> events = gameEventService.findUndelivered(userId);
    if (events.isEmpty()) {
      return new AppendResult(response, List.of());
    }

    String notificationText = formatEvents(events);
    if (notificationText.isEmpty()) {
      return new AppendResult(response, List.of());
    }

    String combined = response;
    if (!combined.endsWith("\n") && !combined.isEmpty()) {
      combined += "\n";
    }
    combined += notificationText;

    List<Long> eventIds = events.stream().map(GameEvent::getId).toList();
    return new AppendResult(combined, eventIds);
  }

  /** 发送成功后标记事件为已投递 */
  @Transactional
  public void markDelivered(List<Long> eventIds) {
    if (eventIds != null && !eventIds.isEmpty()) {
      gameEventService.markDelivered(eventIds);
    }
  }

  // ===================== 格式化 =====================

  private String formatEvents(List<GameEvent> events) {
    StringBuilder sb = new StringBuilder();

    var grouped =
        events.stream()
            .collect(
                Collectors.groupingBy(
                    e -> getSectionGroup(e.getCategory()),
                    LinkedHashMap::new,
                    Collectors.toList()));

    for (Map.Entry<String, List<GameEvent>> entry : grouped.entrySet()) {
      String sectionTitle = entry.getKey();
      List<GameEvent> group = entry.getValue();

      if (sectionTitle != null) {
        sb.append("\n━━━ ").append(sectionTitle).append(" ━━━\n");
        for (GameEvent event : group) {
          sb.append(formatSingleEvent(event)).append("\n");
        }
      } else {
        for (GameEvent event : group) {
          sb.append(formatSingleEvent(event));
        }
      }
    }

    return sb.toString();
  }

  private String getSectionGroup(GameEventCategory category) {
    if (category == null) return null;
    String code = category.getCode();
    if (GameEventCategory.isTravel(code)) return category.getSectionTitle();
    if (GameEventCategory.isTraining(code)) return category.getSectionTitle();
    if (GameEventCategory.isBounty(code)) return category.getSectionTitle();
    return category.getSectionTitle();
  }

  private String formatSingleEvent(GameEvent event) {
    String narrativeKey = event.getNarrativeKey();
    Map<String, Object> args = event.getNarrativeArgs();

    if (narrativeKey != null && args != null && !args.isEmpty()) {
      return renderTemplate(narrativeKey, args);
    }

    return switch (event.getCategory()) {
      case TRAVEL_ARRIVED -> "你到达了目的地。";
      case HP_RECOVERED -> "你的生命值已完全恢复。";
      case DYING_RECOVERED -> "你从重伤中恢复了过来。";
      case BUFF_EXPIRED -> "身上的增益效果已消失。";
      case BOUNTY_READY -> "悬赏任务已完成，请使用「悬赏结算」领取奖励。";
      case TRAINING_INTERRUPTED -> "你在历练中受了重伤，不得不中断。";
      case LEVEL_UP -> "你突破了！";
      default -> "";
    };
  }

  private String renderTemplate(String template, Map<String, Object> args) {
    String result = template;
    for (Map.Entry<String, Object> entry : args.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
    }
    return result;
  }

  public record AppendResult(String text, List<Long> eventIds) {}
}
