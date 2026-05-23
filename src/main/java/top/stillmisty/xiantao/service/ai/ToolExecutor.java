package top.stillmisty.xiantao.service.ai;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工具调用日志包装器，不改变异常传播路径。
 *
 * <p>Spring AI 的 {@code @Tool} 方法抛出的异常会被框架捕获并以标准格式返回给 LLM。 ToolExecutor 仅做日志记录，保证工具异常可追踪且不影响 LLM
 * 的正常错误处理流程。
 */
@Component
@Slf4j
public class ToolExecutor {

  /**
   * 执行工具操作并在异常时记录日志后重新抛出。
   *
   * @param toolName 工具名称（用于日志）
   * @param action 工具的业务逻辑
   * @param <T> 返回值类型
   * @return 工具执行结果
   * @throws RuntimeException 如果 action 抛出异常，则原样重新抛出
   */
  public <T> T execute(String toolName, Supplier<T> action) {
    try {
      return action.get();
    } catch (Exception e) {
      log.error("{} 失败", toolName, e);
      throw e;
    }
  }
}
