package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;

import java.util.Optional;

/**
 * 用户授权服务
 * 处理用户与平台的绑定关系
 */
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserAuthRepository userAuthRepository;

    /**
     * 根据平台和OpenID查找用户授权
     *
     * @param platform 平台类型
     * @param openId   平台用户ID字符串
     * @return 用户授权信息
     */
    public Optional<UserAuth> findUserIdByOpenId(PlatformType platform, String openId) {
        return userAuthRepository.findByPlatformAndOpenId(platform, openId);
    }

    /**
     * 保存用户授权
     *
     * @param userAuth 用户授权实体
     * @return 保存后的实体
     */
    public UserAuth save(UserAuth userAuth) {
        return userAuthRepository.save(userAuth);
    }
}
