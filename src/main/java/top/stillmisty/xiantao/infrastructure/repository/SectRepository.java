package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectTableDef.SECT;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.infrastructure.mapper.SectMapper;

@Repository
@RequiredArgsConstructor
public class SectRepository {

  private final SectMapper sectMapper;

  public Sect save(Sect sect) {
    sectMapper.insertOrUpdateSelective(sect);
    return sect;
  }

  public Optional<Sect> findById(Long id) {
    return Optional.ofNullable(sectMapper.selectOneById(id));
  }

  public Optional<Sect> findByName(String name) {
    QueryWrapper query = QueryWrapper.create().where(SECT.NAME.eq(name));
    return Optional.ofNullable(sectMapper.selectOneByQuery(query));
  }

  public Optional<Sect> findByLeaderId(Long leaderId) {
    QueryWrapper query = QueryWrapper.create().where(SECT.LEADER_ID.eq(leaderId));
    return Optional.ofNullable(sectMapper.selectOneByQuery(query));
  }

  public List<Sect> findAll() {
    return sectMapper.selectAll();
  }

  public void deleteById(Long id) {
    sectMapper.deleteById(id);
  }
}
