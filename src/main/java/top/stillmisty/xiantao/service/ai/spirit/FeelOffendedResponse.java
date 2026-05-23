package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 被冒犯情绪反应结果。
 *
 * <p>当主人言行不当触发地灵不满时调用。好感度下降幅度 = severity 值。 地灵的情绪表达通过对话文本自然体现，不受系统状态的硬编码约束。
 * 仅当对话中确实出现冒犯性内容时才调用，不要无理由地频繁触发。
 */
public record FeelOffendedResponse(
    @JsonPropertyDescription("触发冒犯的具体原因描述") String reason,
    @JsonPropertyDescription("冒犯程度（1-5），好感度下降幅度等于此值") int severity) {}
