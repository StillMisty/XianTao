package top.stillmisty.xiantao.domain.beast.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.beast.entity.MutationTraitConfig;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;

/** 变异特性配置 Repository */
public interface MutationTraitConfigRepository {

  List<MutationTraitConfig> findAllActive();

  List<MutationTraitConfig> findAvailableForBeast(List<String> beastTags, BeastQuality quality);

  Optional<MutationTraitConfig> findById(Long id);

  Optional<MutationTraitConfig> findByName(String name);

  List<MutationTraitConfig> findByCategory(String category);
}
