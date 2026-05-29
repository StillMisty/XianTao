package top.stillmisty.xiantao.handle.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.PillCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class PillListener {
  private final PillCommandHandler pillCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeList(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "丹方列表", pillCommandHandler::handleRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方\\s*{{recipeName}}")
  public void recipeDetail(OneBotMessageEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.oneBot(event, "丹方详情", recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼方\\s*{{recipeName}}")
  public void refineAuto(OneBotMessageEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.oneBot(event, "自动炼丹", recipeName, pillCommandHandler::handleRefineAuto);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼\\s*{{herbInput}}")
  public void refineManual(OneBotMessageEvent event, @FilterValue("herbInput") String herbInput) {
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.oneBot(
        event, "手动炼丹", fmt -> pillCommandHandler.handleRefineManual(herbInputs, fmt));
  }

  // === QQ ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeListQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "丹方列表", pillCommandHandler::handleRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方\\s*{{recipeName}}")
  public void recipeDetailQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.qq(event, "丹方详情", recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼方\\s*{{recipeName}}")
  public void refineAutoQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.qq(event, "自动炼丹", recipeName, pillCommandHandler::handleRefineAuto);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼\\s*{{herbInput}}")
  public void refineManualQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("herbInput") String herbInput) {
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.qq(event, "手动炼丹", fmt -> pillCommandHandler.handleRefineManual(herbInputs, fmt));
  }
}
