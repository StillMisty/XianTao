package top.stillmisty.xiantao.service.ai;

import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;

public class ReasoningScopeAdvisor implements CallAdvisor {

  static final ScopedValue<AtomicReference<String>> REASONING_HOLDER = ScopedValue.newInstance();
  static final ScopedValue<String> CONVERSATION_ID = ScopedValue.newInstance();

  @Override
  public @NonNull String getName() {
    return "ReasoningScopeAdvisor";
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public @NonNull ChatClientResponse adviseCall(
      @NonNull ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain callAdvisorChain) {
    AtomicReference<String> holder = new AtomicReference<>();
    Object convId = chatClientRequest.context().get(ChatMemory.CONVERSATION_ID);
    String conversationId = convId instanceof String s ? s : null;
    try {
      return ScopedValue.where(REASONING_HOLDER, holder)
          .where(CONVERSATION_ID, conversationId)
          .call(() -> callAdvisorChain.nextCall(chatClientRequest));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
