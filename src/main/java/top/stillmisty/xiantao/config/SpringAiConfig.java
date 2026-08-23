package top.stillmisty.xiantao.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import top.stillmisty.xiantao.service.ai.ChatOptionsAdapter;
import top.stillmisty.xiantao.service.ai.DeepSeekChatOptionsAdapter;
import top.stillmisty.xiantao.service.ai.FallbackChatModel;
import top.stillmisty.xiantao.service.ai.OpenAiChatOptionsAdapter;
import top.stillmisty.xiantao.service.ai.PerTypeChatMemory;

@Configuration
@EnableConfigurationProperties(AiTierConfig.class)
public class SpringAiConfig {

  private static final int NPC_MAX_TOKENS = 1000;
  private static final int SHOP_MAX_TOKENS = 800;
  private static final int SPIRIT_MAX_TOKENS = 1200;
  private static final int SECT_MAX_TOKENS = 600;
  private static final int DUNGEON_MAX_TOKENS = 1500;
  private static final int GENERIC_MAX_TOKENS = 400;

  /** AI 请求重试：最多 2 次、1s 起步指数退避、上限 5s（框架默认 10 次最长 180s 会长时间阻塞玩家） */
  private static RetryTemplate aiRetryTemplate() {
    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .maxRetries(2)
            .includes(TransientAiException.class)
            .includes(ResourceAccessException.class)
            .delay(Duration.ofSeconds(1))
            .multiplier(2.0)
            .maxDelay(Duration.ofSeconds(5))
            .build();
    return new RetryTemplate(retryPolicy);
  }

  @Bean
  @Primary
  public ChatMemory chatMemory(ChatMemoryRepository repository) {
    return new PerTypeChatMemory(repository);
  }

  // ---- ChatModel 手动构建（仅实例化 chat-models 中配置的）----

  @Bean
  @Conditional(ChatModelConfigured.DeepSeek.class)
  public DeepSeekChatModel deepSeekChatModel(
      DeepSeekChatProperties chatProperties,
      @Value("${spring.ai.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
      @Value("${spring.ai.deepseek.api-key}") String apiKey) {
    // 显式配置 HTTP 超时：默认无超时会导致挂起连接永久占用虚拟线程
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
    requestFactory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());

    DeepSeekApi deepSeekApi =
        DeepSeekApi.builder()
            .baseUrl(baseUrl)
            .apiKey(new SimpleApiKey(apiKey))
            .completionsPath(chatProperties.getCompletionsPath())
            .betaPrefixPath(chatProperties.getBetaPrefixPath())
            .restClientBuilder(RestClient.builder().requestFactory(requestFactory))
            .responseErrorHandler(RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER)
            .build();

    return DeepSeekChatModel.builder()
        .deepSeekApi(deepSeekApi)
        .options(chatProperties.toOptions())
        .retryTemplate(aiRetryTemplate())
        .build();
  }

  // OpenAI 走官方 SDK 客户端，重试由其 max-retries 配置控制
  @Bean
  @Conditional(ChatModelConfigured.OpenAi.class)
  public OpenAiChatModel openAiChatModel(OpenAiChatProperties chatProperties) {
    return OpenAiChatModel.builder().options(chatProperties.toOptions()).build();
  }

  // ---- Advisors ----

  @Bean
  public ToolCallingAdvisor toolCallingAdvisor(ToolCallingManager toolCallingManager) {
    return ToolCallingAdvisor.builder().toolCallingManager(toolCallingManager).build();
  }

