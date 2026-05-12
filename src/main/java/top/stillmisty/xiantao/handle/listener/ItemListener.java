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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemListener {

  private final CultivationCommandHandler commandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventory(OneBotMessageEvent event) {
    log.debug("收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        commandHandler.handleInventory(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategory(
      OneBotMessageEvent event, @FilterValue("category") String category) {
    String response =
        commandHandler.handleInventoryByCategory(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), category);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equip(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleEquip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequip(OneBotMessageEvent event, @FilterValue("slotName") String slotName) {
    log.debug("收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    String response =
        commandHandler.handleUnequip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), slotName);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discard(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleDiscard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventoryQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        commandHandler.handleInventoryMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategoryQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("category") String category) {
    String response =
        commandHandler.handleInventoryByCategoryMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), category);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equipQq(QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleEquipMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequipQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("slotName") String slotName) {
    log.debug("收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    String response =
        commandHandler.handleUnequipMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), slotName);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discardQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleDiscardMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    replyHelper.replyQQ(event, response);
  }
}
