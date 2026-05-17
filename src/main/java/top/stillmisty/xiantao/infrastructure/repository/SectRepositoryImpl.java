package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SectMapper;

@Repository
@RequiredArgsConstructor
public class SectRepositoryImpl implements SectRepository {

  private final SectMapper sectMapper;

  @Override
  public Sect save(Sect sect) {
    sectMapper.insertOrUpdateSelective(sect);
    return sect;
  }

  @Override
  public Optional<Sect> findById(Long id) {
    return Optional.ofNullable(sectMapper.selectOneById(id));
  }

  @Override
  public Optional<Sect> findByName(String name) {
    QueryWrapper query = new QueryWrapper().eq(Sect::getName, name);
    return Optional.ofNullable(sectMapper.selectOneByQuery(query));
  }

  @Override
  public Optional<Sect> findByLeaderId(Long leaderId) {
    QueryWrapper query = new QueryWrapper().eq(Sect::getLeaderId, leaderId);
    return Optional.ofNullable(sectMapper.selectOneByQuery(query));
  }

  @Override
  public List<Sect> findAll() {
    return sectMapper.selectAll();
  }

  @Override
  public void deleteById(Long id) {
    sectMapper.deleteById(id);
  }
}
