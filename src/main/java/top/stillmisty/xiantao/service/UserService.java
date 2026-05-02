package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.RegisterResult;

import java.util.Optional;
import java.util.Random;

/**
 * 用户服务
 * 处理用户相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Random RANDOM = new Random();
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final FudiService fudiService;

    /**
     * 创建新用户（注册）
     *
     * @param platform 平台类型
     * @param openId   平台用户ID
     * @param nickname 玩家道号
     * @return 注册结果
     */
    @Transactional
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

        // 检查道号是否已被占用
        if (userRepository.existsByNickname(nickname)) {
            return new RegisterResult(
                    false,
                    "此道号已被他人使用，请另择佳名~",
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

        log.info(
                "创建授权记录成功 - UserId: {}, Platform: {}, OpenId: {}",
                user.getId(), platform, openId
        );

        // 自动为用户创建福地和地灵（随机分配MBTI人格）
        MBTIPersonality randomMBTI = getRandomMBTI();
        fudiService.createFudi(user.getId(), randomMBTI);

        log.info("创建福地成功 - UserId: {}, MBTI: {}", user.getId(), randomMBTI.getCode());

        return new RegisterResult(
                true,
                "注册成功~",
                user.getId(),
                user.getNickname()
        );
    }

    /**
     * 随机获取一个MBTI人格类型
     */
    private MBTIPersonality getRandomMBTI() {
        MBTIPersonality[] personalities = MBTIPersonality.values();
        return personalities[RANDOM.nextInt(personalities.length)];
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
