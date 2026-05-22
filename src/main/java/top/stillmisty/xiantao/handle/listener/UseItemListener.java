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
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class UseItemListener {

  private final UseItemCommandHandler useItemCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}} {{args}}")
  public void useItemWithArgs(
      OneBotMessageEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    log.debug(
        "[OneBot] 收到使用物品请求 - AuthorId: {}, ItemName: {}, Args: {}",
        event.getAuthorId(),
        itemName,
        args);
    replyHelper.oneBot(
        event, (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, args, f));
  }

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}}")
  public void useItem(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[OneBot] 收到使用物品请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.oneBot(
        event, (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, null, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}} {{args}}")
  public void useItemWithArgsQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    log.debug(
        "[QQ] 收到使用物品请求 - AuthorId: {}, ItemName: {}, Args: {}",
        event.getAuthorId(),
        itemName,
        args);
    replyHelper.qq(
        event, (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, args, f));
  }

  @Listener
  @ContentTrim
  @Filter("使用 {{itemName}}")
  public void useItemQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("[QQ] 收到使用物品请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    replyHelper.qq(
        event, (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, null, f));
  }
}
