package top.stillmisty.xiantao.domain.worldevent.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;

public interface WorldEventTemplateRepository {

  WorldEventTemplate save(WorldEventTemplate template);

  Optional<WorldEventTemplate> findById(Long id);

  List<WorldEventTemplate> findAll();

  List<WorldEventTemplate> findByCategory(WorldEventCategory category);

  List<WorldEventTemplate> findByScope(String scope);
}
