package top.stillmisty.xiantao.service.ai;

import java.time.Instant;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工具调用日志包装器，提供执行耗时和异常日志。
 *
 * <p>Spring AI 的 {@code @Tool} 方法抛出的异常会被框架捕获并以标准格式返回给 LLM。 ToolExecutor 追踪每次工具调用的执行耗时，保证工具异常可追踪且不影响
 * LLM 的正常错误处理流程。
 */
@Component
@Slf4j
public class ToolExecutor {

  /**
   * 执行工具操作并记录执行耗时和异常日志。
   *
   * @param toolName 工具名称（用于日志）
   * @param action 工具的业务逻辑
   * @param <T> 返回值类型
   * @return 工具执行结果
   * @throws RuntimeException 如果 action 抛出异常，则原样重新抛出
   */
  public <T> T execute(String toolName, Supplier<T> action) {
    Instant start = Instant.now();
    try {
      T result = action.get();
      log.debug(
          "{} 成功 ({}ms)", toolName, java.time.Duration.between(start, Instant.now()).toMillis());
      return result;
    } catch (Exception e) {
      log.error(
          "{} 失败 ({}ms): {}",
          toolName,
          java.time.Duration.between(start, Instant.now()).toMillis(),
          e.getMessage(),
          e);
      throw e;
    }
  }
}
