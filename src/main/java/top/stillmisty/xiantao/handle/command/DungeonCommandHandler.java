package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.DungeonChatService;
import top.stillmisty.xiantao.service.dungeon.DungeonService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DungeonCommandHandler implements CommandGroup {

  private final DungeonService dungeonService;
  private final DungeonChatService dungeonChatService;

  public String handleDungeonOrStatus(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();

    var user = tryGetUserStatus(userId);
    if (user != null) {
      return CommandHandlerHelper.safeCall(
          () -> dungeonService.statusInDungeon(userId),
          fmt,
          status -> fmt.heading("秘境进度", "") + status + "\n\n" + fmt.tip("输入「秘灵 内容」与秘境之灵/叙事者对话"));
    }

    log.debug("处理秘境列表 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.listDungeons(userId), fmt, vo -> formatDungeonList(vo, fmt));
  }

  public String handleDungeonEnter(String dungeonName, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理进入秘境 - UserId: {}, Dungeon: {}", userId, dungeonName);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.enterDungeon(userId, dungeonName),
        fmt,
        msg -> msg + "\n\n" + fmt.tip("输入「秘灵 内容」开始探索和对话"));
  }

  public String handleCreatureHelp(TextFormat fmt) {
    return fmt.tip("输入「秘灵 你说的话」与秘境之灵/叙事者对话") + "\n" + fmt.tip("例如：秘灵 我要探索那块石碑");
  }

  public String handleCreatureChat(String content, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理秘灵对话 - UserId: {}, content: {}", userId, content);
    return CommandHandlerHelper.safeCall(
        () -> dungeonChatService.chatWithDungeon(userId, content), fmt, msg -> msg);
  }

  @SuppressWarnings("NullAway")
  private String tryGetUserStatus(Long userId) {
    try {
      var result = dungeonService.statusInDungeon(userId);
      return switch (result) {
        case top.stillmisty.xiantao.service.ServiceResult.Success<String> s -> s.data();
        case top.stillmisty.xiantao.service.ServiceResult.Failure<String> ignored -> null;
      };
    } catch (Exception e) {
      return null;
    }
  }

  private String formatDungeonList(List<DungeonListVO> dungeons, TextFormat fmt) {
    if (dungeons.isEmpty()) {
      return "暂无开放的秘境。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("秘境列表", ""));
    sb.append(fmt.separator());

    for (int i = 0; i < dungeons.size(); i++) {
      DungeonListVO d = dungeons.get(i);
      sb.append(fmt.bold((i + 1) + ". " + d.name()));

      String elementTag = "";
      if (d.elementName() != null && !d.elementName().isBlank()) {
        elementTag = " " + d.elementName();
      }
      sb.append(elementTag).append("\n");

      sb.append(fmt.listItem("境界：" + d.minLevel() + "-" + d.maxLevel()));
      sb.append(fmt.listItem("队伍上限：" + d.maxTeamSize() + "人"));

      if (d.hasActiveInstance()) {
        sb.append(fmt.listItem("状态：进行中"));
      } else if (d.firstClear()) {
        sb.append(fmt.listItem("奖励：" + d.rewardCount() + "/" + d.dailyLimit() + " · 已首通"));
      } else {
        sb.append(fmt.listItem("奖励：" + d.rewardCount() + "/" + d.dailyLimit()));
      }
      sb.append("\n");
    }

    sb.append(fmt.separator());
    sb.append(fmt.tip("输入「秘境 秘境名」进入秘境"));
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "秘境";
  }

  @Override
  public String groupSummary() {
    return "秘境探索与秘灵对话";
  }

  @Override
  public String groupDescription() {
    return "秘境探索、通关、奖励";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("秘境", "查看秘境列表与当前进度", "秘境"),
        new CommandEntry("秘境 「名」", "进入指定秘境", "秘境 紫府秘境"),
        new CommandEntry("秘灵 「内容」", "与秘境之灵/叙事者对话", "秘灵 探索石碑"));
  }
}
