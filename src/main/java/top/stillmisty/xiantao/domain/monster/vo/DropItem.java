package top.stillmisty.xiantao.domain.monster.vo;

public record DropItem(DropType type, Long templateId, String name, int quantity) {

  public enum DropType {
    EQUIPMENT,
    ITEM
  }
}
