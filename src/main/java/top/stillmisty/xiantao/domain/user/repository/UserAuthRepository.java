package top.stillmisty.xiantao.domain.user.repository;

import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.util.Optional;

public interface UserAuthRepository {
    /**
     * 根据平台和OpenID查找用户
     */
    Optional<UserAuth> findByPlatformAndOpenId(PlatformType platform, String openId);

    /**
     * 保存用户
     */
    UserAuth save(UserAuth userAuth);
}
