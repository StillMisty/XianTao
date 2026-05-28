package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.fudi.entity.table.SpiritTableDef.SPIRIT;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritMapper;

@Repository
@RequiredArgsConstructor
public class SpiritRepository {

  private final SpiritMapper spiritMapper;

  public Optional<Spirit> findById(Long id) {
    Spirit spirit = spiritMapper.selectOneById(id);
    return Optional.ofNullable(spirit);
  }

  public Optional<Spirit> findByFudiId(Long fudiId) {
    QueryWrapper queryWrapper = QueryWrapper.create().where(SPIRIT.FUDI_ID.eq(fudiId));
    Spirit spirit = spiritMapper.selectOneByQuery(queryWrapper);
    return Optional.ofNullable(spirit);
  }

  public Spirit save(Spirit spirit) {
    spiritMapper.insertOrUpdateSelective(spirit);
    return spirit;
  }

  public void deleteById(Long id) {
    spiritMapper.deleteById(id);
  }

  public int tryClaimDailyGift(Long spiritId) {
    return spiritMapper.tryClaimDailyGift(spiritId);
  }
}
