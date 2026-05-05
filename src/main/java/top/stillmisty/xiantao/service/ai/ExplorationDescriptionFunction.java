package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/** 探索描述生成器 调用 LLM 将结构化的探索结果美化为仙侠风格旁白 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExplorationDescriptionFunction {

  private static final String SYSTEM_PROMPT =
      """
            你是一个仙侠世界的旁白叙述者。
            根据给出的探索信息，将其改写为一段身临其境的探索描述。

            要求：
            1. 语言优美，充满仙侠意境与画面感
            2. 融入探索「从外围逐步深入核心」的推进感
            3. 只返回描述文本，不要添加任何说明、标记或前缀
            """;
  private final ChatClient chatClient;

  /** 将探索结果美化为仙侠风格旁白 */
  public Response beautify(Request request) {
    log.debug(
        "生成探索描述 - MapName: {}, EventType: {}, FoundItems: {}",
        request.mapName(),
        request.eventType(),
        request.foundItems());

    try {
      String userMessage = buildUserMessage(request);
      String description =
          chatClient.prompt().system(SYSTEM_PROMPT).user(userMessage).call().content();

      return new Response(description != null ? description.trim() : buildFallback(request));
    } catch (Exception e) {
      log.error("LLM 生成探索描述失败，使用兜底描述", e);
      return new Response(buildFallback(request));
    }
  }

  private String buildUserMessage(Request request) {
    StringBuilder sb = new StringBuilder();
    sb.append("你深入")
        .append(request.mapName())
        .append("展开探索，从外围步步推进至深处，")
        .append(request.eventType())
        .append("。\n");
    if (request.mapDescription() != null && !request.mapDescription().isEmpty()) {
      sb.append("此处").append(request.mapDescription()).append("。\n");
    }
    if (request.eventDescription() != null && !request.eventDescription().isEmpty()) {
      sb.append("探索途中，").append(request.eventDescription()).append("。\n");
    }
    if (request.expGained() != null && request.expGained() > 0) {
      sb.append("获得了").append(request.expGained()).append("点经验值");
    }
    if (request.foundItems() != null && !request.foundItems().isEmpty()) {
      if (request.expGained() != null && request.expGained() > 0) {
        sb.append("，以及：");
      } else {
        sb.append("获得了");
      }
      sb.append(String.join("、", request.foundItems()));
    }
    sb.append("。\n");
    if (request.recipeName() != null && !request.recipeName().isEmpty()) {
      sb.append("还意外发现了一份").append(request.recipeName()).append("。\n");
    }
    sb.append("请将以上探索经历改写为一段优美的仙侠风格旁白：");
    return sb.toString();
  }

  private String buildFallback(Request request) {
    StringBuilder sb = new StringBuilder();
    sb.append("在").append(request.mapName());

    switch (request.eventType()) {
      case "发现基础材料" -> sb.append("的探索中，您仔细搜寻着每一个角落，发现了一些有用的基础材料。");
      case "发现稀有材料" -> sb.append("的深处，您敏锐地察觉到了空气中微弱的灵气波动，找到了一些稀有材料！");
      case "重大发现" -> sb.append("的探索中，您发现了一个隐秘的角落，还找到了一份完整的配方！");
      case "惊人发现" -> sb.append("的探索中，您不仅发现了大量稀有材料和古老配方，还发现了一颗散发着神秘光芒的灵蛋！");
      default -> sb.append("的探索中，您发现了一些有趣的东西。");
    }

    if (request.eventDescription() != null && !request.eventDescription().isEmpty()) {
      sb.append("\n\n").append(request.eventDescription());
    }

    if (request.expGained() != null && request.expGained() > 0) {
      sb.append("\n\n获得 ").append(request.expGained()).append(" 点经验值");
    }

    if (request.foundItems() != null && !request.foundItems().isEmpty()) {
      if (request.expGained() != null && request.expGained() > 0) {
        sb.append("，以及：");
      } else {
        sb.append("\n\n获得：");
      }
      sb.append(String.join("、", request.foundItems()));
    }

    if (request.recipeName() != null && !request.recipeName().isEmpty()) {
      sb.append("\n还发现了一份").append(request.recipeName()).append("配方。");
    }

    sb.append("\n\n这次探索让您对").append(request.mapName()).append("有了更深的了解。");
    return sb.toString();
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonClassDescription("生成探索描述的请求参数")
  public record Request(
      @JsonProperty(required = true, value = "mapName") @JsonPropertyDescription("地图名称，如'幽暗沼泽'")
          String mapName,
      @JsonProperty(value = "mapDescription") @JsonPropertyDescription("地图描述文本")
          String mapDescription,
      @JsonProperty(required = true, value = "eventType")
          @JsonPropertyDescription("事件类型，如'发现基础材料'、'发现稀有材料'、'重大发现'、'惊人发现'")
          String eventType,
      @JsonProperty(value = "foundItems") @JsonPropertyDescription("发现的物品名称列表，如['毒龙草', '铁矿石']")
          List<String> foundItems,
      @JsonProperty(value = "expGained") @JsonPropertyDescription("获得的经验值（探索不获取经验时为空）")
          Long expGained,
      @JsonProperty(value = "recipeName") @JsonPropertyDescription("发现的配方名称（如有）") String recipeName,
      @JsonProperty(value = "eventDescription") @JsonPropertyDescription("探索中触发的事件描述（如有）")
          String eventDescription) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Response(@JsonPropertyDescription("美化的探索描述文本") String description) {}
}
