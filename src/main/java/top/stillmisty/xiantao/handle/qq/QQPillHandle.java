package top.stillmisty.xiantao.handle.qq;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
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
public class QQPillHandle {

  private final PillCommandHandler pillCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("丹方")
  public void recipeList(QGGroupAtMessageCreateEvent event) {
    log.debug("收到丹方列表查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        pillCommandHandler.handleRecipeListMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("丹方 {{recipeName}}")
  public void recipeDetail(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("收到丹方详情查询请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    String response =
        pillCommandHandler.handleRecipeDetailMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), recipeName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("炼方 {{recipeName}}")
  public void refineAuto(
      QGGroupAtMessageCreateEvent event, @FilterValue("recipeName") String recipeName) {
    log.debug("收到自动炼丹请求 - AuthorId: {}, RecipeName: {}", event.getAuthorId(), recipeName);
    String response =
        pillCommandHandler.handleRefineAutoMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), recipeName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("炼 {{herbInput}}")
  public void refineManual(
      QGGroupAtMessageCreateEvent event, @FilterValue("herbInput") String herbInput) {
    log.debug("收到手动炼丹请求 - AuthorId: {}, HerbInput: {}", event.getAuthorId(), herbInput);
    List<String> herbInputs = Arrays.asList(herbInput.split("\\s+"));
    String response =
        pillCommandHandler.handleRefineManualMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), herbInputs);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
