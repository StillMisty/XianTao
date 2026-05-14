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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.ForgingCommandHandler;

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
    String response =
        forgingCommandHandler.handleForgingRecipeList(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("锻造 {{input}}")
  public void forge(OneBotMessageEvent event, @FilterValue("input") String input) {
    String response;
    if (input.contains("×") || input.contains("x") || input.contains("X")) {
      List<String> materialInputs = Arrays.asList(input.split("\\s+"));
      response =
          forgingCommandHandler.handleForgeManual(
              PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), materialInputs);
    } else {
      response =
          forgingCommandHandler.handleForgeAuto(
              PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), input);
    }
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("强化 {{input}}")
  public void enhance(OneBotMessageEvent event, @FilterValue("input") String input) {
    String response;
    String[] parts = input.split("\\s+");
    if (parts.length > 1
        && (parts[1].contains("×") || parts[1].contains("x") || parts[1].contains("X"))) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      response =
          forgingCommandHandler.handleEnhanceManual(
              PlatformType.ONE_BOT_V11,
              event.getAuthorId().toString(),
              equipmentInput,
              materialInputs);
    } else {
      response =
          forgingCommandHandler.handleEnhanceAuto(
              PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), input);
    }
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("锻造列表")
  public void recipeListQq(QGGroupAtMessageCreateEvent event) {
    String response =
        forgingCommandHandler.handleForgingRecipeListMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("锻造 {{input}}")
  public void forgeQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String response;
    if (input.contains("×") || input.contains("x") || input.contains("X")) {
      List<String> materialInputs = Arrays.asList(input.split("\\s+"));
      response =
          forgingCommandHandler.handleForgeManualMarkdown(
              PlatformType.QQ, event.getAuthorId().toString(), materialInputs);
    } else {
      response =
          forgingCommandHandler.handleForgeAutoMarkdown(
              PlatformType.QQ, event.getAuthorId().toString(), input);
    }
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("强化 {{input}}")
  public void enhanceQq(QGGroupAtMessageCreateEvent event, @FilterValue("input") String input) {
    String response;
    String[] parts = input.split("\\s+");
    if (parts.length > 1
        && (parts[1].contains("×") || parts[1].contains("x") || parts[1].contains("X"))) {
      String equipmentInput = parts[0];
      List<String> materialInputs = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
      response =
          forgingCommandHandler.handleEnhanceManualMarkdown(
              PlatformType.QQ, event.getAuthorId().toString(), equipmentInput, materialInputs);
    } else {
      response =
          forgingCommandHandler.handleEnhanceAutoMarkdown(
              PlatformType.QQ, event.getAuthorId().toString(), input);
    }
    replyHelper.replyQQ(event, response);
  }
}
