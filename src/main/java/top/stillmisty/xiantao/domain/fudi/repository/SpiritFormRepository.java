package top.stillmisty.xiantao.domain.fudi.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;

public interface SpiritFormRepository {

  Optional<SpiritForm> findById(Long id);

  List<SpiritForm> findAll();
}
