package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.worldevent.WorldEventParticipationService;
import top.stillmisty.xiantao.service.worldevent.WorldEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldEventCommandHandler {

  private final WorldEventService worldEventService;
  private final WorldEventParticipationService worldEventParticipationService;

  public String handleListEvents(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("查看世界事件列表 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> new ServiceResult.Success<>(worldEventService.findActiveEvents()),
        fmt,
        events -> formatEventList(events, fmt));
  }

  public String handleJoinEvent(PlatformType platform, String openId, String arg, TextFormat fmt) {
    log.debug("参与世界事件 - Platform: {}, OpenId: {}, Arg: {}", platform, openId, arg);
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
        () -> worldEventParticipationService.participate(platform, openId, eventId),
        fmt,
        result -> result);
  }

  private String formatEventList(List<WorldEvent> events, TextFormat fmt) {
    if (events.isEmpty()) {
      return fmt.subHeading("世界事件") + "当前没有进行中的世界事件。";
    }
    StringBuilder sb = new StringBuilder(fmt.subHeading("世界事件"));
    for (int i = 0; i < events.size(); i++) {
      WorldEvent event = events.get(i);
      sb.append(fmt.separator());
      sb.append(fmt.heading("["))
          .append(event.getCategory().getName())
          .append("] #")
          .append(event.getId())
          .append(" ");
      if (event.isRegional()) {
        sb.append("【").append(event.getScope().getName()).append("】");
      }
      sb.append(event.getTitle());
      sb.append("\n").append(event.getDescription());

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
