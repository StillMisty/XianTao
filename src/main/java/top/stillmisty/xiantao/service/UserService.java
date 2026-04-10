package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.common.id.ID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.RegisterResult;

import java.util.Optional;

/**
 * 用户服务
 * 处理用户相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    /**
     * 创建新用户（注册）
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param nickname 玩家道号
     * @return 注册结果
     */
    public RegisterResult createUser(PlatformType platform, String openId, String nickname) {
        // 检查是否已注册
        var existingAuth = userAuthRepository.findByPlatformAndOpenId(platform, openId);
        if (existingAuth.isPresent()) {
            return new RegisterResult(
                    false,
                    "阁下已在仙路中~",
                    null,
                    null
            );
        }

        // 创建用户
        var user = userRepository.save(
                User.create()
                        .setNickname(nickname)
        );

        log.info("创建用户成功 - UserId: {}, Nickname: {}", user.getId(), user.getNickname());

        // 创建授权记录
        UserAuth userAuth = UserAuth.init(platform, openId, user.getId());
        userAuthRepository.save(userAuth);
        
        log.info("创建授权记录成功 - UserId: {}, Platform: {}, OpenId: {}", 
                user.getId(), platform, openId);

        return new RegisterResult(
                true,
                "注册成功~",
                user.getId(),
                user.getNickname()
        );
    }

    /**
     * 根据ID查找用户
     *
     * @param userId 用户ID
     * @return 用户实体（Optional）
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
