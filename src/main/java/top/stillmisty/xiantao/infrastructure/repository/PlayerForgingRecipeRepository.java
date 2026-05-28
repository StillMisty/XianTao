package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.forge.entity.PlayerForgingRecipe;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerForgingRecipeMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerForgingRecipeRepository {

  private final PlayerForgingRecipeMapper mapper;

  public Optional<PlayerForgingRecipe> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<PlayerForgingRecipe> findByUserId(Long userId) {
    return mapper.selectByUserId(userId);
  }

  public Optional<PlayerForgingRecipe> findByUserIdAndBlueprintTemplateId(
      Long userId, Long blueprintTemplateId) {
    return mapper.selectByUserIdAndBlueprintTemplateId(userId, blueprintTemplateId);
  }

  public Optional<PlayerForgingRecipe> findByUserIdAndEquipmentTemplateId(
      Long userId, Long equipmentTemplateId) {
    return mapper.selectByUserIdAndEquipmentTemplateId(userId, equipmentTemplateId);
  }

  public boolean existsByUserIdAndBlueprintTemplateId(Long userId, Long blueprintTemplateId) {
    return mapper.existsByUserIdAndBlueprintTemplateId(userId, blueprintTemplateId);
  }

  public PlayerForgingRecipe save(PlayerForgingRecipe recipe) {
    mapper.insertOrUpdateSelective(recipe);
    return recipe;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }
}
