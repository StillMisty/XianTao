package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.common.id.ID;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.RegisterResult;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserAuthService userAuthService;

    /**
     * 创建新用户（注册）
     *
     * @param nickname 玩家道号
     * @return 创建的用户实体
     */
    public RegisterResult createUser(PlatformType platform, ID openId, String nickname) {
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        if (userAuth.isPresent()) {
            return new RegisterResult(
                    false,
                    "阁下已在仙路中~",
                    null,
                    null
            );
        }

        var user = userRepository.save(
                User.create()
                        .setNickname(nickname)
        );

        log.info("Created user: {}", user);

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
