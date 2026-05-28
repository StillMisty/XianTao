package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectTask;
import top.stillmisty.xiantao.infrastructure.mapper.SectTaskMapper;

@Repository
@RequiredArgsConstructor
public class SectTaskRepository {

  private final SectTaskMapper mapper;

  public SectTask save(SectTask task) {
    mapper.insertOrUpdateSelective(task);
    return task;
  }

  public Optional<SectTask> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<SectTask> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectTask::getSectId, sectId);
    return mapper.selectListByQuery(query);
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public void deleteBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectTask::getSectId, sectId);
    mapper.deleteByQuery(query);
  }
}
