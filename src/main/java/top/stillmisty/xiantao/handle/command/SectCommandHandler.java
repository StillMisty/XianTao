package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ai.SectSpiritChatService;
import top.stillmisty.xiantao.service.sect.SectMemberService;

@Component
@RequiredArgsConstructor
public class SectCommandHandler implements CommandGroup {

  private final SectMemberService sectMemberService;
  private final SectSpiritChatService sectSpiritChatService;

  public String handleOverview(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.getSectOverview(platform, openId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleCreate(
      PlatformType platform, String openId, String name, String ethosDesc, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () ->
            (ethosDesc != null && !ethosDesc.isBlank())
                ? sectMemberService.createSectWithEthos(platform, openId, name, ethosDesc)
                : sectMemberService.createSect(platform, openId, name),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleLeave(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.leaveSect(platform, openId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleDismiss(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> sectMemberService.dismissSect(platform, openId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleSectSpiritChat(
      PlatformType platform, String openId, String userInput, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> sectSpiritChatService.chatWithSectSpirit(platform, openId, userInput),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  @Override
  public String groupName() {
    return "宗门";
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
