package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectTableDef.SECT;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDateTime;
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

  /** 原子增加宗门资金 */
  public int addFunds(Long id, long amount) {
    return sectMapper.addFunds(id, amount);
  }

  /** 原子扣减宗门资金，资金不足返回 0 */
  public int deductFundsIfEnough(Long id, long amount) {
    return sectMapper.deductFundsIfEnough(id, amount);
  }

  /** 仅当距上次事件时间超过 staleBefore 时写入新事件；并发触发下只有一个请求能成功 */
  public int updateEventIfStale(
      Long id,
      String eventType,
      String eventText,
      LocalDateTime eventTime,
      LocalDateTime expiresAt,
      LocalDateTime staleBefore) {
    return sectMapper.updateEventIfStale(
        id, eventType, eventText, eventTime, expiresAt, staleBefore);
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
