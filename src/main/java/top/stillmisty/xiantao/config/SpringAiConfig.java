package top.stillmisty.xiantao.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import top.stillmisty.xiantao.service.ai.PerTypeChatMemory;
import top.stillmisty.xiantao.service.ai.ReasoningScopeAdvisor;

@Configuration
public class SpringAiConfig {

  private static final int SPIRIT_MAX_TOKENS = 1200;
  private static final int SHOP_MAX_TOKENS = 800;
  private static final int SECT_MAX_TOKENS = 600;

  @Bean
  @Primary
  public ChatMemory chatMemory(ChatMemoryRepository repository) {
    return new PerTypeChatMemory(repository);
  }

  @Bean
  @Primary
  public ChatClient npcChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultOptions(OpenAiChatOptions.builder().maxTokens(1000))
        .defaultAdvisors(new ReasoningScopeAdvisor())
        .build();
  }

  @Bean
  public ChatClient shopChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultOptions(OpenAiChatOptions.builder().maxTokens(SHOP_MAX_TOKENS))
        .defaultAdvisors(new ReasoningScopeAdvisor())
        .build();
  }

  @Bean
  public ChatClient spiritChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultOptions(OpenAiChatOptions.builder().maxTokens(SPIRIT_MAX_TOKENS))
        .defaultAdvisors(new ReasoningScopeAdvisor())
        .build();
  }

  @Bean
  public ChatClient sectChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
        .defaultOptions(OpenAiChatOptions.builder().maxTokens(SECT_MAX_TOKENS))
        .defaultAdvisors(new ReasoningScopeAdvisor())
        .build();
  }

  /** 通用美化 ChatClient（控制token长度为150） */
  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.defaultOptions(OpenAiChatOptions.builder().maxTokens(150)).build();
  }
}
