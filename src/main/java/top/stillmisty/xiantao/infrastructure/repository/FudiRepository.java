package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.fudi.entity.table.FudiTableDef.FUDI;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.infrastructure.mapper.FudiMapper;

/** 福地 Repository 实现 */
@Repository
@RequiredArgsConstructor
public class FudiRepository {

  private final FudiMapper fudiMapper;

  public Optional<Fudi> findById(Long id) {
    Fudi fudi = fudiMapper.selectOneById(id);
    return Optional.ofNullable(fudi);
  }

  public Optional<Fudi> findByUserId(Long userId) {
    QueryWrapper queryWrapper = QueryWrapper.create().where(FUDI.USER_ID.eq(userId));
    Fudi fudi = fudiMapper.selectOneByQuery(queryWrapper);
    return Optional.ofNullable(fudi);
  }

  public Optional<Fudi> findByUserIdForUpdate(Long userId) {
    QueryWrapper queryWrapper = QueryWrapper.create().where(FUDI.USER_ID.eq(userId)).forUpdate();
    Fudi fudi = fudiMapper.selectOneByQuery(queryWrapper);
    return Optional.ofNullable(fudi);
  }

  public Fudi save(Fudi fudi) {
    fudiMapper.insertOrUpdateSelective(fudi);
    return fudi;
  }

  public boolean existsByUserId(Long userId) {
    QueryWrapper queryWrapper = QueryWrapper.create().where(FUDI.USER_ID.eq(userId));
    return fudiMapper.selectCountByQuery(queryWrapper) > 0;
  }

  public void deleteById(Long id) {
    fudiMapper.deleteById(id);
  }
}
