package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.ShopCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopListener {

  private final ShopCommandHandler shopCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("掌柜 {{content}}")
  public void shopkeeper(OneBotMessageEvent event, @FilterValue("content") String content) {
    replyHelper.oneBot(event, content, shopCommandHandler::handleShopkeeper);
  }

  @Listener
  @ContentTrim
  @Filter("回收 {{itemName}}")
  public void quickSell(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.oneBot(event, itemName, shopCommandHandler::handleQuickSell);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("掌柜 {{content}}")
  public void shopkeeperQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    replyHelper.qq(event, content, shopCommandHandler::handleShopkeeper);
  }

  @Listener
  @ContentTrim
  @Filter("回收 {{itemName}}")
  public void quickSellQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.qq(event, itemName, shopCommandHandler::handleQuickSell);
  }
}
