package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.infrastructure.mapper.WorldEventTemplateMapper;

@Repository
@RequiredArgsConstructor
public class WorldEventTemplateRepository {

  private final WorldEventTemplateMapper worldEventTemplateMapper;

  public WorldEventTemplate save(WorldEventTemplate template) {
    worldEventTemplateMapper.insertOrUpdateSelective(template);
    return template;
  }

  public Optional<WorldEventTemplate> findById(Long id) {
    return Optional.ofNullable(worldEventTemplateMapper.selectOneById(id));
  }

  public List<WorldEventTemplate> findAll() {
    return worldEventTemplateMapper.selectAll();
  }

  public List<WorldEventTemplate> findByCategory(WorldEventCategory category) {
    return worldEventTemplateMapper.selectByCategory(category.getCode());
  }

  public List<WorldEventTemplate> findByScope(String scope) {
    return worldEventTemplateMapper.selectByScope(scope);
  }
}
