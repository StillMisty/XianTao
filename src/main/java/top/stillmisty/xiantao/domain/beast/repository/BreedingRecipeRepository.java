package top.stillmisty.xiantao.domain.beast.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.beast.entity.BreedingRecipe;

public interface BreedingRecipeRepository {

  /** 查询匹配指定 tag 集合的所有配方（required_tags 是 tags 的子集即命中） */
  List<BreedingRecipe> findMatchingRecipes(List<String> tags);
}
