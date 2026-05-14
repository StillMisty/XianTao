package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.forge.entity.PlayerForgingRecipe;

@Mapper
public interface PlayerForgingRecipeMapper extends BaseMapper<PlayerForgingRecipe> {

  @Select("SELECT * FROM xt_player_forging_recipe WHERE user_id = #{userId}")
  List<PlayerForgingRecipe> selectByUserId(@Param("userId") Long userId);

  @Select(
      "SELECT * FROM xt_player_forging_recipe WHERE user_id = #{userId} AND blueprint_template_id = #{blueprintTemplateId}")
  Optional<PlayerForgingRecipe> selectByUserIdAndBlueprintTemplateId(
      @Param("userId") Long userId, @Param("blueprintTemplateId") Long blueprintTemplateId);

  @Select(
      "SELECT * FROM xt_player_forging_recipe WHERE user_id = #{userId} AND equipment_template_id = #{equipmentTemplateId}")
  Optional<PlayerForgingRecipe> selectByUserIdAndEquipmentTemplateId(
      @Param("userId") Long userId, @Param("equipmentTemplateId") Long equipmentTemplateId);

  @Select(
      "SELECT COUNT(*) > 0 FROM xt_player_forging_recipe WHERE user_id = #{userId} AND blueprint_template_id = #{blueprintTemplateId}")
  boolean existsByUserIdAndBlueprintTemplateId(
      @Param("userId") Long userId, @Param("blueprintTemplateId") Long blueprintTemplateId);
}
