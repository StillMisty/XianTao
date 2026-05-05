package top.stillmisty.xiantao.infrastructure.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;
import top.stillmisty.xiantao.domain.pill.repository.PillResistanceRepository;
import top.stillmisty.xiantao.infrastructure.mapper.PillResistanceMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PillResistanceRepositoryImpl implements PillResistanceRepository {

  private final PillResistanceMapper mapper;

  @Override
  public Optional<PillResistance> findByUserIdAndTemplateId(Long userId, Long templateId) {
    return mapper.selectByUserIdAndTemplateId(userId, templateId);
  }

  @Override
  public int incrementCount(Long userId, Long templateId) {
    mapper.upsertIncrementCount(userId, templateId);
    return findByUserIdAndTemplateId(userId, templateId).map(PillResistance::getCount).orElse(1);
  }
}
