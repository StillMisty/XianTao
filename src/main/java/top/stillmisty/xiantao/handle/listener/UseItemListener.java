package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.UseItemCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class UseItemListener {

  private final UseItemCommandHandler useItemCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}\\s+{{args}}")
  public void useItemWithArgs(
      MessageEvent event,
      @FilterValue("itemName") String itemName,
      @FilterValue("args") String args) {
    replyHelper.dispatch(
        event, "使用物品", fmt -> useItemCommandHandler.handleUseItem(itemName, args, fmt));
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("使用\\s*{{itemName}}")
  public void useItem(MessageEvent event, @FilterValue("itemName") String itemName) {
    replyHelper.dispatch(
        event, "使用物品", fmt -> useItemCommandHandler.handleUseItem(itemName, null, fmt));
  }
}
