package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.sect.vo.SectOverviewVO;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.SectSpiritChatService;
import top.stillmisty.xiantao.service.sect.SectMemberService;

@Component
@RequiredArgsConstructor
public class SectCommandHandler implements CommandGroup {

  private final SectMemberService sectMemberService;
  private final SectSpiritChatService sectSpiritChatService;

  public String handleOverview(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.getSectOverview(userId), fmt, vo -> formatSectOverview(vo, fmt));
  }

  public String handleCreate(String name, String ethosDesc, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () ->
            (ethosDesc != null && !ethosDesc.isBlank())
                ? sectMemberService.createSectWithEthos(userId, name, ethosDesc)
                : sectMemberService.createSect(userId, name),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleLeave(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.leaveSect(userId), fmt, text -> text, msg -> "操作失败: " + msg);
  }

  public String handleDismiss(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.dismissSect(userId), fmt, text -> text, msg -> "操作失败: " + msg);
  }

  public String handleSectSpiritChat(String userInput, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> sectSpiritChatService.chatWithSectSpirit(userId, userInput),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  private String formatSectOverview(SectOverviewVO vo, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading(vo.name(), null));
    if (vo.verse() != null && !vo.verse().isBlank()) {
      sb.append("「").append(vo.verse()).append("」\n");
    }
    sb.append(fmt.separator());
    sb.append("等级: Lv.").append(vo.level()).append("\n");
    sb.append("宗主: ").append(vo.leaderNickname()).append("\n");
    sb.append("成员: ").append(vo.memberCount()).append("/").append(vo.maxMembers()).append("\n");
    sb.append("资金: ").append(vo.funds()).append(" 灵石\n");
    sb.append("我的贡献: ").append(vo.myContribution()).append("\n");
    sb.append("我的职位: ").append(vo.myPosition()).append("\n");
    if (vo.description() != null && !vo.description().isBlank()) {
      sb.append("简介: ").append(vo.description()).append("\n");
    }
    if (vo.notice() != null && !vo.notice().isBlank()) {
      sb.append("公告: ").append(vo.notice()).append("\n");
    }
    if (vo.currentEvent() != null && !vo.currentEvent().isBlank()) {
      sb.append("当前事件: ").append(vo.currentEvent()).append("\n");
    }
    sb.append("\n成员列表:\n");
    for (var m : vo.members()) {
      sb.append("  ").append(m.positionName()).append(" ").append(m.nickname());
      sb.append(" (Lv.").append(m.level()).append(")");
      if (m.isMe()) sb.append(" [我]");
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "宗门";
  }

  @Override
  public String groupSummary() {
    return "创建宗门、宗灵对话";
  }

  @Override
  public String groupDescription() {
    return "宗门创建、宗灵对话、退出解散";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("宗门", "查看宗门总览（名称、等级、成员、资金、公告、事件）", "宗门"),
        new CommandEntry("宗门创建 「名称」 「道统描述?」", "创建宗门（金丹期+5000灵石）", "宗门创建 青云宗 以剑入道"),
        new CommandEntry("宗灵 「内容」", "与宗灵对话，执行所有宗门事务", "宗灵 我想捐献灵石"),
        new CommandEntry("宗门退出", "退出宗门（贡献清零，遗忘共享功法，24h冷却）", "宗门退出"),
        new CommandEntry("宗门解散", "解散宗门（宗主专用）", "宗门解散"));
  }
}
