package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.MasterApprenticeCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class MasterApprenticeListener {

  private final MasterApprenticeCommandHandler masterApprenticeCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "拜师\\s*{{targetNickname,\\S+}}")
  public void requestMentor(
      MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "拜师", targetNickname, masterApprenticeCommandHandler::handleRequestMentor);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "收徒\\s*{{targetNickname,\\S+}}")
  public void requestApprentice(
      MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "收徒", targetNickname, masterApprenticeCommandHandler::handleRequestApprentice);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "师徒")
  public void status(MessageEvent event) {
    replyHelper.dispatch(event, "师徒", masterApprenticeCommandHandler::handleStatus);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "逐出师门\\s*{{targetNickname,\\S+}}")
  public void dismiss(MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "逐出师门", targetNickname, masterApprenticeCommandHandler::handleDismiss);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "叛师")
  public void renounce(MessageEvent event) {
    replyHelper.dispatch(event, "叛师", masterApprenticeCommandHandler::handleRenounce);
  }
}
