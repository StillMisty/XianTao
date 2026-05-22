package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SectToolResponse(
    @JsonPropertyDescription("是否成功") boolean success,
    @JsonPropertyDescription("结果消息") String message) {}
