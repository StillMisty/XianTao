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
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;

/** 统一使用物品监听器 支持：使用 [物品名]、使用 [物品名] [参数] */
@Slf4j
@Component
@RequiredArgsConstructor
public class UseItemHandle {

  private final UseItemCommandHandler useItemCommandHandler;

  /** 处理使用物品命令（带参数） 格式：使用 [物品名] [参数] 示例：使用 进化石 1 */
  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}} {{args}}")
  public void useItemWithArgs(
      MessageEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    log.debug(
        "收到使用物品请求 - AuthorId: {}, ItemName: {}, Args: {}", event.getAuthorId(), itemName, args);
    String response =
        useItemCommandHandler.handleUseItem(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName, args);
    event.replyBlocking(response);
  }

  /** 处理使用物品命令（无参数） 格式：使用 [物品名] 示例：使用 天元丹 */
  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}}")
  public void useItem(MessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到使用物品请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        useItemCommandHandler.handleUseItem(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName, null);
    event.replyBlocking(response);
  }
}
