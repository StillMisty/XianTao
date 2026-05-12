package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.PvpService;
import top.stillmisty.xiantao.service.ServiceResult;

@Component
@RequiredArgsConstructor
public class PvpCommandHandler implements CommandGroup {

  private final PvpService pvpService;

  // ===================== 委托方法（纯文本） =====================

  public String handleSpar(PlatformType platform, String openId, String targetNickname) {
    return handleSpar(platform, openId, targetNickname, TextFormat.PLAIN);
  }

  // ===================== 委托方法（Markdown） =====================

  public String handleSparMarkdown(PlatformType platform, String openId, String targetNickname) {
    return handleSpar(platform, openId, targetNickname, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleSpar(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return switch (pvpService.spar(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  @Override
  public String groupName() {
    return "切磋";
  }

  @Override
  public String groupDescription() {
    return "玩家对战";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("切磋 {{道号}}", "与其他玩家切磋对战", "切磋 张三"));
  }
}
