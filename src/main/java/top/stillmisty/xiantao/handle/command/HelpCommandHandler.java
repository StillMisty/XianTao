package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.HelpService;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler {

  private final HelpService helpService;

  public String handleHelp(PlatformType platform, String openId, String command) {
    if (command == null || command.isBlank()) {
      return formatAllGroups();
    }
    return helpService
        .findByTrigger(command)
        .map(this::formatCommandDetail)
        .orElseGet(
            () -> {
              List<CommandEntry> results = helpService.search(command);
              if (results.isEmpty()) {
                return "未找到命令：" + command;
              }
              if (results.size() == 1) {
                return formatCommandDetail(results.getFirst());
              }
              return formatSearchResults(command, results);
            });
  }

  private String formatAllGroups() {
    StringBuilder sb = new StringBuilder("【修仙指令帮助】\n");
    for (CommandGroup group : helpService.getAllGroups()) {
      sb.append("\n≡ ").append(group.groupName()).append(" ≡");
      if (!group.groupDescription().isBlank()) {
        sb.append(" ").append(group.groupDescription());
      }
      sb.append("\n");
      for (CommandEntry cmd : group.commands()) {
        sb.append("  ")
            .append(cmd.trigger())
            .append("  —  ")
            .append(cmd.description())
            .append("\n");
      }
    }
    sb.append("\n输入「帮助 [命令]」查看详细用法");
    return sb.toString();
  }

  private String formatCommandDetail(CommandEntry cmd) {
    return "【" + cmd.trigger() + "】\n" + cmd.description() + "\n" + "用法：" + cmd.usage();
  }

  private String formatSearchResults(String keyword, List<CommandEntry> results) {
    StringBuilder sb = new StringBuilder("搜索「" + keyword + "」找到以下命令：\n");
    for (CommandEntry e : results) {
      sb.append("  ").append(e.trigger()).append("  —  ").append(e.description()).append("\n");
    }
    return sb.toString();
  }

  public String handleHelpMarkdown(PlatformType platform, String openId, String command) {
    if (command == null || command.isBlank()) {
      return formatAllGroupsMarkdown();
    }
    return helpService
        .findByTrigger(command)
        .map(this::formatCommandDetailMarkdown)
        .orElseGet(
            () -> {
              List<CommandEntry> results = helpService.search(command);
              if (results.isEmpty()) {
                return "未找到命令：" + command;
              }
              if (results.size() == 1) {
                return formatCommandDetailMarkdown(results.getFirst());
              }
              return formatSearchResultsMarkdown(command, results);
            });
  }

  private String formatAllGroupsMarkdown() {
    StringBuilder sb = new StringBuilder("## 修仙指令帮助\n");
    for (CommandGroup group : helpService.getAllGroups()) {
      sb.append("\n### ").append(group.groupName());
      if (!group.groupDescription().isBlank()) {
        sb.append(" — ").append(group.groupDescription());
      }
      sb.append("\n");
      for (CommandEntry cmd : group.commands()) {
        sb.append("- **")
            .append(cmd.trigger())
            .append("** — ")
            .append(cmd.description())
            .append("\n");
      }
    }
    sb.append("\n💡 输入「帮助 [命令]」查看详细用法");
    return sb.toString();
  }

  private String formatCommandDetailMarkdown(CommandEntry cmd) {
    return "## " + cmd.trigger() + "\n" + cmd.description() + "\n" + "用法：" + cmd.usage();
  }

  private String formatSearchResultsMarkdown(String keyword, List<CommandEntry> results) {
    StringBuilder sb = new StringBuilder("搜索 **").append(keyword).append("** 找到以下命令：\n");
    for (CommandEntry e : results) {
      sb.append("- **").append(e.trigger()).append("** — ").append(e.description()).append("\n");
    }
    return sb.toString();
  }
}
