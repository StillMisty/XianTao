package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerPillRecipeMapper;

/** 玩家已学丹方仓储实现 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerPillRecipeRepositoryImpl implements PlayerPillRecipeRepository {

  private final PlayerPillRecipeMapper mapper;

  @Override
  public Optional<PlayerPillRecipe> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<PlayerPillRecipe> findByUserId(Long userId) {
    return mapper.selectByUserId(userId);
  }

  @Override
  public Optional<PlayerPillRecipe> findByUserIdAndRecipeTemplateId(
      Long userId, Long recipeTemplateId) {
    return mapper.selectByUserIdAndRecipeTemplateId(userId, recipeTemplateId);
  }

  @Override
  public boolean existsByUserIdAndRecipeTemplateId(Long userId, Long recipeTemplateId) {
    return mapper.existsByUserIdAndRecipeTemplateId(userId, recipeTemplateId);
  }

  @Override
  public PlayerPillRecipe save(PlayerPillRecipe recipe) {
    if (recipe.getId() == null || !existsById(recipe.getId())) {
      mapper.insert(recipe);
    } else {
      mapper.update(recipe);
    }
    return recipe;
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  @Override
  public void deleteByUserIdAndRecipeTemplateId(Long userId, Long recipeTemplateId) {
    mapper
        .selectByUserIdAndRecipeTemplateId(userId, recipeTemplateId)
        .ifPresent(recipe -> mapper.deleteById(recipe.getId()));
  }

  private boolean existsById(Long id) {
    return mapper.selectOneById(id) != null;
  }
}
