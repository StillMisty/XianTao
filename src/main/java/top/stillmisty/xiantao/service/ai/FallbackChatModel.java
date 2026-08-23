package top.stillmisty.xiantao.service.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

@Slf4j
public class FallbackChatModel implements ChatModel {

  private final List<ChatModel> delegates;
  @Nullable private final String fallbackModel;
  private final Map<Class<?>, ChatOptionsAdapter> adapterRegistry;

  public FallbackChatModel(
      List<ChatModel> delegates,
      @Nullable String fallbackModel,
      List<ChatOptionsAdapter> adapters) {
    if (delegates.isEmpty()) {
      throw new IllegalArgumentException("FallbackChatModel requires at least one delegate");
    }
    this.delegates = List.copyOf(delegates);
    this.fallbackModel = fallbackModel;
    this.adapterRegistry = new HashMap<>();
    for (ChatOptionsAdapter adapter : adapters) {
      adapterRegistry.put(adapter.supportedType(), adapter);
    }
  }

  public FallbackChatModel(List<ChatModel> delegates, @Nullable String fallbackModel) {
    this(delegates, fallbackModel, List.of());
  }

  @Override
  public ChatOptions getOptions() {
    // 必须返回第一个委托模型的 options，使其 instanceof ToolCallingChatOptions，
    // 否则 DefaultChatClientUtils 不会将 .tools() 传进来的回调设置到 Prompt 的选项中，
    // 导致 ToolCallingAdvisor 跳过自身，最终 HTTP 请求中 tools=null。
    return delegates.getFirst().getOptions();
  }

  @Override
  public @NonNull ChatResponse call(@NonNull Prompt prompt) {
    Exception lastException = null;
    for (int i = 0; i < delegates.size(); i++) {
      try {
        ChatModel delegate = delegates.get(i);
        Prompt p = adaptPrompt(prompt, delegate, i == 0 ? null : fallbackModel);
        return delegate.call(p);
      } catch (Exception e) {
        // 首个失败即打完整堆栈，便于排障（最终异常只有链尾信息）
        log.warn("ChatModel[{}] failed, trying next", i, e);
        lastException = e;
      }
    }
    throw new IllegalStateException("All ChatModels in fallback chain failed", lastException);
  }

  private Prompt adaptPrompt(Prompt prompt, ChatModel delegate, @Nullable String overrideModel) {
    // 降级模型：通过适配器替换 model 名
    if (overrideModel != null) {
      ChatOptionsAdapter adapter = adapterRegistry.get(delegate.getClass());
      if (adapter != null) {
        return adapter.adaptPrompt(prompt, overrideModel);
      }
      log.warn(
          "No ChatOptionsAdapter found for {}, returning original prompt", delegate.getClass());
      return prompt;
    }
    // 主模型：advisors 可能将 options 包装为 DefaultChatOptions，
    // 而 DeepSeekChatModel 等实现期望特定 options 类型。
    // 通过适配器重建正确类型的 options 来避免 ClassCastException。
    ChatOptionsAdapter adapter = adapterRegistry.get(delegate.getClass());
    if (adapter != null) {
      String currentModel = prompt.getOptions() != null ? prompt.getOptions().getModel() : null;
      if (currentModel != null) {
        return adapter.adaptPrompt(prompt, currentModel);
      }
    }
    return prompt;
  }
}
