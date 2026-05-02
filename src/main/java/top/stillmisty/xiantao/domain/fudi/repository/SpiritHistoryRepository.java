package top.stillmisty.xiantao.domain.fudi.repository;

import top.stillmisty.xiantao.domain.fudi.entity.SpiritHistory;

import java.util.List;

public interface SpiritHistoryRepository {

    SpiritHistory save(SpiritHistory history);

    List<SpiritHistory> findByFudiIdOrderByCreateTimeDesc(Long fudiId, int limit);
}
