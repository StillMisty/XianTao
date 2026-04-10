package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.common.id.ID;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.repository.UserAuthRepositoryImpl;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserAuthRepositoryImpl userAuthRepository;

    public Optional<UserAuth> findUserIdByOpenId(PlatformType platform, ID openId) {
        return userAuthRepository.findByPlatformAndOpenId(platform, openId.toString());
    }

    public Optional<UserAuth> findUserIdByOpenId(PlatformType platform, String openId) {
        return userAuthRepository.findByPlatformAndOpenId(platform, openId);
    }
    
    /**
     * 保存用户授权
     * @param userAuth 用户授权实体
     * @return 保存后的实体
     */
    public UserAuth save(UserAuth userAuth) {
        return userAuthRepository.save(userAuth);
    }
}
