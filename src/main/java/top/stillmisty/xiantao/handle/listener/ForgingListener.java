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
import top.stillmisty.xiantao.handle.command.ForgingCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.util.MaterialParser;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgingListener {
  private final ForgingCommandHandler forgingCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeList(OneBotMessageEvent event) {
    replyHelper.oneBot(event, forgingCommandHandler::handleForgingRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造\\s*{{input}}")
  public void forge(OneBotMessageEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (MaterialParser.isMaterialInput(parts[0])) {
      List<String> materialInputs = Arrays.asList(parts);
      replyHelper.oneBot(
          event, fmt -> forgingCommandHandler.handleForgeManual(materialInputs, fmt));
    } else {
      replyHelper.oneBot(event, fmt -> forgingCommandHandler.handleForgeAuto(input, fmt));
    }
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("强化\\s*{{input}}")
  public void enhance(OneBotMessageEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (parts.length > 1 && MaterialParser.isMaterialInput(parts[1])) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      replyHelper.oneBot(
          event,
          fmt -> forgingCommandHandler.handleEnhanceManual(equipmentInput, materialInputs, fmt));
    } else {
      replyHelper.oneBot(event, fmt -> forgingCommandHandler.handleEnhanceAuto(input, fmt));
    }
  }

  // === QQ ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeListQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, forgingCommandHandler::handleForgingRecipeList);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("锻造\\s*{{input}}")
  public void forgeQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (MaterialParser.isMaterialInput(parts[0])) {
      List<String> materialInputs = Arrays.asList(parts);
      replyHelper.qq(event, fmt -> forgingCommandHandler.handleForgeManual(materialInputs, fmt));
    } else {
      replyHelper.qq(event, fmt -> forgingCommandHandler.handleForgeAuto(input, fmt));
    }
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("强化\\s*{{input}}")
  public void enhanceQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (parts.length > 1 && MaterialParser.isMaterialInput(parts[1])) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      replyHelper.qq(
          event,
          fmt -> forgingCommandHandler.handleEnhanceManual(equipmentInput, materialInputs, fmt));
    } else {
      replyHelper.qq(event, fmt -> forgingCommandHandler.handleEnhanceAuto(input, fmt));
    }
  }
}
