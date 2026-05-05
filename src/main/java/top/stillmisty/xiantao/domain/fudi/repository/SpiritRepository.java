package top.stillmisty.xiantao.domain.fudi.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;

public interface SpiritRepository {

  Optional<Spirit> findById(Long id);

  Optional<Spirit> findByFudiId(Long fudiId);

  Spirit save(Spirit spirit);

  void deleteById(Long id);
}
