package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.user.entity.table.UserAuthTableDef.USER_AUTH;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.mapper.UserAuthMapper;

@Repository
@RequiredArgsConstructor
public class UserAuthRepository {
  private final UserAuthMapper userAuthMapper;

  @Cacheable(value = "userAuth", key = "#platform.code + ':' + #openId")
  public Optional<UserAuth> findByPlatformAndOpenId(PlatformType platform, String openId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(USER_AUTH.PLATFORM.eq(platform))
            .and(USER_AUTH.PLATFORM_OPEN_ID.eq(openId));
    return Optional.ofNullable(userAuthMapper.selectOneByQuery(query));
  }

  @CacheEvict(value = "userAuth", key = "#userAuth.platform.code + ':' + #userAuth.platformOpenId")
  public UserAuth save(UserAuth userAuth) {
    userAuthMapper.insertOrUpdateSelective(userAuth);
    return userAuth;
  }
}
