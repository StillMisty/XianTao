package top.stillmisty.xiantao.handle.dispatch;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqEventListener;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.listener.ReplyHelper;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;
import top.stillmisty.xiantao.service.AuthenticationService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.analytics.AnalyticsService;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.util.TextFormat;

/**
 * 命令调度器：QQ 事件 → 虚拟线程 → 模板匹配 → 认证 → 执行。
 *
 * <p>拦截顺序与 ADR-0002 一致：匹配（原 priority = 50）→ 认证（100）→ GM（200）。
 * 未匹配的消息静默忽略（恰好命中某命令字面前缀时回复缺参提示）；认证失败与执行异常通过 {@link ReplyHelper} 显式回复。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandDispatcher implements QqEventListener {

  private final CommandRegistry registry;
  private final AuthenticationService authService;
  private final PlatformRegistry platformRegistry;
  private final ReplyHelper replyHelper;
  private final UserStateService userStateService;
  private final AnalyticsService analyticsService;

  /** 每个事件一条虚拟线程，命令内可自由阻塞（数据库、AI 调用）。 */
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  /** 同一玩家的命令按到达顺序串行执行，避免并发指令竞态。 */
  private final PerKeySerialExecutor lanes = new PerKeySerialExecutor(executor);

  @Override
  public void onMessage(QqIncomingMessage message) {
    executor.execute(() -> handle(message));
  }

  private void handle(QqIncomingMessage message) {
    PlatformHandler handler;
    try {
      handler = platformRegistry.getHandler(message);
    } catch (RuntimeException e) {
      log.error("无法定位平台处理器: scene={}", message.scene(), e);
      return;
    }

    String text = message.content().strip();
    try {
      for (RegisteredCommand command : registry.commands()) {
        if (!command.template().matches(text)) {
          continue;
        }
        String laneKey = handler.getPlatformType().name() + ":" + message.openId();
        lanes.execute(laneKey, () -> runInLane(handler, message, command, text));
        return;
      }
      Optional<String> partial = registry.partialPrefix(text);
      if (partial.isPresent()) {
        String name = partial.get();
        log.debug("[{}] 命令缺参: {}", handler.getPlatformType(), name);
        replyHelper.reply(
            handler,
            message,
            TextFormat.get().tip("「" + name + "」还需要补充内容，输入「帮助 " + name + "」查看用法"));
        return;
      }
      log.debug("[{}] 消息未匹配任何命令: {}", handler.getPlatformType(), abbreviate(text));
    } catch (RuntimeException e) {
      // 兜底：匹配阶段异常不能让事件静默丢失（车道内异常由 runInLane 处理）
      log.error("[{}] 处理消息异常: eventId={}", handler.getPlatformType(), message.eventId(), e);
      replyHelper.reply(handler, message, TextFormat.get().error("系统繁忙，请稍后再试"));
    }
  }

  /** 车道内执行：异常兜底回复，保证单个玩家的失败不影响其他玩家与后续命令。 */
  private void runInLane(
      PlatformHandler handler, QqIncomingMessage message, RegisteredCommand command, String text) {
    try {
      dispatch(handler, message, command, text);
    } catch (RuntimeException e) {
      log.error("[{}] 处理消息异常: eventId={}", handler.getPlatformType(), message.eventId(), e);
      replyHelper.reply(handler, message, TextFormat.get().error("系统繁忙，请稍后再试"));
    }
  }

  private void dispatch(
      PlatformHandler handler, QqIncomingMessage message, RegisteredCommand command, String text) {
    Map<String, String> args = command.template().extract(text);

    Long userId = null;
    if (command.requireAuth() || command.requireGm()) {
      ServiceResult<Long> auth =
          authService.authenticate(handler.getPlatformType(), message.openId());
      if (!(auth instanceof ServiceResult.Success<Long>(var authenticated))) {
        String error =
            auth instanceof ServiceResult.Failure<Long>(var ignoredCode, var errorMessage)
                ? errorMessage
                : "认证失败";
        replyHelper.reply(handler, message, TextFormat.get().error(error));
        return;
      }
      userId = authenticated;
      if (command.requireGm() && !authService.isGm(userId)) {
        replyHelper.reply(handler, message, TextFormat.get().error("你不是 GM，无法执行 GM 指令"));
        return;
      }
    }

    long startedNanos = System.nanoTime();
    if (userId == null) {
      invoke(handler, message, command, args, null, startedNanos);
    } else {
      Long boundUserId = userId;
      UserContext.withUser(
          boundUserId,
          () -> {
            // 命令边界统一结算过期状态：深层服务只做纯数据加载（PlayerLoader），不再各自触发结算
            userStateService.settle(boundUserId);
            invoke(handler, message, command, args, boundUserId, startedNanos);
            return null;
          });
    }
  }

  private void invoke(
      PlatformHandler handler,
      QqIncomingMessage message,
      RegisteredCommand command,
      Map<String, String> args,
      @Nullable Long userId,
      long startedNanos) {
    try {
      command.invoke(message, args);
      recordCommand(userId, command, startedNanos, true);
    } catch (Exception e) {
      log.error("[{}] 命令执行异常: {}", handler.getPlatformType(), command.describe(), e);
      replyHelper.reply(handler, message, TextFormat.get().error("系统繁忙，请稍后再试"));
      recordCommand(userId, command, startedNanos, false);
    }
  }

  /** 分析事件：指令名 + 耗时（毫秒）+ 成败。 */
  private void recordCommand(
      @Nullable Long userId, RegisteredCommand command, long startedNanos, boolean ok) {
    long elapsedMs = (System.nanoTime() - startedNanos) / 1_000_000;
    analyticsService.record(
        "command", userId, abbreviate(command.describe()), elapsedMs, Map.of("ok", ok));
  }

  private static String abbreviate(String text) {
    return text.length() <= 60 ? text : text.substring(0, 60) + "...";
  }

  @PreDestroy
  void shutdown() {
    executor.shutdown();
  }
}
