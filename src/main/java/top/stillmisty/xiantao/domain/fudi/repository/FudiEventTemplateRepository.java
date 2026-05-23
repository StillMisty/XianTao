package top.stillmisty.xiantao.domain.fudi.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;

public interface FudiEventTemplateRepository {

  FudiEventTemplate save(FudiEventTemplate template);

  List<FudiEventTemplate> findAll();
}
