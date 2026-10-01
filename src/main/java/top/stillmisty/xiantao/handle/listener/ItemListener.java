package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.InventoryCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class ItemListener {

  private final InventoryCommandHandler inventoryCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("背包")
  public void inventory(QqIncomingMessage event) {
    replyHelper.dispatch(event, "背包查询", inventoryCommandHandler::handleInventory);
  }

  @RequireAuth
  @Command("背包\\s*{{category}}")
  public void inventoryByCategory(QqIncomingMessage event, @Arg("category") String category) {
    replyHelper.dispatch(
        event, "背包分类", category, inventoryCommandHandler::handleInventoryByCategory);
  }

  @RequireAuth
  @Command("装备\\s*{{itemName}}")
  public void equip(QqIncomingMessage event, @Arg("itemName") String itemName) {
    replyHelper.dispatch(event, "装备穿戴", itemName, inventoryCommandHandler::handleEquip);
  }

  @RequireAuth
  @Command("卸下\\s*{{slotName}}")
  public void unequip(QqIncomingMessage event, @Arg("slotName") String slotName) {
    replyHelper.dispatch(event, "装备卸下", slotName, inventoryCommandHandler::handleUnequip);
  }

  @RequireAuth
  @Command("丢弃\\s*{{itemName}}")
  public void discard(QqIncomingMessage event, @Arg("itemName") String itemName) {
    replyHelper.dispatch(event, "丢弃", itemName, inventoryCommandHandler::handleDiscard);
  }
}
