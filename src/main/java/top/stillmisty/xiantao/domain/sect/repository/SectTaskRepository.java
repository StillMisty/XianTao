package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectTask;

public interface SectTaskRepository {
  SectTask save(SectTask task);

  Optional<SectTask> findById(Long id);

  List<SectTask> findBySectId(Long sectId);

  void deleteById(Long id);

  void deleteBySectId(Long sectId);
}
