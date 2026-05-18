package top.stillmisty.xiantao.service.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.services.blocking.AdminService;
import com.openai.services.blocking.AudioService;
import com.openai.services.blocking.BatchService;
import com.openai.services.blocking.BetaService;
import com.openai.services.blocking.ChatService;
import com.openai.services.blocking.CompletionService;
import com.openai.services.blocking.ContainerService;
import com.openai.services.blocking.ConversationService;
import com.openai.services.blocking.EmbeddingService;
import com.openai.services.blocking.EvalService;
import com.openai.services.blocking.FileService;
import com.openai.services.blocking.FineTuningService;
import com.openai.services.blocking.GraderService;
import com.openai.services.blocking.ImageService;
import com.openai.services.blocking.ModelService;
import com.openai.services.blocking.ModerationService;
import com.openai.services.blocking.RealtimeService;
import com.openai.services.blocking.ResponseService;
import com.openai.services.blocking.SkillService;
import com.openai.services.blocking.UploadService;
import com.openai.services.blocking.VectorStoreService;
import com.openai.services.blocking.VideoService;
import com.openai.services.blocking.WebhookService;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ReasoningPreservingOpenAIClient implements OpenAIClient {

  private final OpenAIClient delegate;

  public ReasoningPreservingOpenAIClient(OpenAIClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public @NonNull ChatService chat() {
    return new ReasoningPreservingChatService(delegate.chat());
  }

  @Override
  public @NonNull OpenAIClientAsync async() {
    return delegate.async();
  }

  @Override
  public @NonNull CompletionService completions() {
    return delegate.completions();
  }

  @Override
  public @NonNull EmbeddingService embeddings() {
    return delegate.embeddings();
  }

  @Override
  public @NonNull FileService files() {
    return delegate.files();
  }

  @Override
  public @NonNull ImageService images() {
    return delegate.images();
  }

  @Override
  public @NonNull AudioService audio() {
    return delegate.audio();
  }

  @Override
  public @NonNull AdminService admin() {
    return delegate.admin();
  }

  @Override
  public @NonNull ModerationService moderations() {
    return delegate.moderations();
  }

  @Override
  public @NonNull ModelService models() {
    return delegate.models();
  }

  @Override
  public @NonNull FineTuningService fineTuning() {
    return delegate.fineTuning();
  }

  @Override
  public @NonNull GraderService graders() {
    return delegate.graders();
  }

  @Override
  public @NonNull VectorStoreService vectorStores() {
    return delegate.vectorStores();
  }

  @Override
  public @NonNull WebhookService webhooks() {
    return delegate.webhooks();
  }

  @Override
  public @NonNull BetaService beta() {
    return delegate.beta();
  }

  @Override
  public @NonNull BatchService batches() {
    return delegate.batches();
  }

  @Override
  public @NonNull UploadService uploads() {
    return delegate.uploads();
  }

  @Override
  public @NonNull VideoService videos() {
    return delegate.videos();
  }

  @Override
  public @NonNull ContainerService containers() {
    return delegate.containers();
  }

  @Override
  public @NonNull ConversationService conversations() {
    return delegate.conversations();
  }

  @Override
  public @NonNull EvalService evals() {
    return delegate.evals();
  }

  @Override
  public @NonNull RealtimeService realtime() {
    return delegate.realtime();
  }

  @Override
  public @NonNull ResponseService responses() {
    return delegate.responses();
  }

  @Override
  public @NonNull SkillService skills() {
    return delegate.skills();
  }

  @Override
  public OpenAIClient.@NonNull WithRawResponse withRawResponse() {
    return delegate.withRawResponse();
  }

  @Override
  public @NonNull OpenAIClient withOptions(@NonNull Consumer<com.openai.core.ClientOptions.Builder> consumer) {
    return new ReasoningPreservingOpenAIClient(delegate.withOptions(consumer));
  }

  @Override
  public void close() {
    delegate.close();
  }
}
