package top.stillmisty.xiantao.domain.forge.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 玩家已学锻造图纸实体 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_player_forging_recipe")
public class PlayerForgingRecipe {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;
  private Long blueprintTemplateId;
  private Long equipmentTemplateId;

  @Column(onInsertValue = "now()")
  private LocalDateTime learnTime;

  public boolean isRecipeFor(Long templateId) {
    return blueprintTemplateId != null && blueprintTemplateId.equals(templateId);
  }

  public static PlayerForgingRecipe create(
      Long userId, Long blueprintTemplateId, Long equipmentTemplateId) {
    PlayerForgingRecipe recipe = new PlayerForgingRecipe();
    recipe.userId = userId;
    recipe.blueprintTemplateId = blueprintTemplateId;
    recipe.equipmentTemplateId = equipmentTemplateId;
    recipe.learnTime = LocalDateTime.now();
    return recipe;
  }
}
