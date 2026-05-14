package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;
import top.stillmisty.xiantao.domain.shop.repository.WorldEventRepository;
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
  public void deleteById(Long id) {
    worldEventMapper.deleteById(id);
  }
}