  @Bean
  public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
    return MessageChatMemoryAdvisor.builder(chatMemory).build();
  }

  // ---- ChatOptionsAdapter 注册 ----

  @Bean
  public ChatOptionsAdapter deepSeekChatOptionsAdapter() {
    return new DeepSeekChatOptionsAdapter();
  }

  @Bean
  public ChatOptionsAdapter openAiChatOptionsAdapter() {
    return new OpenAiChatOptionsAdapter();
  }

  // ---- ChatClient（每个 Bean 持有自己 tier 的 FallbackChatModel）----

  @Bean
  public ChatClient chatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().light();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(
            DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(GENERIC_MAX_TOKENS))
        .build();
  }

  @Bean
  @Primary
  public ChatClient npcChatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().standard();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(NPC_MAX_TOKENS))
        .build();
  }

  @Bean
  public ChatClient shopChatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      ToolCallingAdvisor toolCallingAdvisor,
      MessageChatMemoryAdvisor messageChatMemoryAdvisor,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().standard();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(
            DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(SHOP_MAX_TOKENS))
        .defaultAdvisors(toolCallingAdvisor, messageChatMemoryAdvisor)
        .build();
  }

  @Bean
  public ChatClient spiritChatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      ToolCallingAdvisor toolCallingAdvisor,
      MessageChatMemoryAdvisor messageChatMemoryAdvisor,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().heavy();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(
            DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(SPIRIT_MAX_TOKENS))
        .defaultAdvisors(toolCallingAdvisor, messageChatMemoryAdvisor)
        .build();
  }

  @Bean
  public ChatClient sectChatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      ToolCallingAdvisor toolCallingAdvisor,
      MessageChatMemoryAdvisor messageChatMemoryAdvisor,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().standard();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(
            DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(SECT_MAX_TOKENS))
        .defaultAdvisors(toolCallingAdvisor, messageChatMemoryAdvisor)
        .build();
  }

  @Bean
  public ChatClient dungeonChatClient(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      ToolCallingAdvisor toolCallingAdvisor,
      MessageChatMemoryAdvisor messageChatMemoryAdvisor,
      List<ChatOptionsAdapter> adapters) {
    var t = config.tiers().heavy();
    return ChatClient.builder(createFallback(dsProvider, oaProvider, config, t.openai(), adapters))
        .defaultOptions(
            DeepSeekChatOptions.builder().model(t.deepseek()).maxTokens(DUNGEON_MAX_TOKENS))
        .defaultAdvisors(toolCallingAdvisor, messageChatMemoryAdvisor)
        .build();
  }

  private static FallbackChatModel createFallback(
      ObjectProvider<DeepSeekChatModel> dsProvider,
      ObjectProvider<OpenAiChatModel> oaProvider,
      AiTierConfig config,
      @Nullable String fallbackModel,
      List<ChatOptionsAdapter> adapters) {
    Map<String, ChatModel> registry = new HashMap<>();
    ChatModel ds = dsProvider.getIfUnique();
    if (ds != null) {
      registry.put("deepseek", ds);
    }
    ChatModel oa = oaProvider.getIfUnique();
    if (oa != null) {
      registry.put("openai", oa);
    }
    List<ChatModel> ordered = new ArrayList<>();
    for (String name : config.chatModels()) {
      ChatModel model = registry.get(name);
      if (model != null) {
        ordered.add(model);
      }
    }
    if (ordered.isEmpty()) {
      throw new IllegalStateException(
          "No ChatModel available for configured chat-models: " + config.chatModels());
    }
    return new FallbackChatModel(ordered, fallbackModel, adapters);
  }

  // ---- 条件：仅当 chat-models 列表中包含指定名称时才实例化对应的 ChatModel ----

  static class ChatModelConfigured implements Condition {
    static class DeepSeek extends ChatModelConfigured {
      public DeepSeek() {
        super("deepseek");
      }
    }

    static class OpenAi extends ChatModelConfigured {
      public OpenAi() {
        super("openai");
      }
    }

    private final String modelName;

    ChatModelConfigured(String modelName) {
      this.modelName = modelName;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      String[] models =
          context.getEnvironment().getProperty("xiantao.ai.chat-models", String[].class);
      return models != null && Arrays.asList(models).contains(modelName);
    }
  }
}
