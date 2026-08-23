package top.stillmisty.xiantao.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** AI 分级配置。chatModels 缺失会导致启动失败，避免运行期 NPE。 */
@ConfigurationProperties(prefix = "xiantao.ai")
public record AiTierConfig(List<String> chatModels, Tiers tiers) {

  public AiTierConfig {
    if (chatModels == null || chatModels.isEmpty()) {
      throw new IllegalArgumentException("xiantao.ai.chat-models 配置缺失");
    }
    if (tiers == null
        || tiers.heavy() == null
        || tiers.standard() == null
        || tiers.light() == null) {
      throw new IllegalArgumentException("xiantao.ai.tiers 配置不完整（需要 heavy/standard/light）");
    }
  }

  public record Tiers(TierConfig heavy, TierConfig standard, TierConfig light) {}

  public record TierConfig(String deepseek, String openai) {}
}
