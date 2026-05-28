package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.beast.entity.BreedingRecipe;
import top.stillmisty.xiantao.infrastructure.mapper.BreedingRecipeMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BreedingRecipeRepository {

  private final BreedingRecipeMapper mapper;

  public List<BreedingRecipe> findMatchingRecipes(List<String> tags) {
    if (tags == null || tags.isEmpty()) return List.of();
    String jsonArray =
        "[" + String.join(",", tags.stream().map(t -> "\"" + t + "\"").toList()) + "]";
    return mapper.selectListByQuery(
        QueryWrapper.create().where("required_tags <@ ?::jsonb", jsonArray));
  }
}
