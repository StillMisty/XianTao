package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.bounty.entity.table.BountyTableDef.BOUNTY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;
import top.stillmisty.xiantao.domain.bounty.repository.BountyRepository;
import top.stillmisty.xiantao.infrastructure.mapper.BountyMapper;

@Repository
@RequiredArgsConstructor
public class BountyRepositoryImpl implements BountyRepository {

  private final BountyMapper mapper;

  @Override
  public Optional<Bounty> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<Bounty> findByMapId(Long mapId) {
    return mapper.selectListByQuery(QueryWrapper.create().where(BOUNTY.MAP_ID.eq(mapId)));
  }
}
