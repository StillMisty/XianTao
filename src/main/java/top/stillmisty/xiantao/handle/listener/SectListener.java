package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.SectCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class SectListener {

  private final SectCommandHandler sectCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("宗门")
  public void overview(QqIncomingMessage event) {
    replyHelper.dispatch(event, "宗门", sectCommandHandler::handleOverview);
  }

  @RequireAuth
  @Command("宗门创建\\s*{{name,\\S+}}\\s+{{ethosDesc,.+?}}")
  public void createWithEthos(
      QqIncomingMessage event, @Arg("name") String name, @Arg("ethosDesc") String ethosDesc) {
    replyHelper.dispatch(
        event, "宗门创建", fmt -> sectCommandHandler.handleCreate(name, ethosDesc, fmt));
  }

  @RequireAuth
  @Command("宗门创建\\s*{{name,\\S+}}")
  public void create(QqIncomingMessage event, @Arg("name") String name) {
    replyHelper.dispatch(event, "宗门创建", fmt -> sectCommandHandler.handleCreate(name, "", fmt));
  }

  @RequireAuth
  @Command("宗灵\\s*{{content,.+}}")
  public void sectSpirit(QqIncomingMessage event, @Arg("content") String content) {
    replyHelper.dispatch(event, "宗灵对话", content, sectCommandHandler::handleSectSpiritChat);
  }

  @RequireAuth
  @Command("宗门退出")
  public void leave(QqIncomingMessage event) {
    replyHelper.dispatch(event, "宗门退出", sectCommandHandler::handleLeave);
  }

  @RequireAuth
  @Command("宗门解散")
  public void dismiss(QqIncomingMessage event) {
    replyHelper.dispatch(event, "宗门解散", sectCommandHandler::handleDismiss);
  }
}
