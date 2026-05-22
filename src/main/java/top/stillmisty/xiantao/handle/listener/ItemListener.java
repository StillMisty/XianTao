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
    log.debug("[OneBot] 收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, commandHandler::handleInventory);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategory(
      OneBotMessageEvent event, @FilterValue("category") String category) {
    replyHelper.oneBot(event, category, commandHandler::handleInventoryByCategory);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equip(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[OneBot] 收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.oneBot(event, itemName, commandHandler::handleEquip);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequip(OneBotMessageEvent event, @FilterValue("slotName") String slotName) {
    log.debug("[OneBot] 收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    replyHelper.oneBot(event, slotName, commandHandler::handleUnequip);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discard(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[OneBot] 收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.oneBot(event, itemName, commandHandler::handleDiscard);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventoryQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到背包查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, commandHandler::handleInventory);
  }

  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategoryQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("category") String category) {
    replyHelper.qq(event, category, commandHandler::handleInventoryByCategory);
  }

  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equipQq(QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[QQ] 收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.qq(event, itemName, commandHandler::handleEquip);
  }

  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequipQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("slotName") String slotName) {
    log.debug("[QQ] 收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);
    replyHelper.qq(event, slotName, commandHandler::handleUnequip);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discardQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[QQ] 收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.qq(event, itemName, commandHandler::handleDiscard);
  }
}
