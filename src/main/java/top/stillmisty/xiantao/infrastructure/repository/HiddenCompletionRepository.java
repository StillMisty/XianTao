package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.event.entity.table.HiddenCompletionTableDef.HIDDEN_COMPLETION;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;
import top.stillmisty.xiantao.infrastructure.mapper.HiddenCompletionMapper;

@Repository
@RequiredArgsConstructor
public class HiddenCompletionRepository {

  private final HiddenCompletionMapper hiddenCompletionMapper;

  public Optional<HiddenCompletion> findByUserAndEvent(
      Long userId, String activityType, Long ownerId, String code) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(HIDDEN_COMPLETION.USER_ID.eq(userId))
            .and(HIDDEN_COMPLETION.ACTIVITY_TYPE.eq(activityType))
            .and(HIDDEN_COMPLETION.OWNER_ID.eq(ownerId))
            .and(HIDDEN_COMPLETION.CODE.eq(code));
    return Optional.ofNullable(hiddenCompletionMapper.selectOneByQuery(query));
  }

  public HiddenCompletion save(HiddenCompletion completion) {
    hiddenCompletionMapper.insertOrUpdateSelective(completion);
    return completion;
  }

  public boolean exists(Long userId, String activityType, Long ownerId, String code) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(HIDDEN_COMPLETION.USER_ID.eq(userId))
            .and(HIDDEN_COMPLETION.ACTIVITY_TYPE.eq(activityType))
            .and(HIDDEN_COMPLETION.OWNER_ID.eq(ownerId))
            .and(HIDDEN_COMPLETION.CODE.eq(code));
    return hiddenCompletionMapper.selectCountByQuery(query) > 0;
  }

  public boolean existsByCode(Long userId, String code) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(HIDDEN_COMPLETION.USER_ID.eq(userId))
            .and(HIDDEN_COMPLETION.CODE.eq(code));
    return hiddenCompletionMapper.selectCountByQuery(query) > 0;
  }
}
