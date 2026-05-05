package top.stillmisty.xiantao.domain.monster.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;

public interface MonsterTemplateRepository {

  Optional<MonsterTemplate> findById(Long id);

  List<MonsterTemplate> findAll();

  List<MonsterTemplate> findByIds(List<Long> ids);

  MonsterTemplate save(MonsterTemplate template);

  void deleteById(Long id);
}
