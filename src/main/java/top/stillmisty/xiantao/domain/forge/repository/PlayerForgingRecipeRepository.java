package top.stillmisty.xiantao.domain.forge.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.forge.entity.PlayerForgingRecipe;

public interface PlayerForgingRecipeRepository {
  Optional<PlayerForgingRecipe> findById(Long id);

  List<PlayerForgingRecipe> findByUserId(Long userId);

  Optional<PlayerForgingRecipe> findByUserIdAndBlueprintTemplateId(
      Long userId, Long blueprintTemplateId);

  Optional<PlayerForgingRecipe> findByUserIdAndEquipmentTemplateId(
      Long userId, Long equipmentTemplateId);

  boolean existsByUserIdAndBlueprintTemplateId(Long userId, Long blueprintTemplateId);

  PlayerForgingRecipe save(PlayerForgingRecipe recipe);

  void deleteById(Long id);
}
