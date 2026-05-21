package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record PlayerItemsVO(
    @JsonPropertyDescription("玩家未装备的装备列表") List<EquipmentInfo> equipments,
    @JsonPropertyDescription("玩家可出售的堆叠物品列表") List<StackableInfo> items) {

  public record EquipmentInfo(
      @JsonPropertyDescription("装备ID") long id,
      @JsonPropertyDescription("装备名称") String name,
      @JsonPropertyDescription("稀有度") String rarity,
      @JsonPropertyDescription("锻造等级") int forgeLevel,
      @JsonPropertyDescription("词缀摘要") String affixSummary) {}

  public record StackableInfo(
      @JsonPropertyDescription("物品ID") long id,
      @JsonPropertyDescription("物品名称") String name,
      @JsonPropertyDescription("持有数量") int quantity,
      @JsonPropertyDescription("物品类型") String itemType,
      @JsonPropertyDescription("是否可回收") boolean tradable) {}
}
