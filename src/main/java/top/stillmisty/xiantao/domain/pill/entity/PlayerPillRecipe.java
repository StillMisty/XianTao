package top.stillmisty.xiantao.domain.pill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;

/** 玩家已学丹方实体 */
@Data
@Table("xt_player_pill_recipe")
public class PlayerPillRecipe {

  /** 主键ID */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 丹方卷轴模板ID */
  private Long recipeTemplateId;

  /** 成品丹药模板ID */
  private Long resultItemId;

  /** 学习时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime learnTime;

  public boolean isRecipeFor(Long templateId) {
    return recipeTemplateId != null && recipeTemplateId.equals(templateId);
  }

  /** 创建PlayerPillRecipe实例 */
  public static PlayerPillRecipe create(Long userId, Long recipeTemplateId, Long resultItemId) {
    PlayerPillRecipe recipe = new PlayerPillRecipe();
    recipe.userId = userId;
    recipe.recipeTemplateId = recipeTemplateId;
    recipe.resultItemId = resultItemId;
    recipe.learnTime = LocalDateTime.now();
    return recipe;
  }
}
