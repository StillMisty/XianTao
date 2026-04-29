package top.stillmisty.xiantao.domain.bounty.repository;

import top.stillmisty.xiantao.domain.bounty.entity.Bounty;

import java.util.List;
import java.util.Optional;

/**
 * 悬赏任务仓储接口
 */
public interface BountyRepository {

    Optional<Bounty> findById(Long id);

    List<Bounty> findByMapId(Long mapId);
}