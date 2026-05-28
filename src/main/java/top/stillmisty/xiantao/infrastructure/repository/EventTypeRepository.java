package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.EventType;
import top.stillmisty.xiantao.infrastructure.mapper.EventTypeMapper;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

  private final EventTypeMapper eventTypeMapper;

  public Optional<EventType> findByCode(String code) {
    QueryWrapper query = new QueryWrapper().eq(EventType::getCode, code);
    return Optional.ofNullable(eventTypeMapper.selectOneByQuery(query));
  }

  public List<EventType> findByActivityType(String activityType) {
    QueryWrapper query = new QueryWrapper().eq(EventType::getActivityType, activityType);
    return eventTypeMapper.selectListByQuery(query);
  }

  public List<EventType> findAll() {
    return eventTypeMapper.selectAll();
  }
}
