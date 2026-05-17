package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectTask;
import top.stillmisty.xiantao.domain.sect.repository.SectTaskRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SectTaskMapper;

@Repository
@RequiredArgsConstructor
public class SectTaskRepositoryImpl implements SectTaskRepository {

  private final SectTaskMapper mapper;

  @Override
  public SectTask save(SectTask task) {
    mapper.insertOrUpdateSelective(task);
    return task;
  }

  @Override
  public Optional<SectTask> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<SectTask> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectTask::getSectId, sectId);
    return mapper.selectListByQuery(query);
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  @Override
  public void deleteBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectTask::getSectId, sectId);
    mapper.deleteByQuery(query);
  }
}
