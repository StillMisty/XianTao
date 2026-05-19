package top.stillmisty.xiantao.domain.bounty.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;

public interface UserBountyRepository {

  Optional<UserBounty> findActiveByUserId(Long userId);

  Optional<UserBounty> findActiveByUserIdForUpdate(Long userId);

  void save(UserBounty userBounty);
}
