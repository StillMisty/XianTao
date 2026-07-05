package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritFormMapper;

@Repository
public class SpiritFormRepository extends BaseRepository<SpiritForm> {

  public SpiritFormRepository(SpiritFormMapper mapper) {
    super(mapper);
  }

  @Override
  public List<SpiritForm> findAll() {
    return mapper.selectAll();
  }
}
