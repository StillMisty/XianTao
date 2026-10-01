package top.stillmisty.xiantao.handle.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.PillCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class PillListener {
  private final PillCommandHandler pillCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("丹方")
  public void recipeList(QqIncomingMessage event) {
    replyHelper.dispatch(event, "丹方列表", pillCommandHandler::handleRecipeList);
  }

  @RequireAuth
  @Command("丹方\\s*{{recipeName}}")
  public void recipeDetail(QqIncomingMessage event, @Arg("recipeName") String recipeName) {
    replyHelper.dispatch(event, "丹方详情", recipeName, pillCommandHandler::handleRecipeDetail);
  }

  @RequireAuth
  @Command("炼方\\s*{{recipeName}}")
  public void refineAuto(QqIncomingMessage event, @Arg("recipeName") String recipeName) {
    replyHelper.dispatch(event, "自动炼丹", recipeName, pillCommandHandler::handleRefineAuto);
  }

  @RequireAuth
  @Command("炼(?!方)\\s*{{herbInput,.+}}")
  public void refineManual(QqIncomingMessage event, @Arg("herbInput") String herbInput) {
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    replyHelper.dispatch(
        event, "手动炼丹", fmt -> pillCommandHandler.handleRefineManual(herbInputs, fmt));
  }
}
