package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.repository.ActivityEventRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ActivityEventMapper;

@Repository
@RequiredArgsConstructor
public class ActivityEventRepositoryImpl implements ActivityEventRepository {

  private final ActivityEventMapper activityEventMapper;

  @Override
  public List<ActivityEvent> findSubEvents(String activityType, Long ownerId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(ActivityEvent::getActivityType, activityType)
            .eq(ActivityEvent::getOwnerId, ownerId)
            .eq(ActivityEvent::getIsHidden, false);
    return activityEventMapper.selectListByQuery(query);
  }

  @Override
  public List<ActivityEvent> findHiddenEvents(String activityType, Long ownerId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(ActivityEvent::getActivityType, activityType)
            .eq(ActivityEvent::getOwnerId, ownerId)
            .eq(ActivityEvent::getIsHidden, true);
    return activityEventMapper.selectListByQuery(query);
  }

  @Override
  public void save(ActivityEvent event) {
    activityEventMapper.insertOrUpdateSelective(event);
  }
}
