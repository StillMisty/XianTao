package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.ShopCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class ShopListener {

  private final ShopCommandHandler shopCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("掌柜\\s*{{content}}")
  public void shopkeeper(MessageEvent event, @FilterValue("content") String content) {
    replyHelper.dispatch(event, "掌柜", content, shopCommandHandler::handleShopkeeper);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("回收\\s*{{itemName}}")
  public void quickSell(MessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.dispatch(event, "回收", itemName, shopCommandHandler::handleQuickSell);
  }
}
