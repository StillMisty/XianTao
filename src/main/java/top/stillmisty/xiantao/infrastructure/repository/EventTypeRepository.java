package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.event.entity.table.EventTypeTableDef.EVENT_TYPE;

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
    QueryWrapper query = QueryWrapper.create().where(EVENT_TYPE.CODE.eq(code));
    return Optional.ofNullable(eventTypeMapper.selectOneByQuery(query));
  }

  public List<EventType> findByActivityType(String activityType) {
    QueryWrapper query = QueryWrapper.create().where(EVENT_TYPE.ACTIVITY_TYPE.eq(activityType));
    return eventTypeMapper.selectListByQuery(query);
  }

  public List<EventType> findAll() {
    return eventTypeMapper.selectAll();
  }
}
