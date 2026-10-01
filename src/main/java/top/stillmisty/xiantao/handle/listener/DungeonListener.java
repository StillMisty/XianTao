package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.DungeonCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class DungeonListener {

  private final DungeonCommandHandler dungeonCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("秘境")
  public void dungeonOrStatus(QqIncomingMessage event) {
    replyHelper.dispatch(event, "秘境/状态", dungeonCommandHandler::handleDungeonOrStatus);
  }

  @RequireAuth
  @Command("秘境\\s*{{dungeonName}}")
  public void dungeonEnter(QqIncomingMessage event, @Arg("dungeonName") String dungeonName) {
    replyHelper.dispatch(event, "进入秘境", dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  @RequireAuth
  @Command("秘灵")
  public void dungeonChat(QqIncomingMessage event) {
    replyHelper.dispatch(event, "秘灵对话", dungeonCommandHandler::handleCreatureHelp);
  }

  @RequireAuth
  @Command("秘灵\\s*{{content}}")
  public void dungeonChatWithContent(QqIncomingMessage event, @Arg("content") String content) {
    replyHelper.dispatch(event, "秘灵对话", content, dungeonCommandHandler::handleCreatureChat);
  }
}
