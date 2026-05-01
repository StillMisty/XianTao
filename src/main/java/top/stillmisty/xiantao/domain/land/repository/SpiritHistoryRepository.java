package top.stillmisty.xiantao.domain.land.repository;

import top.stillmisty.xiantao.domain.land.entity.SpiritHistory;

import java.util.List;

public interface SpiritHistoryRepository {

    SpiritHistory save(SpiritHistory history);

    List<SpiritHistory> findByFudiIdOrderByCreateTimeDesc(Long fudiId, int limit);

    void deleteByFudiId(Long fudiId);

    int countByFudiId(Long fudiId);
}
