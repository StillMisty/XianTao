package top.stillmisty.xiantao.handle.qq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
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
public class QQItemHandle {

  private final CultivationCommandHandler commandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventory(QGGroupAtMessageCreateEvent event) {
    log.debug("收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        commandHandler.handleInventoryMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategory(
      QGGroupAtMessageCreateEvent event, @FilterValue("category") String category) {
    String response =
        commandHandler.handleInventoryByCategoryMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), category);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equip(QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleEquipMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequip(QGGroupAtMessageCreateEvent event, @FilterValue("slotName") String slotName) {
    log.debug("收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    String response =
        commandHandler.handleUnequipMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), slotName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discard(QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleDiscardMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
