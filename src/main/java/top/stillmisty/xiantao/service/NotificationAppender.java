package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.qqgateway.QqButton;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.util.CommandHandlerHelper;
import top.stillmisty.xiantao.util.TextFormat;

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
    // dispatch 链路中认证拦截器已绑定 userId，直接复用；仅兜底场景走二次认证
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      ServiceResult<Long> auth = authenticationService.authenticate(platform, openId);
      if (!(auth instanceof ServiceResult.Success<Long>(var authenticated))) {
        return new AppendResult(response, List.of(), null);
      }
      userId = authenticated;
    }
    return prepareAppend(userId, response, fmt);
  }

  /** 查询指定用户的未投递事件，格式化拼接 */
  @Transactional(readOnly = true)
  public AppendResult prepareAppend(Long userId, String response, TextFormat fmt) {
    List<GameEvent> events = gameEventService.findUndelivered(userId);
    if (events.isEmpty()) {
      return new AppendResult(response, List.of(), null);
    }

    String notificationText = formatEvents(events, fmt);

    // CHOICE 事件保持未投递状态：在玩家做出选择前每次回复都重新展示选项
    List<Long> deliverableIds = new ArrayList<>();
    for (GameEvent event : events) {
      if (event.isChoiceEvent()) {
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

    return new AppendResult(combined, deliverableIds, buildChoiceKeyboard(events));
  }

  /**
   * 从首个待选择事件构建按钮键盘：点击即发送「选 X」，与正文里的文本选项互为兜底。
   *
   * <p>平台限制最多 5×5 个按钮，超出的选项只保留文本形式。
   */
  private static @Nullable QqKeyboard buildChoiceKeyboard(List<GameEvent> events) {
    for (GameEvent event : events) {
      if (!event.isChoiceEvent()) {
        continue;
      }
      if (!(event.getEffectData() instanceof EffectData.ChoiceOptions choiceOptions)) {
        return null;
      }
      return choiceKeyboard(choiceOptions.options());
    }
    return null;
  }

  private static @Nullable QqKeyboard choiceKeyboard(List<EffectData.Option> options) {
    List<QqButton> buttons = new ArrayList<>();
    int index = 0;
    for (EffectData.Option option : options) {
      String key = option.key();
      if (key == null || key.isBlank()) {
        continue;
      }
      String text = option.text() == null ? "" : option.text().strip();
      String label = text.isEmpty() ? key : truncateLabel(text);
      buttons.add(QqButton.command("choice-" + index, label, "选 " + key));
      index++;
      if (buttons.size() >= QqKeyboard.MAX_BUTTONS) {
        log.warn("选择事件选项超过按钮上限（{}），多余选项仅保留文本形式", QqKeyboard.MAX_BUTTONS);
        break;
      }
    }
    if (buttons.isEmpty()) {
      return null;
    }
    return QqKeyboard.commandGrid(buttons);
  }

  private static String truncateLabel(String text) {
    int maxCodePoints = 20;
    if (text.codePointCount(0, text.length()) <= maxCodePoints) {
      return text;
    }
    return text.substring(0, text.offsetByCodePoints(0, maxCodePoints)) + "…";
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

  private String formatSingleEvent(GameEvent event, TextFormat fmt) {
    String narrativeKey = event.getNarrativeKey();
    Map<String, Object> args = event.getNarrativeArgs();

    if (narrativeKey != null && args != null) {
      String rendered = renderTemplate(narrativeKey, args);
      rendered = CommandHandlerHelper.applyBoldFormatting(rendered, fmt);
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
      case HP_RECOVERED -> "你的气血已完全恢复。";
      case DYING_RECOVERED -> "你从重伤中恢复了过来。";
      case BUFF_EXPIRED -> "身上的增益效果已消失。";
      case BOUNTY_READY -> "悬赏任务已完成，请使用「悬赏结算」领取奖励。";
      case TRAINING_INTERRUPTED -> "你在历练中受了重伤，不得不中断。";
      case LEVEL_UP -> "你突破了！";
      default -> {
        // 正常情况下叙事类事件都携带 narrativeKey，缺失说明数据异常；
        // 返回兜底文案保证事件能被投递，避免僵尸事件反复查询
        log.warn(
            "游戏事件缺少叙事模板: id={}, category={}, narrativeKey={}",
            event.getId(),
            event.getCategory(),
            narrativeKey);
        yield "你有了新的经历。";
      }
    };
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

  /**
   * 追加结果。
   *
   * @param text 拼接后的回复文本
   * @param eventIds 发送成功后需要标记为已投递的事件 ID
   * @param keyboard 可选按钮键盘（选择事件）
   */
  public record AppendResult(String text, List<Long> eventIds, @Nullable QqKeyboard keyboard) {}
}
