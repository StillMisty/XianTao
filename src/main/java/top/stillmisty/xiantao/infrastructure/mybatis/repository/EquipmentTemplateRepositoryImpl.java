package top.stillmisty.xiantao.infrastructure.mybatis.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mybatis.mapper.EquipmentTemplateMapper;

@Repository
@RequiredArgsConstructor
public class EquipmentTemplateRepositoryImpl implements EquipmentTemplateRepository {

  private final EquipmentTemplateMapper mapper;

  @Override
  public Optional<EquipmentTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<EquipmentTemplate> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return mapper.selectListByQuery(QueryWrapper.create().in("id", ids));
  }

  @Override
  public List<EquipmentTemplate> findAll() {
    return mapper.selectAll();
  }
}
