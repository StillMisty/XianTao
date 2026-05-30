package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonSpiritStateTableDef.DUNGEON_SPIRIT_STATE;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonSpiritStateMapper;

@Repository
@RequiredArgsConstructor
public class DungeonSpiritStateRepository {

  private final DungeonSpiritStateMapper mapper;

  public DungeonSpiritState save(DungeonSpiritState state) {
    mapper.insertOrUpdateSelective(state);
    return state;
  }

  public Optional<DungeonSpiritState> findByInstanceIdAndUserId(Long instanceId, Long userId) {
    QueryWrapper qw =
        QueryWrapper.create()
            .where(DUNGEON_SPIRIT_STATE.INSTANCE_ID.eq(instanceId))
            .and(DUNGEON_SPIRIT_STATE.USER_ID.eq(userId));
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }
}
