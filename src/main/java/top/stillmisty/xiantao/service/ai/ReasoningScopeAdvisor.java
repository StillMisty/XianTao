package top.stillmisty.xiantao.service.ai;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class ReasoningScopeAdvisor implements CallAdvisor {

  static final ScopedValue<AtomicReference<String>> REASONING_HOLDER = ScopedValue.newInstance();

  @Override
  public String getName() {
    return "ReasoningScopeAdvisor";
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public ChatClientResponse adviseCall(
      ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    AtomicReference<String> holder = new AtomicReference<>();
    try {
      return ScopedValue.where(REASONING_HOLDER, holder)
          .call(() -> callAdvisorChain.nextCall(chatClientRequest));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
