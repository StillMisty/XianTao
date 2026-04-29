package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 探索描述生成函数
 * 用于 Spring AI function calling，生成美化的探索描述文本
 */
@Slf4j
@Component
public class ExplorationDescriptionFunction {

    /**
     * 探索描述生成函数
     * 作为一个 @Bean 注册，供 LLM 调用
     */
    @Bean
    @Description("生成探索美化的描述文本，基于地图名称、事件类型和发现物品")
    public Function<ExplorationDescriptionFunction.Request, ExplorationDescriptionFunction.Response> generateExplorationDescription() {
        return request -> {
            log.debug(
                    "生成探索描述 - MapName: {}, EventType: {}, FoundItems: {}",
                    request.mapName(), request.eventType(), request.foundItems()
            );

            // 生成美化描述
            String description = generateBeautifiedDescription(
                    request.mapName(),
                    request.eventType(),
                    request.foundItems()
            );

            return new Response(description);
        };
    }

    /**
     * 生成美化描述
     */
    private String generateBeautifiedDescription(String mapName, String eventType, List<String> foundItems) {
        StringBuilder sb = new StringBuilder();

        // 开场描述
        sb.append("在").append(mapName);

        // 根据事件类型生成描述
        switch (eventType) {
            case "发现基础材料" ->
                    sb.append("的探索中，您仔细搜寻着每一个角落。阳光透过树梢洒下，您在一丛灌木下发现了一些有用的基础材料。");
            case "发现稀有材料" ->
                    sb.append("的深处，凭借您的智慧，您敏锐地察觉到了空气中微弱的灵气波动。经过仔细搜寻，您找到了一些稀有材料！");
            case "重大发现" ->
                    sb.append("的探索中，您的智慧指引您发现了一个隐秘的角落。除了一些珍贵的稀有材料，您还在一块古老的石碑上发现了一份完整的配方！");
            case "惊人发现" ->
                    sb.append("的探索中，您的智慧令您震惊！您不仅发现了大量稀有材料，还找到一份失传已久的古老配方。更令您惊喜的是，在一处隐蔽的洞穴中，您发现了一颗散发着神秘光芒的灵蛋！");
            default -> sb.append("的探索中，您发现了一些有趣的东西。");
        }

        // 添加物品描述
        if (foundItems != null && !foundItems.isEmpty()) {
            sb.append("\n\n您获得了: ");
            sb.append(String.join("、", foundItems));
        }

        sb.append("\n\n这次探索让您对").append(mapName).append("有了更深的了解。");

        return sb.toString();
    }

    /**
     * 探索描述生成请求
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("生成探索描述的请求参数")
    public record Request(
            @JsonProperty(required = true, value = "mapName")
            @JsonPropertyDescription("地图名称，如'幽暗沼泽'")
            String mapName,

            @JsonProperty(required = true, value = "eventType")
            @JsonPropertyDescription("事件类型，如'发现基础材料'、'发现稀有材料'、'重大发现'、'惊人发现'")
            String eventType,

            @JsonProperty(value = "foundItems")
            @JsonPropertyDescription("发现的物品名称列表，如['毒龙草', '铁矿石']")
            List<String> foundItems
    ) {
    }

    /**
     * 探索描述生成响应
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Response(
            @JsonPropertyDescription("美化的探索描述文本")
            String description
    ) {
    }
}
