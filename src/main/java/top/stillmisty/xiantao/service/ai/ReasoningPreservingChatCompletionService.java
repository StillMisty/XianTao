package top.stillmisty.xiantao.service.ai;

import com.openai.core.JsonValue;
import com.openai.core.RequestOptions;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionDeleteParams;
import com.openai.models.chat.completions.ChatCompletionDeleted;
import com.openai.models.chat.completions.ChatCompletionListPage;
import com.openai.models.chat.completions.ChatCompletionListParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionRetrieveParams;
import com.openai.models.chat.completions.ChatCompletionUpdateParams;
import com.openai.services.blocking.chat.ChatCompletionService;
import com.openai.services.blocking.chat.completions.MessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ReasoningPreservingChatCompletionService implements ChatCompletionService {

  private final ChatCompletionService delegate;

  public ReasoningPreservingChatCompletionService(ChatCompletionService delegate) {
    this.delegate = delegate;
  }

  private static AtomicReference<String> reasoningHolder() {
    return ReasoningScopeAdvisor.REASONING_HOLDER.isBound()
        ? ReasoningScopeAdvisor.REASONING_HOLDER.get()
        : null;
  }

  @Override
  public ChatCompletion create(ChatCompletionCreateParams params, RequestOptions options) {
    ChatCompletionCreateParams modified = injectReasoning(params);
    ChatCompletion response = delegate.create(modified, options);
    extractReasoning(response);
    return response;
  }

  @Override
  public StreamResponse<ChatCompletionChunk> createStreaming(
      ChatCompletionCreateParams params, RequestOptions options) {
    ChatCompletionCreateParams modified = injectReasoning(params);
    return delegate.createStreaming(modified, options);
  }

  private ChatCompletionCreateParams injectReasoning(ChatCompletionCreateParams params) {
    AtomicReference<String> holder = reasoningHolder();
    if (holder == null) {
      return params;
    }
    String reasoning = holder.get();
    if (reasoning == null) {
      return params;
    }

    ChatCompletionCreateParams.Body body = params._body();
    List<ChatCompletionMessageParam> messages = body.messages();

    boolean modified = false;
    List<ChatCompletionMessageParam> newMessages = new ArrayList<>();

    for (ChatCompletionMessageParam msg : messages) {
      if (msg.isAssistant()) {
        ChatCompletionAssistantMessageParam assistant = msg.asAssistant();
        if (assistant.toolCalls().isPresent()
            && !assistant._additionalProperties().containsKey("reasoning_content")) {
          ChatCompletionAssistantMessageParam param =
              assistant.toBuilder()
                  .putAdditionalProperty("reasoning_content", JsonValue.from(reasoning))
                  .build();
          newMessages.add(ChatCompletionMessageParam.ofAssistant(param));
          modified = true;
          holder.set(null);
        } else {
          newMessages.add(msg);
        }
      } else {
        newMessages.add(msg);
      }
    }

    if (!modified) {
      return params;
    }

    return params.toBuilder().messages(newMessages).build();
  }

  private void extractReasoning(ChatCompletion response) {
    AtomicReference<String> holder = reasoningHolder();
    if (holder == null) {
      return;
    }
    for (ChatCompletion.Choice choice : response.choices()) {
      Map<String, JsonValue> props = choice.message()._additionalProperties();
      JsonValue value = props.get("reasoning_content");
      if (value != null) {
        String reasoning = value.convert(String.class);
        if (reasoning != null && !reasoning.isEmpty()) {
          holder.set(reasoning);
          return;
        }
      }
    }
  }

  @Override
  public ChatCompletionService.WithRawResponse withRawResponse() {
    return delegate.withRawResponse();
  }

  @Override
  @SuppressWarnings("deprecation")
  public ChatCompletionService withOptions(
      Consumer<com.openai.core.ClientOptions.Builder> consumer) {
    return new ReasoningPreservingChatCompletionService(delegate.withOptions(consumer));
  }

  @Override
  public MessageService messages() {
    return delegate.messages();
  }

  @Override
  public ChatCompletion retrieve(ChatCompletionRetrieveParams params, RequestOptions options) {
    return delegate.retrieve(params, options);
  }

  @Override
  public ChatCompletion update(ChatCompletionUpdateParams params, RequestOptions options) {
    return delegate.update(params, options);
  }

  @Override
  public ChatCompletionListPage list(ChatCompletionListParams params, RequestOptions options) {
    return delegate.list(params, options);
  }

  @Override
  public ChatCompletionDeleted delete(ChatCompletionDeleteParams params, RequestOptions options) {
    return delegate.delete(params, options);
  }
}
