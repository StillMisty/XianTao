package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserAuthMapper;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {
  private final UserAuthMapper userAuthMapper;

  @Override
  @Cacheable(value = "userAuth", key = "#platform.code + ':' + #openId")
  public Optional<UserAuth> findByPlatformAndOpenId(PlatformType platform, String openId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(UserAuth::getPlatform, platform)
            .eq(UserAuth::getPlatformOpenId, openId);
    return Optional.ofNullable(userAuthMapper.selectOneByQuery(query));
  }

  @Override
  @CacheEvict(value = "userAuth", key = "#userAuth.platform.code + ':' + #userAuth.platformOpenId")
  public UserAuth save(UserAuth userAuth) {
    userAuthMapper.insertOrUpdateSelective(userAuth);
    return userAuth;
  }
}
