package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.BeastCommandHandler;

/** 灵兽系统监听器 处理灵兽出战、召回、恢复、进化、放生等命令 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeastHandle {

  private final BeastCommandHandler beastCommandHandler;

  /** 处理灵兽出战命令 格式：灵兽出战 [编号] 示例：灵兽出战 1 */
  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeast(MessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleDeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    event.replyBlocking(response);
  }

  /** 处理灵兽召回命令 格式：灵兽召回 [编号/all] 示例：灵兽召回 1 或 灵兽召回 all */
  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(MessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    event.replyBlocking(response);
  }

  /** 处理灵兽恢复命令 格式：灵兽恢复 [编号/all] 示例：灵兽恢复 1 或 灵兽恢复 all */
  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(MessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    event.replyBlocking(response);
  }

  /** 处理灵兽进化命令 格式：灵兽进化 [编号] [升阶/升品] 示例：灵兽进化 1 升阶 或 灵兽进化 1 升品 */
  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}} {{mode}}")
  public void evolveBeast(
      MessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug(
        "收到灵兽进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        beastCommandHandler.handleEvolveBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, mode);
    event.replyBlocking(response);
  }

  /** 处理灵兽放生命令 格式：灵兽放生 [编号] 示例：灵兽放生 1 */
  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(MessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    event.replyBlocking(response);
  }

  /** 处理查看出战灵兽命令 格式：出战灵兽 */
  @Listener
  @ContentTrim
  @Filter("出战灵兽")
  public void getDeployedBeasts(MessageEvent event) {
    log.debug("收到查看出战灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeasts(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    event.replyBlocking(response);
  }
}
