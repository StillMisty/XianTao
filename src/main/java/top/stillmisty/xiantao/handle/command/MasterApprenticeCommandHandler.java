package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;

@Component
@RequiredArgsConstructor
public class MasterApprenticeCommandHandler implements CommandGroup {

  private final MasterApprenticeService masterApprenticeService;

  public String handleRequestMentor(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = masterApprenticeService.requestMentor(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleRequestApprentice(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = masterApprenticeService.requestApprentice(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleStatus(PlatformType platform, String openId, TextFormat fmt) {
    var result = masterApprenticeService.getStatus(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var info) -> formatStatus(info, fmt);
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleDismiss(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = masterApprenticeService.dismissApprentice(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleRenounce(PlatformType platform, String openId, TextFormat fmt) {
    var result = masterApprenticeService.renounceMaster(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
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
                  + fmt.bold(info.masterName())
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
