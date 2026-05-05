package top.stillmisty.xiantao.domain.pill.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;

/** 玩家已学丹方仓储接口 */
public interface PlayerPillRecipeRepository {

  /** 根据ID查找丹方 */
  Optional<PlayerPillRecipe> findById(Long id);

  /** 根据用户ID查找所有已学丹方 */
  List<PlayerPillRecipe> findByUserId(Long userId);

  /** 根据用户ID和丹方模板ID查找丹方 */
  Optional<PlayerPillRecipe> findByUserIdAndRecipeTemplateId(Long userId, Long recipeTemplateId);

  /** 检查玩家是否已学指定丹方 */
  boolean existsByUserIdAndRecipeTemplateId(Long userId, Long recipeTemplateId);

  /** 保存丹方 */
  PlayerPillRecipe save(PlayerPillRecipe recipe);

  /** 删除丹方 */
  void deleteById(Long id);

  /** 根据用户ID和丹方模板ID删除丹方 */
  void deleteByUserIdAndRecipeTemplateId(Long userId, Long recipeTemplateId);
}
