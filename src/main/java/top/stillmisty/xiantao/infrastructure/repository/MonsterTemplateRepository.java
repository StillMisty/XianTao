package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.infrastructure.mapper.MonsterTemplateMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MonsterTemplateRepository {

  private final MonsterTemplateMapper mapper;

  public Optional<MonsterTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<MonsterTemplate> findByName(String name) {
    return Optional.ofNullable(mapper.selectByName(name));
  }

  public List<MonsterTemplate> findAll() {
    return mapper.selectAll();
  }

  public List<MonsterTemplate> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return mapper.selectListByIds(ids);
  }

  public MonsterTemplate save(MonsterTemplate template) {
    mapper.insertOrUpdateSelective(template);
    return template;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }
}
