package top.stillmisty.xiantao.domain.bounty.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;

/** 悬赏任务仓储接口 */
public interface BountyRepository {

  Optional<Bounty> findById(Long id);

  List<Bounty> findByMapId(Long mapId);

  List<Bounty> findByMapIdExcluding(Long mapId, Set<Long> excludeIds);
}
