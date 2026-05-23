package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.domain.worldevent.repository.WorldEventRepository;
import top.stillmisty.xiantao.infrastructure.mapper.WorldEventMapper;

@Repository
@RequiredArgsConstructor
public class WorldEventRepositoryImpl implements WorldEventRepository {

  private final WorldEventMapper worldEventMapper;

  @Override
  public WorldEvent save(WorldEvent event) {
    worldEventMapper.insertOrUpdateSelective(event);
    return event;
  }

  @Override
  public Optional<WorldEvent> findById(Long id) {
    return Optional.ofNullable(worldEventMapper.selectOneById(id));
  }

  @Override
  public List<WorldEvent> findActiveEvents() {
    return worldEventMapper.selectActiveEvents();
  }

  @Override
  public List<WorldEvent> findActiveByScope(WorldEventScope scope) {
    return worldEventMapper.selectActiveByScope(scope.getCode());
  }

  @Override
  public List<WorldEvent> findActiveByRegion(Long mapNodeId) {
    return worldEventMapper.selectActiveByRegion(mapNodeId);
  }

  @Override
  public List<WorldEvent> findUpcoming() {
    return worldEventMapper.selectUpcoming();
  }

  @Override
  public int markStatus(Long id, String status) {
    return worldEventMapper.updateStatus(id, status);
  }

  @Override
  public void deleteById(Long id) {
    worldEventMapper.deleteById(id);
  }

  @Override
  public int incrementParticipationCount(Long id) {
    return worldEventMapper.incrementParticipationCount(id);
  }
}
