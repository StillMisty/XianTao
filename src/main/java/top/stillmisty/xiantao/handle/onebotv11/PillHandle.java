package top.stillmisty.xiantao.handle.onebotv11;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.PillCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class PillHandle {

  private final PillCommandHandler pillCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeList(MessageEvent event) {
    log.debug("收到丹方列表查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        pillCommandHandler.handleRecipeList(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丹方 {{recipeName}}")
  public void recipeDetail(MessageEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("收到丹方详情查询请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    String response =
        pillCommandHandler.handleRecipeDetail(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), recipeName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("炼方 {{recipeName}}")
  public void refineAuto(MessageEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("收到自动炼丹请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    String response =
        pillCommandHandler.handleRefineAuto(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), recipeName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("炼 {{herbInput}}")
  public void refineManual(MessageEvent event, @FilterValue("herbInput") String herbInput) {
    log.debug("收到手动炼丹请求 - AuthorId: {}, HerbInput: {}", event.getAuthorId(), herbInput);
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    String response =
        pillCommandHandler.handleRefineManual(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), herbInputs);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(MessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
