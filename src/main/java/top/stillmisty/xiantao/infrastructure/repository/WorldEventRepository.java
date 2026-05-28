package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.infrastructure.mapper.WorldEventMapper;

@Repository
@RequiredArgsConstructor
public class WorldEventRepository {

  private final WorldEventMapper worldEventMapper;

  public WorldEvent save(WorldEvent event) {
    worldEventMapper.insertOrUpdateSelective(event);
    return event;
  }

  public Optional<WorldEvent> findById(Long id) {
    return Optional.ofNullable(worldEventMapper.selectOneById(id));
  }

  public List<WorldEvent> findActiveEvents() {
    return worldEventMapper.selectActiveEvents();
  }

  public List<WorldEvent> findActiveByScope(WorldEventScope scope) {
    return worldEventMapper.selectActiveByScope(scope.getCode());
  }

  public List<WorldEvent> findActiveByRegion(Long mapNodeId) {
    return worldEventMapper.selectActiveByRegion(mapNodeId);
  }

  public List<WorldEvent> findUpcoming() {
    return worldEventMapper.selectUpcoming();
  }

  public int markStatus(Long id, String status) {
    return worldEventMapper.updateStatus(id, status);
  }

  public void deleteById(Long id) {
    worldEventMapper.deleteById(id);
  }

  public int incrementParticipationCount(Long id) {
    return worldEventMapper.incrementParticipationCount(id);
  }
}
