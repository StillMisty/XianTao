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
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemHandle {

  private final CultivationCommandHandler commandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventory(OneBotMessageEvent event) {
    log.debug("收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        commandHandler.handleInventory(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategory(
      OneBotMessageEvent event, @FilterValue("category") String category) {
    String response =
        commandHandler.handleInventoryByCategory(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), category);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equip(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleEquip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequip(OneBotMessageEvent event, @FilterValue("slotName") String slotName) {
    log.debug("收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    String response =
        commandHandler.handleUnequip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), slotName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discard(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleDiscard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
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
