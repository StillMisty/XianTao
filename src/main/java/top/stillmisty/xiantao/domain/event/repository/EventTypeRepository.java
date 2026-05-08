package top.stillmisty.xiantao.domain.event.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.event.entity.EventType;

public interface EventTypeRepository {

  Optional<EventType> findByCode(String code);

  List<EventType> findByActivityType(String activityType);

  List<EventType> findAll();
}
