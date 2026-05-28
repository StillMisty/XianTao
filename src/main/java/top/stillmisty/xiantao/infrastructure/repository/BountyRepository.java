package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.bounty.entity.table.BountyTableDef.BOUNTY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;
import top.stillmisty.xiantao.infrastructure.mapper.BountyMapper;

@Repository
@RequiredArgsConstructor
public class BountyRepository {

  private final BountyMapper mapper;

  public Optional<Bounty> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<Bounty> findByMapId(Long mapId) {
    return mapper.selectListByQuery(QueryWrapper.create().where(BOUNTY.MAP_ID.eq(mapId)));
  }

  public List<Bounty> findByMapIdExcluding(Long mapId, Set<Long> excludeIds) {
    QueryWrapper qw = QueryWrapper.create().where(BOUNTY.MAP_ID.eq(mapId));
    if (excludeIds != null && !excludeIds.isEmpty()) {
      qw.and(BOUNTY.ID.notIn(excludeIds));
    }
    return mapper.selectListByQuery(qw);
  }

  public List<Bounty> findByMapIdWithLevelFilter(Long mapId, int userLevel) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .where(BOUNTY.MAP_ID.eq(mapId))
            .and(BOUNTY.REQUIRE_LEVEL.le(userLevel)));
  }

  public List<Bounty> findByMapIdExcludingWithLevelFilter(
      Long mapId, Set<Long> excludeIds, int userLevel) {
    QueryWrapper qw =
        QueryWrapper.create()
            .where(BOUNTY.MAP_ID.eq(mapId))
            .and(BOUNTY.REQUIRE_LEVEL.le(userLevel));
    if (excludeIds != null && !excludeIds.isEmpty()) {
      qw.and(BOUNTY.ID.notIn(excludeIds));
    }
    return mapper.selectListByQuery(qw);
  }
}
