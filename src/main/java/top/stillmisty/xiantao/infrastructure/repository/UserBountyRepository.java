package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.bounty.entity.table.UserBountyTableDef.USER_BOUNTY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.infrastructure.mapper.UserBountyMapper;

@Repository
@RequiredArgsConstructor
public class UserBountyRepository {

  private final UserBountyMapper mapper;

  public Optional<UserBounty> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<UserBounty> findByIdForUpdate(Long id) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(QueryWrapper.create().where(USER_BOUNTY.ID.eq(id)).forUpdate()));
  }

  /** 定位玩家当前的悬赏记录：优先按活动目标取（兼容自动完成后 COMPLETED 的待领奖记录）， 否则回退到进行中的记录。 */
  public Optional<UserBounty> findCurrentForUser(Long userId, @Nullable Long activityTargetId) {
    if (activityTargetId != null) {
      Optional<UserBounty> byId = findByIdForUpdate(activityTargetId);
      if (byId.isPresent()
          && userId.equals(byId.get().getUserId())
          && byId.get().getStatus() != BountyStatus.ABANDONED) {
        return byId;
      }
    }
    return findActiveByUserIdForUpdate(userId);
  }

  public Optional<UserBounty> findActiveByUserId(Long userId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .where(USER_BOUNTY.USER_ID.eq(userId))
                .and(USER_BOUNTY.STATUS.eq(BountyStatus.ACTIVE))));
  }

  public Optional<UserBounty> findActiveByUserIdForUpdate(Long userId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .where(USER_BOUNTY.USER_ID.eq(userId))
                .and(USER_BOUNTY.STATUS.eq(BountyStatus.ACTIVE))
                .forUpdate()));
  }

  public void save(UserBounty userBounty) {
    mapper.insertOrUpdateSelective(userBounty);
  }

  public Optional<UserBounty> findCompletedByUserIdAndBountyId(Long userId, Long bountyId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .where(USER_BOUNTY.USER_ID.eq(userId))
                .and(USER_BOUNTY.BOUNTY_ID.eq(bountyId))
                .and(USER_BOUNTY.STATUS.eq(BountyStatus.COMPLETED))));
  }

  public List<Long> findCompletedBountyIds(Long userId, List<Long> bountyIds) {
    if (bountyIds == null || bountyIds.isEmpty()) return List.of();
    return mapper
        .selectListByQuery(
            QueryWrapper.create()
                .select(USER_BOUNTY.BOUNTY_ID)
                .where(USER_BOUNTY.USER_ID.eq(userId))
                .and(USER_BOUNTY.BOUNTY_ID.in(bountyIds))
                .and(USER_BOUNTY.STATUS.eq(BountyStatus.COMPLETED)))
        .stream()
        .map(UserBounty::getBountyId)
        .toList();
  }
}
