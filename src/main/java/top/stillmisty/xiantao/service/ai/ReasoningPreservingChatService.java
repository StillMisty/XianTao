package top.stillmisty.xiantao.service.ai;

import com.openai.services.blocking.ChatService;
import com.openai.services.blocking.chat.ChatCompletionService;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ReasoningPreservingChatService implements ChatService {

  private final ChatService delegate;

  public ReasoningPreservingChatService(ChatService delegate) {
    this.delegate = delegate;
  }

  @Override
  public @NonNull ChatCompletionService completions() {
    return new ReasoningPreservingChatCompletionService(delegate.completions());
  }

  @Override
  public ChatService.@NonNull WithRawResponse withRawResponse() {
    return delegate.withRawResponse();
  }

  @Override
  public @NonNull ChatService withOptions(@NonNull Consumer<com.openai.core.ClientOptions.Builder> consumer) {
    return new ReasoningPreservingChatService(delegate.withOptions(consumer));
  }
}
