package top.stillmisty.xiantao.domain.dungeon.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;

public interface DungeonTemplateRepository {
  DungeonTemplate save(DungeonTemplate template);

  Optional<DungeonTemplate> findById(Long id);

  Optional<DungeonTemplate> findByName(String name);

  List<DungeonTemplate> findAll();

  List<DungeonTemplate> findActive();
}
