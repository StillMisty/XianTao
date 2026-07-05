package top.stillmisty.xiantao.service.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * ChatModel 适配器 — 为降级模型生成带 model 名称的 Prompt。
 *
 * <p>当主模型调用失败触发 fallback 时，需要将 Prompt 中的 options 替换为 降级模型的对应配置（model 名 +
 * maxTokens/temperature/topP）。 每个 ChatModel 类型需要自己的适配器来构造正确的 options。
 */
public interface ChatOptionsAdapter {

  /** 当前适配器支持的 ChatModel 类型 */
  Class<? extends ChatModel> supportedType();

  /**
   * 为降级模型创建新的 Prompt，替换 model 名但保留其他 options。
   *
   * @param prompt 原始 Prompt
   * @param overrideModel 降级模型的 model 名称
   * @return 替换了 model 名的新 Prompt
   */
  Prompt adaptPrompt(Prompt prompt, String overrideModel);
}
