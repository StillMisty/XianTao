package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.infrastructure.mapper.DaoProtectionMapper;

/** 护道关系仓储实现 */
@Repository
@RequiredArgsConstructor
public class DaoProtectionRepositoryImpl implements DaoProtectionRepository {

  private final DaoProtectionMapper daoProtectionMapper;

  @Override
  public DaoProtection save(DaoProtection protection) {
    daoProtectionMapper.insertOrUpdateSelective(protection);
    return protection;
  }

  @Override
  public Optional<DaoProtection> findById(Long id) {
    return Optional.ofNullable(daoProtectionMapper.selectOneById(id));
  }

  @Override
  public List<DaoProtection> findByProtectorId(Long protectorId) {
    QueryWrapper query = new QueryWrapper().eq(DaoProtection::getProtectorId, protectorId);
    return daoProtectionMapper.selectListByQuery(query);
  }

  @Override
  public List<DaoProtection> findByProtegeId(Long protegeId) {
    QueryWrapper query = new QueryWrapper().eq(DaoProtection::getProtegeId, protegeId);
    return daoProtectionMapper.selectListByQuery(query);
  }

  @Override
  public Optional<DaoProtection> findByProtectorAndProtege(Long protectorId, Long protegeId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(DaoProtection::getProtectorId, protectorId)
            .eq(DaoProtection::getProtegeId, protegeId);
    return Optional.ofNullable(daoProtectionMapper.selectOneByQuery(query));
  }

  @Override
  public void deleteById(Long id) {
    daoProtectionMapper.deleteById(id);
  }

  @Override
  public void deleteByProtegeId(Long protegeId) {
    QueryWrapper query = new QueryWrapper().eq(DaoProtection::getProtegeId, protegeId);
    daoProtectionMapper.deleteByQuery(query);
  }

  @Override
  public long countByProtectorId(Long protectorId) {
    QueryWrapper query = new QueryWrapper().eq(DaoProtection::getProtectorId, protectorId);
    return daoProtectionMapper.selectCountByQuery(query);
  }
}
