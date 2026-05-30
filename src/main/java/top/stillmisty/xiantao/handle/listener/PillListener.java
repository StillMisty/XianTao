package top.stillmisty.xiantao.handle.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
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

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeList(MessageEvent event) {
    replyHelper.dispatch(event, "丹方列表", pillCommandHandler::handleRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("丹方\\s*{{recipeName}}")
  public void recipeDetail(MessageEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.dispatch(event, "丹方详情", recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼方\\s*{{recipeName}}")
  public void refineAuto(MessageEvent event, @FilterValue("recipeName") String recipeName) {
    replyHelper.dispatch(event, "自动炼丹", recipeName, pillCommandHandler::handleRefineAuto);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("炼\\s*{{herbInput}}")
  public void refineManual(MessageEvent event, @FilterValue("herbInput") String herbInput) {
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.dispatch(
        event, "手动炼丹", fmt -> pillCommandHandler.handleRefineManual(herbInputs, fmt));
  }
}
