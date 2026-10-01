package top.stillmisty.xiantao.handle.listener;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.ForgingCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.util.MaterialParser;

@Component
@RequiredArgsConstructor
@CommandGroup
public class ForgingListener {
  private final ForgingCommandHandler forgingCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("锻造列表")
  public void recipeList(QqIncomingMessage event) {
    replyHelper.dispatch(event, "锻造列表", forgingCommandHandler::handleForgingRecipeList);
  }

  @RequireAuth
  @Command("锻造\\s*{{input}}")
  public void forge(QqIncomingMessage event, @Arg("input") String input) {
    replyHelper.dispatch(
        event,
        "锻造",
        fmt -> {
          String[] parts = input.split("\\s+", -1);
          if (MaterialParser.isMaterialInput(parts[0])) {
            List<String> materialInputs = Arrays.asList(parts);
            return forgingCommandHandler.handleForgeManual(materialInputs, fmt);
          } else {
            return forgingCommandHandler.handleForgeAuto(input, fmt);
          }
        });
  }

  @RequireAuth
  @Command("强化\\s*{{input}}")
  public void enhance(QqIncomingMessage event, @Arg("input") String input) {
    replyHelper.dispatch(
        event,
        "强化",
        fmt -> {
          String[] parts = input.split("\\s+", -1);
          if (parts.length > 1 && MaterialParser.isMaterialInput(parts[1])) {
            String equipmentInput = parts[0];
            List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
            return forgingCommandHandler.handleEnhanceManual(equipmentInput, materialInputs, fmt);
          } else {
            return forgingCommandHandler.handleEnhanceAuto(input, fmt);
          }
        });
  }
}
