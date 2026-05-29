package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.UserContext;

@Slf4j
@Component
@RequiredArgsConstructor
public class FortuneCommandHandler implements CommandGroup {

  private final FortuneService fortuneService;

  public String handleFortune(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理运势查询 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> fortuneService.getFortune(userId), fmt, fortuneService::buildDisplay);
  }

  @Override
  public String groupName() {
    return "运势";
  }

  @Override
  public String groupSummary() {
    return "查看今日运势";
  }

  @Override
  public String groupDescription() {
    return "查看今日运势";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("今日运势", "查看今日财运/机缘/气运", "今日运势"));
  }
}
