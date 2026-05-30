package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonTemplateTableDef.DUNGEON_TEMPLATE;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonTemplateMapper;

@Repository
@RequiredArgsConstructor
public class DungeonTemplateRepository {

  private final DungeonTemplateMapper mapper;

  public DungeonTemplate save(DungeonTemplate template) {
    mapper.insertOrUpdateSelective(template);
    return template;
  }

  public Optional<DungeonTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<DungeonTemplate> findByName(String name) {
    QueryWrapper qw = QueryWrapper.create().where(DUNGEON_TEMPLATE.NAME.eq(name));
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  public List<DungeonTemplate> findAll() {
    return mapper.selectAll();
  }

  public List<DungeonTemplate> findActive() {
    QueryWrapper qw = QueryWrapper.create().where(DUNGEON_TEMPLATE.IS_ACTIVE.eq(true));
    return mapper.selectListByQuery(qw);
  }
}
