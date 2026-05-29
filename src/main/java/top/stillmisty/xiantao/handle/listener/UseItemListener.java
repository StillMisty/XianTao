package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;

@Component
@RequiredArgsConstructor
public class UseItemListener {

  private final UseItemCommandHandler useItemCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}\\s+{{args}}")
  public void useItemWithArgs(
      OneBotMessageEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    replyHelper.oneBot(
        event, "使用物品", (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, args, f));
  }

  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}")
  public void useItem(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.oneBot(
        event, "使用物品", (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, null, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}\\s+{{args}}")
  public void useItemWithArgsQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    replyHelper.qq(
        event, "使用物品", (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, args, f));
  }

  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}")
  public void useItemQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.qq(
        event, "使用物品", (p, o, f) -> useItemCommandHandler.handleUseItem(p, o, itemName, null, f));
  }
}
