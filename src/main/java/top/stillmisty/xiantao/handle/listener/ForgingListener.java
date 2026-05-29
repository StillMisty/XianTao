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
import top.stillmisty.xiantao.util.MaterialParser;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgingListener {
  private final ForgingCommandHandler forgingCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeList(OneBotMessageEvent event) {
    replyHelper.oneBot(event, forgingCommandHandler::handleForgingRecipeList);
  }

  @Listener
  @ContentTrim
  @Filter("锻造 {{input}}")
  public void forge(OneBotMessageEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (MaterialParser.isMaterialInput(parts[0])) {
      List<String> materialInputs = Arrays.asList(parts);
      replyHelper.oneBot(
          event, (p, o, f) -> forgingCommandHandler.handleForgeManual(p, o, materialInputs, f));
    } else {
      replyHelper.oneBot(event, (p, o, f) -> forgingCommandHandler.handleForgeAuto(p, o, input, f));
    }
  }

  @Listener
  @ContentTrim
  @Filter("强化 {{input}}")
  public void enhance(OneBotMessageEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (parts.length > 1 && MaterialParser.isMaterialInput(parts[1])) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      replyHelper.oneBot(
          event,
          (p, o, f) ->
              forgingCommandHandler.handleEnhanceManual(p, o, equipmentInput, materialInputs, f));
    } else {
      replyHelper.oneBot(
          event, (p, o, f) -> forgingCommandHandler.handleEnhanceAuto(p, o, input, f));
    }
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeListQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, forgingCommandHandler::handleForgingRecipeList);
  }

  @Listener
  @ContentTrim
  @Filter("锻造 {{input}}")
  public void forgeQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (MaterialParser.isMaterialInput(parts[0])) {
      List<String> materialInputs = Arrays.asList(parts);
      replyHelper.qq(
          event, (p, o, f) -> forgingCommandHandler.handleForgeManual(p, o, materialInputs, f));
    } else {
      replyHelper.qq(event, (p, o, f) -> forgingCommandHandler.handleForgeAuto(p, o, input, f));
    }
  }

  @Listener
  @ContentTrim
  @Filter("强化 {{input}}")
  public void enhanceQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String[] parts = input.split("\\s+");
    if (parts.length > 1 && MaterialParser.isMaterialInput(parts[1])) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      replyHelper.qq(
          event,
          (p, o, f) ->
              forgingCommandHandler.handleEnhanceManual(p, o, equipmentInput, materialInputs, f));
    } else {
      replyHelper.qq(event, (p, o, f) -> forgingCommandHandler.handleEnhanceAuto(p, o, input, f));
    }
  }
}
