package top.stillmisty.xiantao.service.ai;

import com.openai.services.blocking.ChatService;
import com.openai.services.blocking.chat.ChatCompletionService;
import java.util.function.Consumer;

public class ReasoningPreservingChatService implements ChatService {

  private final ChatService delegate;

  public ReasoningPreservingChatService(ChatService delegate) {
    this.delegate = delegate;
  }

  @Override
  public ChatCompletionService completions() {
    return new ReasoningPreservingChatCompletionService(delegate.completions());
  }

  @Override
  public ChatService.WithRawResponse withRawResponse() {
    return delegate.withRawResponse();
  }

  @Override
  @SuppressWarnings("deprecation")
  public ChatService withOptions(Consumer<com.openai.core.ClientOptions.Builder> consumer) {
    return new ReasoningPreservingChatService(delegate.withOptions(consumer));
  }
}
