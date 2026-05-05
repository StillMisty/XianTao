package top.stillmisty.xiantao.domain.user.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

public interface UserAuthRepository {
  /** 根据平台和OpenID查找用户 */
  Optional<UserAuth> findByPlatformAndOpenId(PlatformType platform, String openId);

  /** 保存用户 */
  UserAuth save(UserAuth userAuth);
}
