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

  public List<FudiEventTemplate> findEnabled() {
    return fudiEventTemplateMapper.selectEnabled();
  }
}
