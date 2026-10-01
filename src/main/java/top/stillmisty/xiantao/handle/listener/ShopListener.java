package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.ShopCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class ShopListener {

  private final ShopCommandHandler shopCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("掌柜\\s*{{content}}")
  public void shopkeeper(QqIncomingMessage event, @Arg("content") String content) {
    replyHelper.dispatch(event, "掌柜", content, shopCommandHandler::handleShopkeeper);
  }

  @RequireAuth
  @Command("回收\\s*{{itemName}}")
  public void quickSell(QqIncomingMessage event, @Arg("itemName") String itemName) {
    replyHelper.dispatch(event, "回收", itemName, shopCommandHandler::handleQuickSell);
  }
}
