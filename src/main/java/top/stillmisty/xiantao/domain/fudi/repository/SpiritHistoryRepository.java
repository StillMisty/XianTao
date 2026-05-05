package top.stillmisty.xiantao.domain.fudi.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritHistory;

public interface SpiritHistoryRepository {

  SpiritHistory save(SpiritHistory history);

  List<SpiritHistory> findByFudiIdOrderByCreateTimeDesc(Long fudiId, int limit);
}
