package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.user.entity.table.DaoProtectionTableDef.DAO_PROTECTION;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.infrastructure.mapper.DaoProtectionMapper;

/** 护道关系仓储实现 */
@Repository
@RequiredArgsConstructor
public class DaoProtectionRepository {

  private final DaoProtectionMapper daoProtectionMapper;

  public DaoProtection save(DaoProtection protection) {
    daoProtectionMapper.insertOrUpdateSelective(protection);
    return protection;
  }

  public Optional<DaoProtection> findById(Long id) {
    return Optional.ofNullable(daoProtectionMapper.selectOneById(id));
  }

  public List<DaoProtection> findByProtectorId(Long protectorId) {
    QueryWrapper query = QueryWrapper.create().where(DAO_PROTECTION.PROTECTOR_ID.eq(protectorId));
    return daoProtectionMapper.selectListByQuery(query);
  }

  public List<DaoProtection> findByProtegeId(Long protegeId) {
    QueryWrapper query = QueryWrapper.create().where(DAO_PROTECTION.PROTEGE_ID.eq(protegeId));
    return daoProtectionMapper.selectListByQuery(query);
  }

  public Optional<DaoProtection> findByProtectorAndProtege(Long protectorId, Long protegeId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(DAO_PROTECTION.PROTECTOR_ID.eq(protectorId))
            .and(DAO_PROTECTION.PROTEGE_ID.eq(protegeId));
    return Optional.ofNullable(daoProtectionMapper.selectOneByQuery(query));
  }

  public void deleteById(Long id) {
    daoProtectionMapper.deleteById(id);
  }

  public void deleteByProtegeId(Long protegeId) {
    QueryWrapper query = QueryWrapper.create().where(DAO_PROTECTION.PROTEGE_ID.eq(protegeId));
    daoProtectionMapper.deleteByQuery(query);
  }

  public long countByProtectorId(Long protectorId) {
    QueryWrapper query = QueryWrapper.create().where(DAO_PROTECTION.PROTECTOR_ID.eq(protectorId));
    return daoProtectionMapper.selectCountByQuery(query);
  }
}
