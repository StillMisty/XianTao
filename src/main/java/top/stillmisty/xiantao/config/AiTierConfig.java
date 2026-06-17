package top.stillmisty.xiantao.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xiantao.ai")
public record AiTierConfig(List<String> chatModels, Tiers tiers) {

  public record Tiers(TierConfig heavy, TierConfig standard, TierConfig light) {}

  public record TierConfig(String deepseek, String openai) {}
}
