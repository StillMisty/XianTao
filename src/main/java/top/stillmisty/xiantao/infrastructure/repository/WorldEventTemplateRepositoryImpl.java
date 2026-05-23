package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.repository.WorldEventTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.WorldEventTemplateMapper;

@Repository
@RequiredArgsConstructor
public class WorldEventTemplateRepositoryImpl implements WorldEventTemplateRepository {

  private final WorldEventTemplateMapper worldEventTemplateMapper;

  @Override
  public WorldEventTemplate save(WorldEventTemplate template) {
    worldEventTemplateMapper.insertOrUpdateSelective(template);
    return template;
  }

  @Override
  public Optional<WorldEventTemplate> findById(Long id) {
    return Optional.ofNullable(worldEventTemplateMapper.selectOneById(id));
  }

  @Override
  public List<WorldEventTemplate> findAll() {
    return worldEventTemplateMapper.selectAll();
  }

  @Override
  public List<WorldEventTemplate> findByCategory(WorldEventCategory category) {
    return worldEventTemplateMapper.selectByCategory(category.getCode());
  }

  @Override
  public List<WorldEventTemplate> findByScope(String scope) {
    return worldEventTemplateMapper.selectByScope(scope);
  }
}
