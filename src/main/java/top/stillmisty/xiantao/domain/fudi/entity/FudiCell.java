package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.CellConfigTypeHandler;

/** 福地地块实体 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_fudi_cell")
public class FudiCell {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 关联福地ID */
  private Long fudiId;

  /** 地块编号（从1开始） */
  private Integer cellId;

  /** 地块类型：empty/farm/pen */
  private CellType cellType;

  /** 地块等级（1-5） */
  private Integer cellLevel;

  /** 建筑专有属性（JSONB） */
  @Column(typeHandler = CellConfigTypeHandler.class)
  private CellConfig config;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务方法 =====================

  /** 创建空地块 */
  public static FudiCell createEmpty(Long fudiId, Integer cellId) {
    FudiCell cell = new FudiCell();
    cell.setFudiId(fudiId);
    cell.setCellId(cellId);
    cell.setCellType(CellType.EMPTY);
    cell.setCellLevel(1);
    cell.setConfig(new CellConfig.EmptyConfig());
    return cell;
  }

  /** 是否为空地块 */
  public boolean isEmpty() {
    return cellType == CellType.EMPTY;
  }

  /** 清空所有配置 */
  public void clearConfig() {
    this.config = new CellConfig.EmptyConfig();
  }

  // ===================== 产出物品便捷方法（仅 PenConfig 有效） =====================

  public List<CellConfig.ProductionItem> getProductionStored() {
    if (config instanceof CellConfig.PenConfig pen) {
      return pen.productionStored();
    }
    return List.of();
  }

  public void addProductionItem(Long templateId, String name, int quantity) {
    if (config instanceof CellConfig.PenConfig pen) {
      pen.addProductionItem(templateId, name, quantity);
    }
  }

  public void clearProductionStored() {
    if (config instanceof CellConfig.PenConfig pen) {
      setConfig(pen.withClearedProduction());
    }
  }

  public int getTotalProductionQuantity() {
    if (config instanceof CellConfig.PenConfig pen) {
      return pen.totalProductionQuantity();
    }
    return 0;
  }
}
