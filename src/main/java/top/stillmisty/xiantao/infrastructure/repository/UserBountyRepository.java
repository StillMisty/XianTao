package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.bounty.entity.table.UserBountyTableDef.USER_BOUNTY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.infrastructure.mapper.UserBountyMapper;

@Repository
@RequiredArgsConstructor
public class UserBountyRepository {

  private final UserBountyMapper mapper;

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
