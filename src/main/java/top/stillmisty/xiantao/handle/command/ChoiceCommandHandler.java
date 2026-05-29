package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ChoiceService;
import top.stillmisty.xiantao.service.UserContext;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChoiceCommandHandler implements CommandGroup {

  private final ChoiceService choiceService;

  public String handleChoice(String choice, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理选择 - UserId: {}, Choice: {}", userId, choice);
    return CommandHandlerHelper.safeCall(
        () -> choiceService.handleChoice(userId, choice), fmt, result -> result);
  }

  @Override
  public String groupName() {
    return "事件抉择";
  }

  @Override
  public String groupSummary() {
    return "选择事件决策";
  }

  @Override
  public String groupDescription() {
    return "对游戏中的选择事件做出抉择";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("选 「A/B/C」", "对游戏中的选择事件做出抉择（A/B/C）", "选 A"));
  }
}
