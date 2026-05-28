package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.infrastructure.mapper.FudiEventTemplateMapper;

@Repository
@RequiredArgsConstructor
public class FudiEventTemplateRepository {

  private final FudiEventTemplateMapper fudiEventTemplateMapper;

  public FudiEventTemplate save(FudiEventTemplate template) {
    fudiEventTemplateMapper.insertOrUpdateSelective(template);
    return template;
  }

  public List<FudiEventTemplate> findAll() {
    return fudiEventTemplateMapper.selectAll();
  }
}
