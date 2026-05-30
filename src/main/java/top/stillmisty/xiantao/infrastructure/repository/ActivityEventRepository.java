package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.event.entity.table.ActivityEventTableDef.ACTIVITY_EVENT;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.EventTypeEnum;
import top.stillmisty.xiantao.infrastructure.mapper.ActivityEventMapper;

@Repository
@RequiredArgsConstructor
public class ActivityEventRepository {

  private final ActivityEventMapper activityEventMapper;

  public List<ActivityEvent> findSubEvents(String activityType, Long ownerId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(ACTIVITY_EVENT.ACTIVITY_TYPE.eq(activityType))
            .and(ACTIVITY_EVENT.OWNER_ID.eq(ownerId))
            .and(ACTIVITY_EVENT.IS_HIDDEN.eq(false));
    return activityEventMapper.selectListByQuery(query);
  }

  public List<ActivityEvent> findHiddenEvents(String activityType, Long ownerId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(ACTIVITY_EVENT.ACTIVITY_TYPE.eq(activityType))
            .and(ACTIVITY_EVENT.OWNER_ID.eq(ownerId))
            .and(ACTIVITY_EVENT.IS_HIDDEN.eq(true));
    return activityEventMapper.selectListByQuery(query);
  }

  public void save(ActivityEvent event) {
    activityEventMapper.insertOrUpdateSelective(event);
  }

  public List<ActivityEvent> findByType(
      String activityType, Long ownerId, EventTypeEnum eventType) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(ACTIVITY_EVENT.ACTIVITY_TYPE.eq(activityType))
            .and(ACTIVITY_EVENT.OWNER_ID.eq(ownerId))
            .and(ACTIVITY_EVENT.EVENT_TYPE.eq(eventType))
            .and(ACTIVITY_EVENT.IS_HIDDEN.eq(false));
    return activityEventMapper.selectListByQuery(query);
  }

  public Map<Long, List<ActivityEvent>> findByOwnerIdsAndType(
      String activityType, List<Long> ownerIds, EventTypeEnum eventType) {
    if (ownerIds == null || ownerIds.isEmpty()) return Map.of();
    QueryWrapper query =
        QueryWrapper.create()
            .where(ACTIVITY_EVENT.ACTIVITY_TYPE.eq(activityType))
            .and(ACTIVITY_EVENT.OWNER_ID.in(ownerIds))
            .and(ACTIVITY_EVENT.EVENT_TYPE.eq(eventType))
            .and(ACTIVITY_EVENT.IS_HIDDEN.eq(false));
    List<ActivityEvent> events = activityEventMapper.selectListByQuery(query);
    return events.stream()
        .collect(
            Collectors.groupingBy(
                ActivityEvent::getOwnerId, LinkedHashMap::new, Collectors.toList()));
  }
}
