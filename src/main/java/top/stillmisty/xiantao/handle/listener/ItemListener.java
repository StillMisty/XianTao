package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.InventoryCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class ItemListener {

  private final InventoryCommandHandler inventoryCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "背包")
  public void inventory(MessageEvent event) {
    replyHelper.dispatch(event, "背包查询", inventoryCommandHandler::handleInventory);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "背包\\s*{{category}}")
  public void inventoryByCategory(MessageEvent event, @FilterValue("category") String category) {
    replyHelper.dispatch(
        event, "背包分类", category, inventoryCommandHandler::handleInventoryByCategory);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "装备\\s*{{itemName}}")
  public void equip(MessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.dispatch(event, "装备穿戴", itemName, inventoryCommandHandler::handleEquip);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "卸下\\s*{{slotName}}")
  public void unequip(MessageEvent event, @FilterValue("slotName") String slotName) {
    replyHelper.dispatch(event, "装备卸下", slotName, inventoryCommandHandler::handleUnequip);
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "丢弃\\s*{{itemName}}")
  public void discard(MessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.dispatch(event, "丢弃", itemName, inventoryCommandHandler::handleDiscard);
  }
}
