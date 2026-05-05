package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;

/** 属性变化 VO */
@Data
@Builder
public class AttributeChange {
  private int strChange;
  private int conChange;
  private int agiChange;
  private int wisChange;
  private int attackChange;
  private int defenseChange;
  private int maxHpChange;
}
