package top.stillmisty.xiantao.infrastructure.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonFirstClear;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonFirstClearMapper;

@Repository
@RequiredArgsConstructor
public class DungeonFirstClearRepository {

  private final DungeonFirstClearMapper mapper;

  public DungeonFirstClear save(DungeonFirstClear firstClear) {
    mapper.insertOrUpdateSelective(firstClear);
    return firstClear;
  }

  public Optional<DungeonFirstClear> findByDungeonId(Long dungeonId) {
    return Optional.ofNullable(mapper.selectOneById(dungeonId));
  }
}
