package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

@Slf4j
public class FallbackChatModel implements ChatModel {

  private final List<ChatModel> delegates;
  @Nullable private final String fallbackModel;

  public FallbackChatModel(List<ChatModel> delegates, @Nullable String fallbackModel) {
    if (delegates.isEmpty()) {
      throw new IllegalArgumentException("FallbackChatModel requires at least one delegate");
    }
    this.delegates = List.copyOf(delegates);
    this.fallbackModel = fallbackModel;
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
        log.warn("ChatModel[{}] failed, trying next: {}", i, e.getMessage());
        lastException = e;
      }
    }
    throw new IllegalStateException("All ChatModels in fallback chain failed", lastException);
  }

  private Prompt adaptPrompt(Prompt prompt, ChatModel delegate, @Nullable String overrideModel) {
    ChatOptions opts = prompt.getOptions();
    if (delegate instanceof DeepSeekChatModel) {
      return new Prompt(
          prompt.getInstructions(),
          DeepSeekChatOptions.builder()
              .model(overrideModel != null ? overrideModel : opts != null ? opts.getModel() : null)
              .maxTokens(opts != null ? opts.getMaxTokens() : null)
              .temperature(opts != null ? opts.getTemperature() : null)
              .topP(opts != null ? opts.getTopP() : null)
              .build());
    }
    if (delegate instanceof OpenAiChatModel) {
      return new Prompt(
          prompt.getInstructions(),
          OpenAiChatOptions.builder()
              .model(overrideModel != null ? overrideModel : opts != null ? opts.getModel() : null)
              .maxTokens(opts != null ? opts.getMaxTokens() : null)
              .temperature(opts != null ? opts.getTemperature() : null)
              .topP(opts != null ? opts.getTopP() : null)
              .build());
    }
    return prompt;
  }
}
