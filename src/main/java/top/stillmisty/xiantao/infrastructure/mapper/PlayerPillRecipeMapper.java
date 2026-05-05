package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;

@Mapper
public interface PlayerPillRecipeMapper extends BaseMapper<PlayerPillRecipe> {

  /** 根据用户ID查找所有已学丹方 */
  @Select("SELECT * FROM xt_player_pill_recipe WHERE user_id = #{userId}")
  List<PlayerPillRecipe> selectByUserId(@Param("userId") Long userId);

  /** 根据用户ID和丹方模板ID查找丹方 */
  @Select(
      "SELECT * FROM xt_player_pill_recipe WHERE user_id = #{userId} AND recipe_template_id = #{recipeTemplateId}")
  Optional<PlayerPillRecipe> selectByUserIdAndRecipeTemplateId(
      @Param("userId") Long userId, @Param("recipeTemplateId") Long recipeTemplateId);

  /** 检查玩家是否已学指定丹方 */
  @Select(
      "SELECT COUNT(*) > 0 FROM xt_player_pill_recipe WHERE user_id = #{userId} AND recipe_template_id = #{recipeTemplateId}")
  boolean existsByUserIdAndRecipeTemplateId(
      @Param("userId") Long userId, @Param("recipeTemplateId") Long recipeTemplateId);
}
