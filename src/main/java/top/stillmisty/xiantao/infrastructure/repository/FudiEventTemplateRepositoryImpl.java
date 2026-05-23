package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.domain.fudi.repository.FudiEventTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.FudiEventTemplateMapper;

@Repository
@RequiredArgsConstructor
public class FudiEventTemplateRepositoryImpl implements FudiEventTemplateRepository {

  private final FudiEventTemplateMapper fudiEventTemplateMapper;

  @Override
  public FudiEventTemplate save(FudiEventTemplate template) {
    fudiEventTemplateMapper.insertOrUpdateSelective(template);
    return template;
  }

  @Override
  public List<FudiEventTemplate> findAll() {
    return fudiEventTemplateMapper.selectAll();
  }
}
