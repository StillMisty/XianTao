package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectTaskProgress;

public interface SectTaskProgressRepository {
  SectTaskProgress save(SectTaskProgress progress);

  Optional<SectTaskProgress> findById(Long id);

  List<SectTaskProgress> findByTaskId(Long taskId);

  Optional<SectTaskProgress> findByTaskIdAndUserId(Long taskId, Long userId);

  void deleteByTaskId(Long taskId);

  void deleteByUserId(Long userId);
}
