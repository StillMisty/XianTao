package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.HelpService;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler {

  private final HelpService helpService;

  public String handleHelp(PlatformType platform, String openId, String command, TextFormat fmt) {
    if (command == null || command.isBlank()) {
      return formatAllGroups(fmt);
    }
    return helpService
        .findByGroupName(command)
        .map(group -> formatGroupDetail(group, fmt))
        .orElseGet(
            () ->
                helpService
                    .findByTrigger(command)
                    .map(cmd -> formatCommandDetail(cmd, fmt))
                    .orElseGet(
                        () -> {
                          List<CommandEntry> results = helpService.search(command);
                          if (results.isEmpty()) {
                            return "未找到命令或子系统：" + command;
                          }
                          if (results.size() == 1) {
                            return formatCommandDetail(results.getFirst(), fmt);
                          }
                          return formatSearchResults(command, results, fmt);
                        }));
  }

  private String formatAllGroups(TextFormat fmt) {
    StringBuilder sb = new StringBuilder(fmt.subHeading("修仙指令帮助"));
    for (CommandGroup group : helpService.getAllGroups()) {
      sb.append(fmt.listItem(fmt.bold(group.groupName()) + " — " + group.groupSummary()));
    }
    sb.append(fmt.separator());
    sb.append(fmt.tip("输入「帮助 [子系统]」查看详细玩法，如「帮助 修炼」"));
    return sb.toString();
  }

  private String formatGroupDetail(CommandGroup group, TextFormat fmt) {
    StringBuilder sb = new StringBuilder(fmt.heading(group.groupName()));
    if (!group.groupDescription().isBlank()) {
      sb.append(group.groupDescription()).append("\n");
    }
    sb.append(fmt.separator());
    for (CommandEntry cmd : group.commands()) {
      sb.append(fmt.listItem(fmt.bold(cmd.trigger()) + " — " + cmd.description()));
    }
    sb.append(fmt.tip("输入「帮助 [命令]」查看单条命令用法，如「帮助 突破」"));
    return sb.toString();
  }

  private String formatCommandDetail(CommandEntry cmd, TextFormat fmt) {
    return fmt.subHeading(cmd.trigger()) + cmd.description() + "\n" + "用法：" + cmd.usage();
  }

  private String formatSearchResults(String keyword, List<CommandEntry> results, TextFormat fmt) {
    StringBuilder sb = new StringBuilder("搜索");
    sb.append(fmt.bold(keyword));
    sb.append("找到以下命令：\n");
    for (CommandEntry e : results) {
      sb.append(fmt.listItem(fmt.bold(e.trigger()) + " — " + e.description()));
    }
    return sb.toString();
  }
}
