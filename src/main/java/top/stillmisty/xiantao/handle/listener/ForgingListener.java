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
import top.stillmisty.xiantao.handle.command.ForgingCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.util.MaterialParser;

@Component
@RequiredArgsConstructor
public class ForgingListener {
  private final ForgingCommandHandler forgingCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeList(MessageEvent event) {
    replyHelper.dispatch(event, "锻造列表", forgingCommandHandler::handleForgingRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造\\s*{{input}}")
  public void forge(MessageEvent event, @FilterValue("input") String input) {
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
  @Listener
  @ContentTrim
  @Filter("强化\\s*{{input}}")
  public void enhance(MessageEvent event, @FilterValue("input") String input) {
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
