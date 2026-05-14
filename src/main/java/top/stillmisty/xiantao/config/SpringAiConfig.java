package top.stillmisty.xiantao.config;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAutoConfigurationUtil;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.stillmisty.xiantao.service.ai.ReasoningPreservingOpenAIClient;
import top.stillmisty.xiantao.service.ai.ReasoningScopeAdvisor;

/** Spring AI 配置类 */
@Configuration
public class SpringAiConfig {

  /** 通用 NPC ChatClient（支持 reasoning_content 传递，max-tokens: 1000） */
  @Bean
  public ChatClient npcChatClient(
      OpenAiCommonProperties commonProperties,
      OpenAiChatProperties chatProperties,
      ToolCallingManager toolCallingManager,
      ObjectProvider<ObservationRegistry> observationRegistry,
      ObjectProvider<ToolExecutionEligibilityPredicate> predicate) {

    var resolved =
        OpenAiAutoConfigurationUtil.resolveCommonProperties(commonProperties, chatProperties);

    OpenAIClient baseClient =
        OpenAiSetup.setupSyncClient(
            resolved.getBaseUrl(),
            resolved.getApiKey(),
            resolved.getCredential(),
            resolved.getMicrosoftDeploymentName(),
            resolved.getMicrosoftFoundryServiceVersion(),
            resolved.getOrganizationId(),
            resolved.isMicrosoftFoundry(),
            resolved.isGitHubModels(),
            resolved.getModel(),
            resolved.getTimeout(),
            resolved.getMaxRetries(),
            resolved.getProxy(),
            resolved.getCustomHeaders());

    OpenAIClientAsync baseAsyncClient =
        OpenAiSetup.setupAsyncClient(
            resolved.getBaseUrl(),
            resolved.getApiKey(),
            resolved.getCredential(),
            resolved.getMicrosoftDeploymentName(),
            resolved.getMicrosoftFoundryServiceVersion(),
            resolved.getOrganizationId(),
            resolved.isMicrosoftFoundry(),
            resolved.isGitHubModels(),
            resolved.getModel(),
            resolved.getTimeout(),
            resolved.getMaxRetries(),
            resolved.getProxy(),
            resolved.getCustomHeaders());

    OpenAIClient reasoningClient = new ReasoningPreservingOpenAIClient(baseClient);

    OpenAiChatModel npcChatModel =
        OpenAiChatModel.builder()
            .openAiClient(reasoningClient)
            .openAiClientAsync(baseAsyncClient)
            .options(chatProperties.toOptions())
            .toolCallingManager(toolCallingManager)
            .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
            .toolExecutionEligibilityPredicate(
                predicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
            .build();

    return ChatClient.builder(npcChatModel).defaultAdvisors(new ReasoningScopeAdvisor()).build();
  }

  /** 通用美化 ChatClient（控制token长度为150） */
  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.defaultOptions(OpenAiChatOptions.builder().maxTokens(150)).build();
  }
}
