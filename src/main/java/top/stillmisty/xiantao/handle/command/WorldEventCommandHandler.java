package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.worldevent.WorldEventParticipationService;
import top.stillmisty.xiantao.service.worldevent.WorldEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldEventCommandHandler implements CommandGroup {

  private final WorldEventService worldEventService;
  private final WorldEventParticipationService worldEventParticipationService;

  @Override
  public String groupName() {
    return "世界事件";
  }

  @Override
  public String groupSummary() {
    return "世界事件查看与参与";
  }

  @Override
  public String groupDescription() {
    return "查看当前世界事件、参与事件";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("世界事件", "查看当前进行中的世界事件", "世界事件"),
        new CommandEntry("参与事件 「编号」", "参与指定的世界事件", "参与事件 1"));
  }

  public String handleListEvents(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("查看世界事件列表 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> new ServiceResult.Success<>(worldEventService.findActiveEvents()),
        fmt,
        events -> formatEventList(events, fmt));
  }

  public String handleJoinEvent(String arg, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("参与世界事件 - UserId: {}, Arg: {}", userId, arg);
    if (arg == null || arg.isBlank()) {
      return fmt.error("请输入要参与的事件编号，如「参与事件 1」");
    }
    long eventId;
    try {
      eventId = Long.parseLong(arg.trim());
    } catch (NumberFormatException e) {
      return fmt.error("事件编号格式错误，请输入数字编号");
    }
    return CommandHandlerHelper.safeCall(
        () -> worldEventParticipationService.participate(userId, eventId),
        fmt,
        result -> CommandHandlerHelper.applyBoldFormatting(result, fmt));
  }

  private String formatEventList(List<WorldEvent> events, TextFormat fmt) {
    if (events.isEmpty()) {
      return fmt.subHeading("世界事件") + "当前没有进行中的世界事件。";
    }
    StringBuilder sb = new StringBuilder(fmt.subHeading("世界事件"));
    for (int i = 0; i < events.size(); i++) {
      WorldEvent event = events.get(i);
      sb.append(fmt.separator());
      sb.append(fmt.bold("[" + event.getCategory().getName() + " #" + event.getId() + "]"));
      if (event.isRegional()) {
        sb.append(" ").append(fmt.bold(event.getScope().getName()));
      }
      sb.append(" ").append(event.getTitle()).append("\n");
      sb.append(event.getDescription()).append("\n");

      if (event.getCategory() == WorldEventCategory.PARTICIPATORY
          && Boolean.TRUE.equals(event.getParticipationEnabled())) {
        boolean unlimited =
            event.getParticipationLimit() == null || event.getParticipationLimit() == 0;
        sb.append(
            fmt.tip(
                "\n"
                    + (unlimited
                        ? "参与人数：无限制"
                        : "参与人数："
                            + event.getParticipationCount()
                            + "/"
                            + event.getParticipationLimit())));
      }
      sb.append("\n");
    }
    sb.append(fmt.tip("输入「参与事件 <编号>」来参与支持的事件"));
    return sb.toString();
  }
}
