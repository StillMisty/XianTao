package top.stillmisty.xiantao.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring AI 配置类
 * 配置地灵系统所需的 ChatClient
 */
@Configuration
@EnableAspectJAutoProxy
public class SpringAiConfig {

    /**
     * 创建地灵专用 ChatClient
     * 使用默认的 OpenAI 兼容 API（已配置 deepseek-chat）
     */
    @Bean
    public ChatClient spiritChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
