package top.stillmisty.xiantao.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/** 宗门命名师 — 为新建宗门生成诗号、道统与宗灵人格；LLM 失败时返回默认身份。 */
@Slf4j
@Component
public class SectIdentityGenerator {

  private final ChatClient npcChatClient;

  public SectIdentityGenerator(ChatClient npcChatClient) {
    this.npcChatClient = npcChatClient;
  }

  /** 生成宗门身份：[诗号, 道统, 宗灵人格]。调用方须先用 {@link PromptSanitizer} 净化玩家可控文本。 */
  public String[] generate(String name, String ethosDesc) {
    try {
      String prompt =
          """
                你是一位修仙世界的宗门命名师。请根据以下信息为宗门生成诗号、道统和宗灵人格。

                宗门名称：%s
                道统描述：%s

                请严格按以下格式回复（每行一项）：
                诗号：xxx
                道统：xxx
                宗灵人格：xxx

                要求：
                - 诗号：4-7言对仗句，体现宗门气质
                - 道统：100字以内的宗门修行理念简述
                - 宗灵人格：50字以内的宗灵人格种子描述
                """
              .formatted(
                  name,
                  ethosDesc != null && !ethosDesc.isBlank() ? ethosDesc : "无特殊描述，请根据宗门名称自由发挥");

      String response =
          npcChatClient.prompt().system("你是修仙世界的宗门命名师，擅长为宗门赋予灵性身份。").user(prompt).call().content();

      if (response == null || response.isBlank()) {
        return defaultIdentity(name);
      }

      String verse = "";
      String ethos = "";
      String personality = "";

      for (String line : response.lines().toList()) {
        String trimmed = line.trim();
        if (trimmed.startsWith("诗号：") || trimmed.startsWith("诗号:")) {
          verse = extractSuffix(trimmed);
        } else if (trimmed.startsWith("道统：") || trimmed.startsWith("道统:")) {
          ethos = extractSuffix(trimmed);
        } else if (trimmed.startsWith("宗灵人格：") || trimmed.startsWith("宗灵人格:")) {
          personality = extractSuffix(trimmed);
        }
      }

      if (ethos.isBlank()) ethos = "以" + name + "之名，问道长生。";
      if (personality.isBlank()) personality = "沉稳大气的宗门意志";

      if (verse.length() > 100) verse = verse.substring(0, 100);
      if (personality.length() > 100) personality = personality.substring(0, 100);
      if (ethos.length() > 200) ethos = ethos.substring(0, 200);

      return new String[] {verse, ethos, personality};
    } catch (Exception e) {
      log.warn("LLM 生成宗门身份失败，使用默认值", e);
      return defaultIdentity(name);
    }
  }

  private static String[] defaultIdentity(String name) {
    return new String[] {"", "以" + name + "之名，问道长生。", "沉稳大气的宗门意志"};
  }

  private static String extractSuffix(String trimmed) {
    int fullWidthIndex = trimmed.indexOf('：');
    if (fullWidthIndex >= 0) {
      return trimmed.substring(fullWidthIndex + 1).trim();
    }
    int halfWidthIndex = trimmed.indexOf(':');
    if (halfWidthIndex >= 0) {
      return trimmed.substring(halfWidthIndex + 1).trim();
    }
    return trimmed;
  }
}
