package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritFormMapper;

@Repository
@RequiredArgsConstructor
public class SpiritFormRepository {

  private final SpiritFormMapper spiritFormMapper;

  public Optional<SpiritForm> findById(Long id) {
    SpiritForm form = spiritFormMapper.selectOneById(id);
    return Optional.ofNullable(form);
  }

  public List<SpiritForm> findAll() {
    return spiritFormMapper.selectAll();
  }
}
