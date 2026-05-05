package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.bounty.entity.table.UserBountyTableDef.USER_BOUNTY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.repository.UserBountyRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserBountyMapper;

@Repository
@RequiredArgsConstructor
public class UserBountyRepositoryImpl implements UserBountyRepository {

  private final UserBountyMapper mapper;

  @Override
  public Optional<UserBounty> findActiveByUserId(Long userId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .where(USER_BOUNTY.USER_ID.eq(userId))
                .and(USER_BOUNTY.STATUS.eq("active"))));
  }

  @Override
  public void save(UserBounty userBounty) {
    mapper.insert(userBounty);
  }

  @Override
  public void update(UserBounty userBounty) {
    mapper.update(userBounty);
  }
}
