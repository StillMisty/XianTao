package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MonsterTemplateMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MonsterTemplateRepositoryImpl implements MonsterTemplateRepository {

  private final MonsterTemplateMapper mapper;

  @Override
  public Optional<MonsterTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<MonsterTemplate> findAll() {
    return mapper.selectAll();
  }

  @Override
  public List<MonsterTemplate> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return mapper.selectListByIds(ids);
  }

  @Override
  public MonsterTemplate save(MonsterTemplate template) {
    if (template.getId() == null) {
      mapper.insert(template);
    } else {
      mapper.update(template);
    }
    return template;
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }
}
