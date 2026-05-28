package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.beast.entity.BeastTemplate;
import top.stillmisty.xiantao.infrastructure.mapper.BeastTemplateMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BeastTemplateRepository {

  private final BeastTemplateMapper mapper;

  public Optional<BeastTemplate> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<BeastTemplate> findByName(String name) {
    return mapper.selectListByQuery(QueryWrapper.create()).stream()
        .filter(t -> t.getName().equals(name))
        .findFirst();
  }

  public List<BeastTemplate> findAll() {
    return mapper.selectListByQuery(QueryWrapper.create());
  }

  public BeastTemplate save(BeastTemplate template) {
    mapper.insertOrUpdateSelective(template);
    return template;
  }
}
