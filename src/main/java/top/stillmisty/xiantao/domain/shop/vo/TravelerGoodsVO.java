package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/** 旅行商人临时货摊视图 — 供 LLM 工具返回给模型。 */
public record TravelerGoodsVO(
    @JsonPropertyDescription("旅行商人的名号") String merchantName,
    @JsonPropertyDescription("货摊剩余有效时间（分钟），过期后商人离开") long remainingMinutes,
    @JsonPropertyDescription("货摊上的货物清单") List<TravelerGood> goods) {

  /** 单件货物：编号为 item_template 的编号，价格与库存由程序计算 */
  public record TravelerGood(
      @JsonPropertyDescription("货物编号") long templateId,
      @JsonPropertyDescription("货物名称，购买时使用此名称") String name,
      @JsonPropertyDescription("货物类别") String type,
      @JsonPropertyDescription("单价（灵石）") long unitPrice,
      @JsonPropertyDescription("剩余库存") int stock) {}
}
