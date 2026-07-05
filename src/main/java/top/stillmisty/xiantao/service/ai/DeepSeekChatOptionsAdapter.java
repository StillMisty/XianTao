package top.stillmisty.xiantao.service.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/** DeepSeekChatModel 适配器 */
public class DeepSeekChatOptionsAdapter implements ChatOptionsAdapter {

  @Override
  public Class<? extends ChatModel> supportedType() {
    return DeepSeekChatModel.class;
  }

  @Override
  public Prompt adaptPrompt(Prompt prompt, String overrideModel) {
    ChatOptions opts = prompt.getOptions();
    var builder = DeepSeekChatOptions.builder().model(overrideModel);
    if (opts != null) {
      if (opts.getMaxTokens() != null) builder.maxTokens(opts.getMaxTokens());
      if (opts.getTemperature() != null) builder.temperature(opts.getTemperature());
      if (opts.getTopP() != null) builder.topP(opts.getTopP());
      if (opts.getStopSequences() != null) builder.stop(opts.getStopSequences());
      if (opts.getFrequencyPenalty() != null) builder.frequencyPenalty(opts.getFrequencyPenalty());
      if (opts.getPresencePenalty() != null) builder.presencePenalty(opts.getPresencePenalty());
      // 保留 ToolCallingAdvisor 注入的工具回调
      if (opts instanceof ToolCallingChatOptions toolOpts) {
        var callbacks = toolOpts.getToolCallbacks();
        if (callbacks != null && !callbacks.isEmpty()) {
          builder.toolCallbacks(callbacks);
        }
        var toolCtx = toolOpts.getToolContext();
        if (toolCtx != null && !toolCtx.isEmpty()) {
          builder.toolContext(toolCtx);
        }
      }
    }
    return new Prompt(prompt.getInstructions(), builder.build());
  }
}
