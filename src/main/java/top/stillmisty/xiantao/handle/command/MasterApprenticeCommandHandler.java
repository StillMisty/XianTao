package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;

@Component
@RequiredArgsConstructor
public class MasterApprenticeCommandHandler implements CommandGroup {

  private final MasterApprenticeService masterApprenticeService;

  public String handleRequestMentor(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> masterApprenticeService.requestMentor(userId, targetNickname),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleRequestApprentice(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> masterApprenticeService.requestApprentice(userId, targetNickname),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleStatus(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> masterApprenticeService.getStatus(userId),
        fmt,
        info -> formatStatus(info, fmt),
        msg -> "操作失败: " + msg);
  }

  public String handleDismiss(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> masterApprenticeService.dismissApprentice(userId, targetNickname),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleRenounce(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> masterApprenticeService.renounceMaster(userId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  private String formatStatus(
      top.stillmisty.xiantao.domain.masterapprentice.vo.MasterApprenticeInfoVO info,
      TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    if (info.hasMaster()) {
      sb.append(fmt.heading("师门信息", "🎓"));
      sb.append(
          fmt.listItem(
              "师傅: "
                  + fmt.bold(info.masterName() != null ? info.masterName() : "未知")
                  + " (Lv."
                  + info.masterLevel()
                  + " "
                  + info.masterRealmDisplay()
                  + ")"));
      sb.append(fmt.listItem("状态: " + info.status()));
    } else {
      sb.append("你尚未拜师，逍遥自在。\n");
    }

    if (info.apprenticeCount() > 0) {
      sb.append(fmt.separator());
      sb.append(fmt.heading("我的徒弟 (" + info.apprenticeCount() + "人)", "👥"));
      for (var apprentice : info.apprentices()) {
        sb.append(
            fmt.listItem(
                fmt.bold(apprentice.nickname())
                    + " (Lv."
                    + apprentice.level()
                    + " "
                    + apprentice.realmDisplay()
                    + ") "
                    + apprentice.status()));
      }
    }

    return sb.toString();
  }

  @Override
  public String groupName() {
    return "师徒";
  }

  @Override
  public String groupSummary() {
    return "拜师收徒、传承关系";
  }

  @Override
  public String groupDescription() {
    return "拜师、收徒、查看师徒关系";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("拜师 「道号」", "向目标发出拜师请求", "拜师 张三"),
        new CommandEntry("收徒 「道号」", "向目标发出收徒邀请", "收徒 李四"),
        new CommandEntry("师徒", "查看师徒关系信息", "师徒"),
        new CommandEntry("逐出 「道号」", "师傅将徒弟逐出师门", "逐出师门 李四"),
        new CommandEntry("叛师", "徒弟叛离师门", "叛师"));
  }
}
