package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 福地地块状态查询结果。
 *
 * <p>工具调用成功后，LLM 应优先从 {@code emptyCellIds} 中选择地块编号进行种植/建造。 若所有地块已满（{@code emptyCount ==
 * 0}），提示主人先拆除不需要的地块。
 */
public record CheckFudiCellsResponse(
    @JsonPropertyDescription("福地总地块数") int totalCells,
    @JsonPropertyDescription("已被占用（灵田/兽栏）的地块数") int occupiedCount,
    @JsonPropertyDescription("尚未开发可用的空地块数") int emptyCount,
    @JsonPropertyDescription("可用的空地块编号列表（如 [1, 3, 5]）。若为空列表，所有地块已在用，需先拆除部分再操作")
        List<Integer> emptyCellIds) {}
