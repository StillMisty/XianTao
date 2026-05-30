package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DungeonListener {

  private final DungeonCommandHandler dungeonCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "秘境")
  public void dungeonList(MessageEvent event) {
    replyHelper.dispatch(event, "秘境列表", dungeonCommandHandler::handleDungeon);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "秘境\\s*{{dungeonName}}")
  public void dungeonEnter(MessageEvent event, @FilterValue("dungeonName") String dungeonName) {
    replyHelper.dispatch(event, "进入秘境", dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "秘境探索")
  public void dungeonExplore(MessageEvent event) {
    replyHelper.dispatch(event, "秘境探索", dungeonCommandHandler::handleDungeonExplore);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "秘境继续")
  public void dungeonContinue(MessageEvent event) {
    replyHelper.dispatch(event, "秘境继续", dungeonCommandHandler::handleDungeonContinue);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "秘境撤退")
  public void dungeonRetreat(MessageEvent event) {
    replyHelper.dispatch(event, "秘境撤退", dungeonCommandHandler::handleDungeonRetreat);
  }
}
