package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 建造/开辟地块操作结果。
 *
 * <p>在空地块上建造灵田（FARM）或兽栏（PEN）。该地块必须当前为空（EMPTY）， 建议先调用 checkFudiCells 确认可用空地块编号。
 */
public record BuildCellResponse(
    @JsonPropertyDescription("建造的地块编号") String position,
    @JsonPropertyDescription("建造后的地块类型：灵田 或 兽栏") String cellType) {}
