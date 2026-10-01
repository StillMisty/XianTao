package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class FudiListener {

  private final FudiCommandHandler fudiCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("福地")
  public void handleFudi(QqIncomingMessage event) {
    replyHelper.dispatch(event, "福地", fudiCommandHandler::handleFudiStatus);
  }

  @RequireAuth
  @Command("福地地块")
  public void handleFudiGrid(QqIncomingMessage event) {
    replyHelper.dispatch(event, "福地地块", fudiCommandHandler::handleFudiGrid);
  }

  @RequireAuth
  @Command("地灵\\s*{{content}}")
  public void handleFudiSpirit(QqIncomingMessage event, @Arg("content") String content) {
    replyHelper.dispatch(event, "地灵对话", content, fudiCommandHandler::handleSpiritChat);
  }

  @RequireAuth
  @Command("福地渡劫")
  public void handleFudiTribulation(QqIncomingMessage event) {
    replyHelper.dispatch(event, "福地渡劫", fudiCommandHandler::handleTriggerTribulation);
  }
}
