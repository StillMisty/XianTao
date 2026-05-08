package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.infrastructure.mapper.HiddenCompletionMapper;

@Repository
@RequiredArgsConstructor
public class HiddenCompletionRepositoryImpl implements HiddenCompletionRepository {

  private final HiddenCompletionMapper hiddenCompletionMapper;

  @Override
  public Optional<HiddenCompletion> findByUserAndEvent(
      Long userId, String activityType, Long ownerId, String code) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(HiddenCompletion::getUserId, userId)
            .eq(HiddenCompletion::getActivityType, activityType)
            .eq(HiddenCompletion::getOwnerId, ownerId)
            .eq(HiddenCompletion::getCode, code);
    return Optional.ofNullable(hiddenCompletionMapper.selectOneByQuery(query));
  }

  @Override
  public HiddenCompletion save(HiddenCompletion completion) {
    hiddenCompletionMapper.insertOrUpdateSelective(completion);
    return completion;
  }

  @Override
  public boolean exists(Long userId, String activityType, Long ownerId, String code) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(HiddenCompletion::getUserId, userId)
            .eq(HiddenCompletion::getActivityType, activityType)
            .eq(HiddenCompletion::getOwnerId, ownerId)
            .eq(HiddenCompletion::getCode, code);
    return hiddenCompletionMapper.selectCountByQuery(query) > 0;
  }
}
