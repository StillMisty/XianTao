package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.activity.SubEventEffectExecutor;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoiceService {

  private final GameEventService gameEventService;
  private final SubEventEffectExecutor effectExecutor;
  private final UserStateService userStateService;

  @Authenticated
  @Transactional
  public ServiceResult<String> handleChoice(
      PlatformType platform, String openId, String choiceText) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(handleChoice(userId, choiceText));
  }

  @Transactional
  public String handleChoice(Long userId, String choiceText) {
    String key = choiceText.trim().toUpperCase();
    if (key.length() != 1 || key.charAt(0) < 'A' || key.charAt(0) > 'Z') {
      return "无效的选择，请使用 A/B/C 等字母作答。";
    }

    var events = gameEventService.findUndelivered(userId);
    GameEvent choiceEvent = null;
    for (GameEvent event : events) {
      if (event.isChoiceEvent()) {
        choiceEvent = event;
        break;
      }
    }
    if (choiceEvent == null) {
      return "当前没有需要选择的事件。";
    }

    Map<String, Object> effects = choiceEvent.getEffects();
    Map<String, Object> choice = getChoice(effects);
    if (choice == null) return "选择事件数据异常。";

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> options = (List<Map<String, Object>>) choice.get("options");
    if (options == null || options.isEmpty()) return "选择事件选项为空。";

    Map<String, Object> selectedOption = null;
    for (Map<String, Object> option : options) {
      if (key.equals(option.get("key"))) {
        selectedOption = option;
        break;
      }
    }
    if (selectedOption == null) {
      return "无效的选择「"
          + key
          + "」，可用选项："
          + options.stream()
              .map(o -> (String) o.get("key"))
              .reduce((a, b) -> a + "/" + b)
              .orElse("");
    }

    User user = userStateService.loadUser(userId);
    Map<String, Object> templateArgs = executeOptionEffects(selectedOption, userId, user);

    gameEventService.markDelivered(List.of(choiceEvent.getId()));

    String optionText = (String) selectedOption.get("text");
    return "你选择了【" + key + "】" + optionText + "。\n" + formatEffectResults(templateArgs);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getChoice(Map<String, Object> effects) {
    if (effects == null) return null;
    return (Map<String, Object>) effects.get("choice");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> executeOptionEffects(
      Map<String, Object> option, Long userId, User user) {
    List<Map<String, Object>> effectsList = (List<Map<String, Object>>) option.get("effects");
    if (effectsList == null || effectsList.isEmpty()) return Map.of();

    Map<String, Object> container = Map.of("effects", effectsList);
    return effectExecutor.executeEffects(container, userId, user, Map.of());
  }

  private String formatEffectResults(Map<String, Object> templateArgs) {
    if (templateArgs == null || templateArgs.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Object> entry : templateArgs.entrySet()) {
      if (sb.length() > 0) sb.append("，");
      sb.append("+").append(entry.getValue()).append(" ").append(entry.getKey());
    }
    if (sb.length() > 0) return sb.toString();
    return "";
  }
}
