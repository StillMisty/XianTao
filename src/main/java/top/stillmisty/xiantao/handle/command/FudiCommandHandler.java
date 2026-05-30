package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.fudi.vo.FudiStatusVO;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.SpiritChatService;
import top.stillmisty.xiantao.service.fudi.FudiService;

@Component
@Slf4j
@RequiredArgsConstructor
public class FudiCommandHandler implements CommandGroup {

  private final FudiService fudiService;
  private final SpiritChatService spiritChatService;

  public String handleFudiStatus(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> fudiService.ensureFudiReady(userId), fmt, vo -> formatFudiStatus(vo, fmt));
  }

  public String handleFudiGrid(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> fudiService.ensureFudiReady(userId), fmt, vo -> formatCellLayout(vo, fmt));
  }

  public String handleSpiritChat(String userInput, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> spiritChatService.chatWithSpirit(userId, userInput), fmt, response -> response);
  }

  public String handleTriggerTribulation(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> fudiService.triggerTribulation(userId),
        fmt,
        vo -> vo.tribulationResult() + "\n劫数：" + vo.tribulationStage() + "  连胜×" + vo.winStreak());
  }

  private String formatFudiStatus(FudiStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    if (status.tribulationResult() != null) {
      sb.append(status.tribulationResult()).append("\n\n");
    }

    sb.append(fmt.heading("福地状态", "🏔️"));
    sb.append(
        fmt.listItem("劫数：" + status.tribulationStage() + "  连胜×" + status.tribulationWinStreak()));
    sb.append(
        fmt.listItem(
            "地灵形态：" + (status.spiritFormName() != null ? status.spiritFormName() : "未知形态")));
    if (status.mbtiType() != null) {
      sb.append(fmt.listItem("地灵人格：" + status.mbtiType().getCode()));
    }
    sb.append(fmt.listItem("已占地块：" + status.occupiedCells() + "/" + status.totalCells()));
    sb.append(fmt.separator());
    sb.append(fmt.tip("输入「福地地块」查看详细布局"));
    return sb.toString();
  }

  private String formatCellLayout(FudiStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("福地地块布局", "🗺️"));
    if (status.cellDetails() == null || status.cellDetails().isEmpty()) {
      sb.append("（空地，尚未建造任何地块）\n");
    } else {
      for (var cell : status.cellDetails()) {
        StringBuilder cellLine = new StringBuilder();
        cellLine.append("#").append(cell.cellId()).append(" ");
        cellLine.append(cell.type().getChineseName());
        if (cell.cellLevel() != null && cell.cellLevel() > 1) {
          cellLine.append(" Lv").append(cell.cellLevel());
        }
        if (cell.name() != null) cellLine.append(" - ").append(cell.name());
        if (cell.growthProgress() != null) {
          int percent = (int) (cell.growthProgress() * 100);
          cellLine.append(" [").append(percent).append("%]");
          if (Boolean.TRUE.equals(cell.isMature())) cellLine.append(fmt.bold("可收取"));
        }
        if (cell.quality() != null) cellLine.append(" ").append(cell.quality());
        if (cell.productionStored() != null && cell.productionStored() > 0)
          cellLine.append(" x").append(cell.productionStored());
        if (Boolean.TRUE.equals(cell.isIncubating())) cellLine.append(" 孵化中");
        sb.append(fmt.listItem(cellLine.toString()));
      }
    }
    sb.append(fmt.separator());
    sb.append(fmt.tip("与地灵聊天即可进行种植、建造、收取等操作"));
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "福地";
  }

  @Override
  public String groupSummary() {
    return "福地经营、地灵互动";
  }

  @Override
  public String groupDescription() {
    return "福地经营、地灵互动";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("福地", "查看福地概况", "福地"),
        new CommandEntry("福地地块", "查看福地详细布局", "福地地块"),
        new CommandEntry("福地渡劫", "手动触发天劫", "福地渡劫"),
        new CommandEntry("地灵 「内容」", "与地灵自然语言聊天", "地灵 你好呀"));
  }
}
