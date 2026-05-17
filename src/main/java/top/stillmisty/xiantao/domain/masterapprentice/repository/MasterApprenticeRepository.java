package top.stillmisty.xiantao.domain.masterapprentice.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.masterapprentice.entity.MasterApprentice;

/** 师徒关系仓储接口 */
public interface MasterApprenticeRepository {

  MasterApprentice save(MasterApprentice relation);

  Optional<MasterApprentice> findById(Long id);

  Optional<MasterApprentice> findByApprenticeId(Long apprenticeId);

  List<MasterApprentice> findByMasterId(Long masterId);

  long countActiveByMasterId(Long masterId);

  void deleteById(Long id);
}
