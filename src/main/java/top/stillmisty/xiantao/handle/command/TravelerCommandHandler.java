package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.TravelerChatService;
import top.stillmisty.xiantao.util.CommandHandlerHelper;
import top.stillmisty.xiantao.util.TextFormat;

@Component
@RequiredArgsConstructor
public class TravelerCommandHandler implements CommandGroup {

  private final TravelerChatService travelerChatService;

  @Override
  public String groupName() {
    return "旅行商人";
  }

  @Override
  public String groupSummary() {
    return "与旅行商人交易";
  }

  @Override
  public String groupDescription() {
    return "与旅途偶遇的旅行商人自然语言交易（买货/打听）";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("游商 「内容」", "与旅行商人自然语言交互（仅在摊位有效期内）", "游商 看货"));
  }

  public String handleTraveler(String userInput, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> travelerChatService.chatWithTraveler(userId, userInput),
        fmt,
        msg -> msg,
        msg -> fmt.bold("旅行商人") + " " + msg);
  }
}
