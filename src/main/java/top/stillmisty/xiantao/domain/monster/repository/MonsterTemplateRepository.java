package top.stillmisty.xiantao.domain.monster.repository;

import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;

import java.util.List;
import java.util.Optional;

public interface MonsterTemplateRepository {

    Optional<MonsterTemplate> findById(Long id);

    List<MonsterTemplate> findAll();

    List<MonsterTemplate> findByIds(List<Long> ids);

    MonsterTemplate save(MonsterTemplate template);

    void deleteById(Long id);
}
