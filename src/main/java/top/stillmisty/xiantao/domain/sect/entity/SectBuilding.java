package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;

@EqualsAndHashCode(callSuper = true)
@Table("xt_sect_building")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class SectBuilding extends Model<SectBuilding> {

  public static SectBuilding create() {
    return new SectBuilding();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long sectId;

  private SectBuildingType buildingType;

  private Integer level;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updatedAt;

  public boolean isMaxLevel() {
    return level >= buildingType.getMaxLevel();
  }
}
