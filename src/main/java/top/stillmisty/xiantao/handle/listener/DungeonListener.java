package top.stillmisty.xiantao.handle.listener;

import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.DungeonCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
public class DungeonListener {

  private final DungeonCommandHandler dungeonCommandHandler;
  private final ReplyHelper replyHelper;

  public DungeonListener(DungeonCommandHandler dungeonCommandHandler, ReplyHelper replyHelper) {
    this.dungeonCommandHandler = dungeonCommandHandler;
    this.replyHelper = replyHelper;
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "秘境")
  public void dungeonOrStatus(MessageEvent event) {
    replyHelper.dispatch(event, "秘境/状态", dungeonCommandHandler::handleDungeonOrStatus);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "秘境\\s*{{dungeonName}}")
  public void dungeonEnter(MessageEvent event, @FilterValue("dungeonName") String dungeonName) {
    replyHelper.dispatch(event, "进入秘境", dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "秘灵")
  public void dungeonChat(MessageEvent event) {
    replyHelper.dispatch(event, "秘灵对话", dungeonCommandHandler::handleCreatureHelp);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "秘灵\\s*{{content}}")
  public void dungeonChatWithContent(MessageEvent event, @FilterValue("content") String content) {
    replyHelper.dispatch(event, "秘灵对话", content, dungeonCommandHandler::handleCreatureChat);
  }
}
