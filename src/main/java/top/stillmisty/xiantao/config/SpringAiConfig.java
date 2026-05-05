package top.stillmisty.xiantao.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring AI 配置类 */
@Configuration
public class SpringAiConfig {

  /** 创建地灵专用 ChatClient */
  @Bean
  public ChatClient spiritChatClient(ChatClient.Builder builder) {
    return builder.build();
  }

  /** 通用美化 ChatClient（控制token长度为150） */
  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.defaultOptions(OpenAiChatOptions.builder().maxTokens(150)).build();
  }
}
