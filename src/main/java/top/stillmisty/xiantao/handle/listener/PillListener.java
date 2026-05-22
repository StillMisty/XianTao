package top.stillmisty.xiantao.handle.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.PillCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class PillListener {
  private final PillCommandHandler pillCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeList(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到丹方列表查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, pillCommandHandler::handleRecipeList);
  }

  @Listener
  @ContentTrim
  @Filter("丹方 {{recipeName}}")
  public void recipeDetail(OneBotMessageEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug(
        "[OneBot] 收到丹方详情查询请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    replyHelper.oneBot(event, recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @Listener
  @ContentTrim
  @Filter("炼方 {{recipeName}}")
  public void refineAuto(OneBotMessageEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("[OneBot] 收到自动炼丹请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    replyHelper.oneBot(event, recipeName, pillCommandHandler::handleRefineAuto);
  }

  @Listener
  @ContentTrim
  @Filter("炼 {{herbInput}}")
  public void refineManual(OneBotMessageEvent event, @FilterValue("herbInput") String herbInput) {
    log.debug("[OneBot] 收到手动炼丹请求 - AuthorId: {}, HerbInput: {}", event.getAuthorId(), herbInput);
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.oneBot(
        event, (p, o, f) -> pillCommandHandler.handleRefineManual(p, o, herbInputs, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeListQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到丹方列表查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, pillCommandHandler::handleRecipeList);
  }

  @Listener
  @ContentTrim
  @Filter("丹方 {{recipeName}}")
  public void recipeDetailQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("[QQ] 收到丹方详情查询请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    replyHelper.qq(event, recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @Listener
  @ContentTrim
  @Filter("炼方 {{recipeName}}")
  public void refineAutoQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("[QQ] 收到自动炼丹请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    replyHelper.qq(event, recipeName, pillCommandHandler::handleRefineAuto);
  }

  @Listener
  @ContentTrim
  @Filter("炼 {{herbInput}}")
  public void refineManualQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("herbInput") String herbInput) {
    log.debug("[QQ] 收到手动炼丹请求 - AuthorId: {}, HerbInput: {}", event.getAuthorId(), herbInput);
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.qq(event, (p, o, f) -> pillCommandHandler.handleRefineManual(p, o, herbInputs, f));
  }
}
