package top.stillmisty.xiantao.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;

public interface WorldEventRepository {

  WorldEvent save(WorldEvent event);

  Optional<WorldEvent> findById(Long id);

  List<WorldEvent> findActiveEvents();

  void deleteById(Long id);
}
