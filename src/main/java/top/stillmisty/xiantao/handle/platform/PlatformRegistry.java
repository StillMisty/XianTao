package top.stillmisty.xiantao.handle.platform;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import love.forte.simbot.event.MessageEvent;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

/** 平台注册表 管理所有平台处理器，提供统一的平台分发接口 */
@Component
public class PlatformRegistry {

  private final Map<PlatformType, PlatformHandler> handlers;

  public PlatformRegistry(List<PlatformHandler> handlerList) {
    this.handlers =
        handlerList.stream()
            .collect(Collectors.toMap(PlatformHandler::getPlatformType, Function.identity()));
  }

  /**
   * 获取支持指定事件的平台处理器
   *
   * @param event 消息事件
   * @return 平台处理器
   * @throws IllegalArgumentException 如果没有支持的处理器
   */
  public PlatformHandler getHandler(MessageEvent event) {
    return handlers.values().stream()
        .filter(handler -> handler.supports(event))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName()));
  }

  /**
   * 获取指定平台类型的处理器
   *
   * @param platformType 平台类型
   * @return 平台处理器
   * @throws IllegalArgumentException 如果没有找到处理器
   */
  public PlatformHandler getHandler(PlatformType platformType) {
    PlatformHandler handler = handlers.get(platformType);
    if (handler == null) {
      throw new IllegalArgumentException("不支持的平台类型: " + platformType);
    }
    return handler;
  }

  /**
   * 检查是否支持指定的事件类型
   *
   * @param event 消息事件
   * @return 是否支持
   */
  public boolean supports(MessageEvent event) {
    return handlers.values().stream().anyMatch(handler -> handler.supports(event));
  }

  /**
   * 获取所有支持的平台类型
   *
   * @return 平台类型集合
   */
  public java.util.Set<PlatformType> getSupportedPlatforms() {
    return handlers.keySet();
  }
}
