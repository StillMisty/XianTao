package top.stillmisty.xiantao.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring AI 配置类
 */
@Configuration
@EnableAspectJAutoProxy
public class SpringAiConfig {

    /**
     * 创建地灵专用 ChatClient
     */
    @Bean
    public ChatClient spiritChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
