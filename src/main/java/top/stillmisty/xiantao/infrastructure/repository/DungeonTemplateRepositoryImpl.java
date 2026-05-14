package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonTemplateMapper;

@Repository
@RequiredArgsConstructor
public class DungeonTemplateRepositoryImpl implements DungeonTemplateRepository {

  private final DungeonTemplateMapper mapper;

  @Override
  public DungeonTemplate save(DungeonTemplate template) {
    mapper.insertOrUpdateSelective(template);
    return template;
  }

  @Override
  public Optional<DungeonTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public Optional<DungeonTemplate> findByName(String name) {
    QueryWrapper qw = new QueryWrapper().eq(DungeonTemplate::getName, name);
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  @Override
  public List<DungeonTemplate> findAll() {
    return mapper.selectAll();
  }

  @Override
  public List<DungeonTemplate> findActive() {
    QueryWrapper qw = new QueryWrapper().eq(DungeonTemplate::getIsActive, true);
    return mapper.selectListByQuery(qw);
  }
}
