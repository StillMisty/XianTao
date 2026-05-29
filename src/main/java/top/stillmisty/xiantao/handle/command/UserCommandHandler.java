package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.player.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandHandler implements CommandGroup {

  private final UserService userService;

  public String handleRegister(
      PlatformType platform, String openId, String nickname, TextFormat fmt) {
    log.debug("处理注册请求 - Platform: {}, OpenId: {}, Nickname: {}", platform, openId, nickname);
    return CommandHandlerHelper.safeCall(
        () -> userService.createUser(platform, openId, nickname),
        fmt,
        result -> {
          if (!result.success()) {
            return result.message() != null ? result.message() : "系统错误：用户创建失败，请联系管理员";
          }
          log.info("玩家注册成功 - UserId: {}, Nickname: {}", result.userId(), result.nickname());
          return "欢迎踏入仙途！您的道号为：" + result.nickname() + "\n输入「状态」查看您的角色信息";
        });
  }

  public String handleChangeNickname(
      PlatformType platform, String openId, String newNickname, TextFormat fmt) {
    log.debug("处理改号 - Platform: {}, OpenId: {}, NewNickname: {}", platform, openId, newNickname);
    return CommandHandlerHelper.safeCall(
        () -> userService.changeNickname(platform, openId, newNickname), fmt, msg -> msg);
  }

  @Override
  public String groupName() {
    return "角色";
  }

  @Override
  public String groupSummary() {
    return "创建角色、更改道号";
  }

  @Override
  public String groupDescription() {
    return "角色注册与改号";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("我要修仙 「道号」", "注册新角色", "我要修仙 张三"),
        new CommandEntry("改号 「新道号」", "更改道号（道号不可与其他玩家重复）", "改号 李四"));
  }
}
