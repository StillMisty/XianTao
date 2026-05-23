package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 拆除地块操作结果。
 *
 * <p>拆除后该地块恢复为空地（EMPTY），资源不返还。不可拆除已种植或已孵化中的地块。
 */
public record RemoveCellResponse(
    @JsonPropertyDescription("被拆除的地块编号") String position,
    @JsonPropertyDescription("拆除前的地块类型（灵田/兽栏），用于确认操作") String type) {}
