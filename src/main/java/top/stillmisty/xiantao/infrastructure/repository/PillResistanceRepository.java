package top.stillmisty.xiantao.infrastructure.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;
import top.stillmisty.xiantao.infrastructure.mapper.PillResistanceMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PillResistanceRepository {

  private final PillResistanceMapper mapper;

  public Optional<PillResistance> findByUserIdAndTemplateIdAndQuality(
      Long userId, Long templateId, String quality) {
    return mapper.selectByUserIdAndTemplateIdAndQuality(userId, templateId, quality);
  }

  /** upsert 并经 RETURNING 直接返回最新次数，省一次回查 */
  public int incrementCount(Long userId, Long templateId, String quality) {
    Integer count = mapper.upsertIncrementCount(userId, templateId, quality);
    return count != null ? count : 1;
  }
}
