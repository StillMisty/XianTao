package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
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
        Prompt p = (i == 0 || fallbackModel == null) ? prompt : adaptForFallback(prompt);
        return delegates.get(i).call(p);
      } catch (Exception e) {
        log.warn("ChatModel[{}] failed, trying next: {}", i, e.getMessage());
        lastException = e;
      }
    }
    throw new IllegalStateException("All ChatModels in fallback chain failed", lastException);
  }

  private Prompt adaptForFallback(Prompt prompt) {
    ChatOptions opts = prompt.getOptions();
    return new Prompt(
        prompt.getInstructions(),
        OpenAiChatOptions.builder()
            .model(fallbackModel)
            .maxTokens(opts != null ? opts.getMaxTokens() : null)
            .temperature(opts != null ? opts.getTemperature() : null)
            .topP(opts != null ? opts.getTopP() : null)
            .build());
  }
}
