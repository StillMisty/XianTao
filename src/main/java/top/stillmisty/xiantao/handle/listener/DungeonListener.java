package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.DungeonCommandHandler;

@Component
@RequiredArgsConstructor
public class DungeonListener {

  private final DungeonCommandHandler dungeonCommandHandler;
  private final ReplyHelper replyHelper;

  // === 秘境列表 ===

  @Listener
  @ContentTrim
  @Filter("秘境")
  public void dungeonList(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "秘境列表", dungeonCommandHandler::handleDungeon);
  }

  @Listener
  @ContentTrim
  @Filter("秘境")
  public void dungeonListQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "秘境列表", dungeonCommandHandler::handleDungeon);
  }

  // === 秘境进入 ===

  @Listener
  @ContentTrim
  @Filter("秘境\\s*{{dungeonName}}")
  public void dungeonEnter(
      OneBotMessageEvent event, @FilterValue("dungeonName") String dungeonName) {
    replyHelper.oneBot(event, "进入秘境", dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  @Listener
  @ContentTrim
  @Filter("秘境\\s*{{dungeonName}}")
  public void dungeonEnterQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("dungeonName") String dungeonName) {
    replyHelper.qq(event, "进入秘境", dungeonName, dungeonCommandHandler::handleDungeonEnter);
  }

  // === 秘境探索 ===

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExplore(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "秘境探索", dungeonCommandHandler::handleDungeonExplore);
  }

  @Listener
  @ContentTrim
  @Filter("秘境探索")
  public void dungeonExploreQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "秘境探索", dungeonCommandHandler::handleDungeonExplore);
  }

  // === 秘境继续 ===

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinue(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "秘境继续", dungeonCommandHandler::handleDungeonContinue);
  }

  @Listener
  @ContentTrim
  @Filter("秘境继续")
  public void dungeonContinueQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "秘境继续", dungeonCommandHandler::handleDungeonContinue);
  }

  // === 秘境撤退 ===

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreat(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "秘境撤退", dungeonCommandHandler::handleDungeonRetreat);
  }

  @Listener
  @ContentTrim
  @Filter("秘境撤退")
  public void dungeonRetreatQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "秘境撤退", dungeonCommandHandler::handleDungeonRetreat);
  }
}
