package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;

/** 通知追加器 — 在每条回复发送前查询未投递事件，格式化后追加到回复尾部 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAppender {

  private final GameEventService gameEventService;
  private final AuthenticationService authenticationService;

  /** 根据平台和 openId 解析 userId，查询 events，格式化拼接 */
  @Transactional(readOnly = true)
  public AppendResult prepareAppend(
      PlatformType platform, String openId, String response, TextFormat fmt) {
    ServiceResult<Long> auth = authenticationService.authenticate(platform, openId);
    if (auth instanceof ServiceResult.Failure<Long>) {
      return new AppendResult(response, List.of(), null);
    }
    Long userId = ((ServiceResult.Success<Long>) auth).data();

    List<GameEvent> events = gameEventService.findUndelivered(userId);
    if (events.isEmpty()) {
      return new AppendResult(response, List.of(), null);
    }

    String notificationText = formatEvents(events, fmt);

    // 检测是否有 CHOICE 事件未决，有则只投递到 choice 之前的事件
    Long pendingChoiceEventId = null;
    List<Long> deliverableIds = new ArrayList<>();
    for (GameEvent event : events) {
      if (event.isChoiceEvent()) {
        pendingChoiceEventId = event.getId();
        break;
      }
      deliverableIds.add(event.getId());
    }

    if (notificationText.isEmpty()) {
      return new AppendResult(response, List.of(), null);
    }

    String combined = response;
    if (!combined.isEmpty()) {
      combined += fmt.separator();
    }
    combined += notificationText;

    return new AppendResult(combined, deliverableIds, pendingChoiceEventId);
  }

  /** 发送成功后标记事件为已投递 */
  @Transactional
  public void markDelivered(List<Long> eventIds) {
    if (eventIds != null && !eventIds.isEmpty()) {
      gameEventService.markDelivered(eventIds);
    }
  }

  // ===================== 格式化 =====================

  private String formatEvents(List<GameEvent> events, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    var grouped =
        events.stream()
            .collect(
                Collectors.groupingBy(
                    e -> getSectionGroup(e.getCategory()),
                    LinkedHashMap::new,
                    Collectors.toList()));

    boolean firstGroup = true;
    for (Map.Entry<String, List<GameEvent>> entry : grouped.entrySet()) {
      if (!firstGroup) {
        sb.append(fmt.separator());
      }
      firstGroup = false;
      String sectionTitle = entry.getKey();
      List<GameEvent> group = entry.getValue();

      if (!sectionTitle.isEmpty()) {
        sb.append("\n").append(fmt.heading(sectionTitle));
        for (int i = 0; i < group.size(); i++) {
          if (i > 0) sb.append(fmt.separator());
          sb.append(formatSingleEvent(group.get(i), fmt)).append("\n");
        }
      } else {
        for (int i = 0; i < group.size(); i++) {
          if (i > 0) sb.append(fmt.separator());
          sb.append(formatSingleEvent(group.get(i), fmt));
        }
      }
    }

    return sb.toString();
  }

  private String getSectionGroup(GameEventCategory category) {
    if (category == null) return "";
    return category.getSectionTitle() != null ? category.getSectionTitle() : "";
  }

  private static final Pattern BRACKET_PATTERN = Pattern.compile("【([^】]+)】");

  private String formatSingleEvent(GameEvent event, TextFormat fmt) {
    String narrativeKey = event.getNarrativeKey();
    Map<String, Object> args = event.getNarrativeArgs();

    if (narrativeKey != null && args != null) {
      String rendered = renderTemplate(narrativeKey, args);
      rendered = applyBoldFormatting(rendered, fmt);
      rendered = applyWorldEventBold(rendered, event.getCategory(), fmt);
      if (event.isChoiceEvent()) {
        return rendered + "\n" + renderChoiceOptions(event.getEffectData(), fmt);
      }
      return rendered;
    }

    if (event.isChoiceEvent()) {
      return renderChoiceOptions(event.getEffectData(), fmt);
    }

    return switch (event.getCategory()) {
      case TRAVEL_ARRIVED -> "你到达了目的地。";
      case HP_RECOVERED -> "你的生命值已完全恢复。";
      case DYING_RECOVERED -> "你从重伤中恢复了过来。";
      case BUFF_EXPIRED -> "身上的增益效果已消失。";
      case BOUNTY_READY -> "悬赏任务已完成，请使用「悬赏结算」领取奖励。";
      case TRAINING_INTERRUPTED -> "你在历练中受了重伤，不得不中断。";
      case LEVEL_UP -> "你突破了！";
      case FORTUNE -> narrativeKey != null ? narrativeKey : "今日运势已更新";
      default -> "";
    };
  }

  private String applyBoldFormatting(String text, TextFormat fmt) {
    Matcher matcher = BRACKET_PATTERN.matcher(text);
    if (!matcher.find()) {
      return text;
    }
    matcher.reset();
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(sb, Matcher.quoteReplacement(fmt.bold(matcher.group(1))));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private String applyWorldEventBold(String text, GameEventCategory category, TextFormat fmt) {
    if (category != GameEventCategory.WORLD_EVENT
        && category != GameEventCategory.WORLD_EVENT_PARTICIPATION) {
      return text;
    }
    int colonIdx = text.indexOf('：');
    if (colonIdx <= 0) {
      return text;
    }
    return fmt.bold(text.substring(0, colonIdx)) + text.substring(colonIdx + 1);
  }

  private String renderChoiceOptions(@Nullable EffectData effectData, TextFormat fmt) {
    if (!(effectData instanceof EffectData.ChoiceOptions choiceOptions)) return "";
    List<EffectData.Option> options = choiceOptions.options();
    if (options.isEmpty()) return "";
    StringBuilder sb = new StringBuilder("\n请选择：\n");
    for (EffectData.Option option : options) {
      String optKey = option.key() != null ? option.key() : "?";
      String optText = option.text() != null ? option.text() : "";
      sb.append(fmt.bold(optKey)).append(" ").append(optText).append("\n");
    }
    return sb.toString();
  }

  private String renderTemplate(String template, Map<String, Object> args) {
    String result = template;
    for (Map.Entry<String, Object> entry : args.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
    }
    // 未替换的 {{xxx}} 占位符替换为 ？，避免暴露内部变量名
    result = result.replaceAll("\\{\\{[^}]+}}", "？");
    return result;
  }

  public record AppendResult(
      String text, List<Long> eventIds, @Nullable Long pendingChoiceEventId) {}
}
