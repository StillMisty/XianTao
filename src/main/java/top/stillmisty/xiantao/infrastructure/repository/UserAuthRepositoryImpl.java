package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserAuthMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {
    private final UserAuthMapper userAuthMapper;

    @Override
    public Optional<UserAuth> findByPlatformAndOpenId(PlatformType platform, String openId) {
        QueryWrapper query = new QueryWrapper()
                .eq(UserAuth::getPlatform, platform)
                .eq(UserAuth::getPlatformOpenId, openId);
        return Optional.ofNullable(userAuthMapper.selectOneByQuery(query));
    }

    @Override
    public UserAuth save(UserAuth userAuth) {
        userAuthMapper.insertOrUpdate(userAuth);
        return userAuth;
    }
}
