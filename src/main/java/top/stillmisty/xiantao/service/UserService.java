package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.common.id.ID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
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
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final FudiService fudiService;
    private final MapNodeRepository mapNodeRepository;
    private static final Random RANDOM = new Random();
    
    // 默认起始地图名称
    private static final String DEFAULT_START_MAP_NAME = "黑金主城";

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

        // 检查道号是否已被占用
        if (userRepository.existsByNickname(nickname)) {
            return new RegisterResult(
                    false,
                    "此道号已被他人使用，请另择佳名~",
                    null,
                    null
            );
        }

        // 获取默认起始地图ID
        Long defaultLocationId = mapNodeRepository.findByName(DEFAULT_START_MAP_NAME)
                .map(top.stillmisty.xiantao.domain.map.entity.MapNode::getId)
                .orElse(1L); // 如果找不到默认地图，使用ID 1作为后备

        // 创建用户并设置初始位置
        var user = userRepository.save(
                User.create()
                        .setNickname(nickname)
                        .setLocationId(defaultLocationId)
        );

        log.info("创建用户成功 - UserId: {}, Nickname: {}, LocationId: {}", 
                user.getId(), user.getNickname(), user.getLocationId());

        // 创建授权记录
        UserAuth userAuth = UserAuth.init(platform, openId, user.getId());
        userAuthRepository.save(userAuth);
        
        log.info("创建授权记录成功 - UserId: {}, Platform: {}, OpenId: {}", 
                user.getId(), platform, openId);

        // 自动为用户创建福地和地灵（随机分配MBTI人格）
        MBTIPersonality randomMbti = getRandomMBTI();
        fudiService.createFudi(user.getId(), randomMbti);
        
        log.info("创建福地成功 - UserId: {}, MBTI: {}", user.getId(), randomMbti.getCode());

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
