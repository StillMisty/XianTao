package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class UseItemHandle {

  private final UseItemCommandHandler useItemCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}} {{args}}")
  public void useItemWithArgs(
      OneBotMessageEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    log.debug(
        "收到使用物品请求 - AuthorId: {}, ItemName: {}, Args: {}", event.getAuthorId(), itemName, args);
    String response =
        useItemCommandHandler.handleUseItem(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName, args);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}}")
  public void useItem(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到使用物品请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        useItemCommandHandler.handleUseItem(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName, null);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(OneBotMessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
