package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

/** 物品管理监听器 处理背包、装备、丢弃相关命令 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemHandle {

  private final CultivationCommandHandler commandHandler;

  /** 处理背包查询命令 */
  @Listener
  @ContentTrim
  @Filter("背包")
  public void inventory(MessageEvent event) {
    log.debug("收到背包查询请求 - AuthorId: {}", event.getAuthorId());

    String response =
        commandHandler.handleInventory(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());

    event.replyBlocking(response);
  }

  /** 处理背包分类查询命令 格式：背包 [分类] 分类可选：种子、装备、兽卵 */
  @Listener
  @ContentTrim
  @Filter("背包 {{category}}")
  public void inventoryByCategory(MessageEvent event, @FilterValue("category") String category) {
    String response =
        commandHandler.handleInventoryByCategory(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), category);
    event.replyBlocking(response);
  }

  /** 处理装备穿戴命令 格式：装备 [物品名/编号] */
  @Listener
  @ContentTrim
  @Filter("装备 {{itemName}}")
  public void equip(MessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到装备穿戴请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);

    String response =
        commandHandler.handleEquip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);

    event.replyBlocking(response);
  }

  /** 处理装备卸下命令 格式：卸下 [部位] */
  @Listener
  @ContentTrim
  @Filter("卸下 {{slotName}}")
  public void unequip(MessageEvent event, @FilterValue("slotName") String slotName) {
    log.debug("收到装备卸下请求 - AuthorId: {}, SlotName: {}", event.getAuthorId(), slotName);

    String response =
        commandHandler.handleUnequip(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), slotName);

    event.replyBlocking(response);
  }

  /** 处理丢弃命令 格式：丢弃 [物品名/编号] */
  @Listener
  @ContentTrim
  @Filter("丢弃 {{itemName}}")
  public void discard(MessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到丢弃请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        commandHandler.handleDiscard(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
    event.replyBlocking(response);
  }
}
