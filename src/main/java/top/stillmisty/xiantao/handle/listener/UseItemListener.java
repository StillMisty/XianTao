package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class UseItemListener {

  private final UseItemCommandHandler useItemCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("使用\\s*{{itemName,\\S+}}\\s+{{args,.+}}")
  public void useItemWithArgs(
      QqIncomingMessage event, @Arg("itemName") String itemName, @Arg("args") String args) {
    replyHelper.dispatch(
        event, "使用物品", fmt -> useItemCommandHandler.handleUseItem(itemName, args, fmt));
  }

  @RequireAuth
  @Command("使用\\s*{{itemName,\\S+}}")
  public void useItem(QqIncomingMessage event, @Arg("itemName") String itemName) {
    replyHelper.dispatch(
        event, "使用物品", fmt -> useItemCommandHandler.handleUseItem(itemName, "", fmt));
  }
}
