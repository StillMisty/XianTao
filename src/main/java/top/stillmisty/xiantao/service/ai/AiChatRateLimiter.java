package top.stillmisty.xiantao.service.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

/**
 * LLM 对话入口的用户级频控（内存滑动窗口，重启即重置）。
 *
 * <p>掌柜/宗灵/秘灵/地灵等入口每条消息都会触发带工具循环的完整 LLM 调用， 无限连发既刷成本也易触发上游限流。
 */
@Component
public class AiChatRateLimiter {

  /** 每用户每分钟最多 AI 对话次数 */
  private static final int MAX_REQUESTS_PER_MINUTE = 10;

  private final Cache<Long, AtomicInteger> counters =
      Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(2)).build();

  /** 超出频控时抛出业务异常，否则计数 +1 */
  public void checkAllowed(Long userId) {
    AtomicInteger counter = counters.get(userId, k -> new AtomicInteger(0));
    if (counter != null && counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
      throw new BusinessException(ErrorCode.AI_RATE_LIMITED, MAX_REQUESTS_PER_MINUTE);
    }
  }
}
