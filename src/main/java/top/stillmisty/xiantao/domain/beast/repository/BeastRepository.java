package top.stillmisty.xiantao.domain.beast.repository;

import top.stillmisty.xiantao.domain.beast.entity.Beast;

import java.util.List;
import java.util.Optional;

public interface BeastRepository {

    Optional<Beast> findById(Long id);

    List<Beast> findByUserId(Long userId);

    List<Beast> findByFudiId(Long fudiId);

    List<Beast> findDeployedByUserId(Long userId);

    List<Beast> findByUserIdAndIsDeployed(Long userId, boolean isDeployed);

    Beast save(Beast beast);

    void deleteById(Long id);

    long countByUserId(Long userId);
}
