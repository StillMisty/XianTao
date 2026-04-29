package top.stillmisty.xiantao.domain.bounty.repository;

import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;

import java.util.Optional;

public interface UserBountyRepository {

    Optional<UserBounty> findActiveByUserId(Long userId);

    void save(UserBounty userBounty);

    void update(UserBounty userBounty);
}