package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.beast.entity.table.MutationTraitConfigTableDef.MUTATION_TRAIT_CONFIG;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.beast.entity.MutationTraitConfig;
import top.stillmisty.xiantao.domain.beast.repository.MutationTraitConfigRepository;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.infrastructure.mapper.MutationTraitConfigMapper;

/** 变异特性配置 Repository 实现 */
@Repository
@RequiredArgsConstructor
public class MutationTraitConfigRepositoryImpl implements MutationTraitConfigRepository {

  private final MutationTraitConfigMapper mapper;

  @Override
  public List<MutationTraitConfig> findAllActive() {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .where(MUTATION_TRAIT_CONFIG.IS_ACTIVE.eq(true))
            .orderBy(MUTATION_TRAIT_CONFIG.SORT_ORDER.asc()));
  }

  @Override
  public List<MutationTraitConfig> findAvailableForBeast(
      List<String> beastTags, BeastQuality quality) {
    List<MutationTraitConfig> allActive = findAllActive();
    return allActive.stream()
        .filter(config -> checkTagRequirement(config.getRequiredTags(), beastTags))
        .filter(config -> checkQualityRequirement(config.getRequiredQuality(), quality))
        .toList();
  }

  @Override
  public Optional<MutationTraitConfig> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public Optional<MutationTraitConfig> findByName(String name) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(QueryWrapper.create().where(MUTATION_TRAIT_CONFIG.NAME.eq(name))));
  }

  @Override
  public List<MutationTraitConfig> findByCategory(String category) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .where(MUTATION_TRAIT_CONFIG.CATEGORY.eq(category))
            .and(MUTATION_TRAIT_CONFIG.IS_ACTIVE.eq(true))
            .orderBy(MUTATION_TRAIT_CONFIG.SORT_ORDER.asc()));
  }

  /** 检查标签要求：requiredTags 为 null 或空表示通用，否则 beastTags 必须包含全部 requiredTags */
  private boolean checkTagRequirement(List<String> requiredTags, List<String> beastTags) {
    if (requiredTags == null || requiredTags.isEmpty()) {
      return true;
    }
    if (beastTags == null || beastTags.isEmpty()) {
      return false;
    }
    return beastTags.containsAll(requiredTags);
  }

  /** 检查品质要求：requiredQuality 为 null 表示无限制，否则 beastQuality 必须 >= requiredQuality */
  private boolean checkQualityRequirement(String requiredQuality, BeastQuality beastQuality) {
    if (requiredQuality == null) {
      return true;
    }
    if (beastQuality == null) {
      return false;
    }
    try {
      BeastQuality required = BeastQuality.fromCode(requiredQuality);
      return beastQuality.ordinal() >= required.ordinal();
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
