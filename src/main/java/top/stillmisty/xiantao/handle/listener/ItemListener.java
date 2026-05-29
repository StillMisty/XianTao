package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.InventoryCommandHandler;

@Component
@RequiredArgsConstructor
public class ItemListener {

  private final InventoryCommandHandler inventoryCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventory(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "背包查询", inventoryCommandHandler::handleInventory);
  }

  @Listener
  @ContentTrim
  @Filter("背包\\s*{{category}}")
  public void inventoryByCategory(
      OneBotMessageEvent event, @FilterValue("category") String category) {
    replyHelper.oneBot(event, "背包分类", category, inventoryCommandHandler::handleInventoryByCategory);
  }

  @Listener
  @ContentTrim
  @Filter("装备\\s*{{itemName}}")
  public void equip(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.oneBot(event, "装备穿戴", itemName, inventoryCommandHandler::handleEquip);
  }

  @Listener
  @ContentTrim
  @Filter("卸下\\s*{{slotName}}")
  public void unequip(OneBotMessageEvent event, @FilterValue("slotName") String slotName) {
    replyHelper.oneBot(event, "装备卸下", slotName, inventoryCommandHandler::handleUnequip);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃\\s*{{itemName}}")
  public void discard(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.oneBot(event, "丢弃", itemName, inventoryCommandHandler::handleDiscard);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventoryQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "背包查询", inventoryCommandHandler::handleInventory);
  }

  @Listener
  @ContentTrim
  @Filter("背包\\s*{{category}}")
  public void inventoryByCategoryQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("category") String category) {
    replyHelper.qq(event, "背包分类", category, inventoryCommandHandler::handleInventoryByCategory);
  }

  @Listener
  @ContentTrim
  @Filter("装备\\s*{{itemName}}")
  public void equipQq(QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.qq(event, "装备穿戴", itemName, inventoryCommandHandler::handleEquip);
  }

  @Listener
  @ContentTrim
  @Filter("卸下\\s*{{slotName}}")
  public void unequipQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("slotName") String slotName) {
    replyHelper.qq(event, "装备卸下", slotName, inventoryCommandHandler::handleUnequip);
  }

  @Listener
  @ContentTrim
  @Filter("丢弃\\s*{{itemName}}")
  public void discardQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.qq(event, "丢弃", itemName, inventoryCommandHandler::handleDiscard);
  }
}
