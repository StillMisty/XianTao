package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.masterapprentice.entity.MasterApprentice;
import top.stillmisty.xiantao.domain.masterapprentice.repository.MasterApprenticeRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MasterApprenticeMapper;

@Repository
@RequiredArgsConstructor
public class MasterApprenticeRepositoryImpl implements MasterApprenticeRepository {

  private final MasterApprenticeMapper masterApprenticeMapper;

  @Override
  public MasterApprentice save(MasterApprentice relation) {
    masterApprenticeMapper.insertOrUpdateSelective(relation);
    return relation;
  }

  @Override
  public Optional<MasterApprentice> findById(Long id) {
    return Optional.ofNullable(masterApprenticeMapper.selectOneById(id));
  }

  @Override
  public Optional<MasterApprentice> findByApprenticeId(Long apprenticeId) {
    QueryWrapper query = new QueryWrapper().eq(MasterApprentice::getApprenticeId, apprenticeId);
    return Optional.ofNullable(masterApprenticeMapper.selectOneByQuery(query));
  }

  @Override
  public List<MasterApprentice> findByMasterId(Long masterId) {
    QueryWrapper query = new QueryWrapper().eq(MasterApprentice::getMasterId, masterId);
    return masterApprenticeMapper.selectListByQuery(query);
  }

  @Override
  public long countActiveByMasterId(Long masterId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(MasterApprentice::getMasterId, masterId)
            .eq(MasterApprentice::getStatus, "ACTIVE");
    return masterApprenticeMapper.selectCountByQuery(query);
  }

  @Override
  public void deleteById(Long id) {
    masterApprenticeMapper.deleteById(id);
  }
}
