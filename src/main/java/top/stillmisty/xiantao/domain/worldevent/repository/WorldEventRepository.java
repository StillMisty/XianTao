package top.stillmisty.xiantao.domain.worldevent.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;

public interface WorldEventRepository {

  WorldEvent save(WorldEvent event);

  Optional<WorldEvent> findById(Long id);

  List<WorldEvent> findActiveEvents();

  List<WorldEvent> findActiveByScope(WorldEventScope scope);

  List<WorldEvent> findActiveByRegion(Long mapNodeId);

  List<WorldEvent> findUpcoming();

  int markStatus(Long id, String status);

  void deleteById(Long id);

  int incrementParticipationCount(Long id);
}
