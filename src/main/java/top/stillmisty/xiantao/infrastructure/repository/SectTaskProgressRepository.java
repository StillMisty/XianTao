package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectTaskProgressTableDef.SECT_TASK_PROGRESS;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectTaskProgress;
import top.stillmisty.xiantao.infrastructure.mapper.SectTaskProgressMapper;

@Repository
@RequiredArgsConstructor
public class SectTaskProgressRepository {

  private final SectTaskProgressMapper mapper;

  public SectTaskProgress save(SectTaskProgress progress) {
    mapper.insertOrUpdateSelective(progress);
    return progress;
  }

  public Optional<SectTaskProgress> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<SectTaskProgress> findByTaskId(Long taskId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_TASK_PROGRESS.TASK_ID.eq(taskId));
    return mapper.selectListByQuery(query);
  }

  public Optional<SectTaskProgress> findByTaskIdAndUserId(Long taskId, Long userId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(SECT_TASK_PROGRESS.TASK_ID.eq(taskId))
            .and(SECT_TASK_PROGRESS.USER_ID.eq(userId));
    return Optional.ofNullable(mapper.selectOneByQuery(query));
  }

  public void deleteByTaskId(Long taskId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_TASK_PROGRESS.TASK_ID.eq(taskId));
    mapper.deleteByQuery(query);
  }

  public void deleteByUserId(Long userId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_TASK_PROGRESS.USER_ID.eq(userId));
    mapper.deleteByQuery(query);
  }
}
