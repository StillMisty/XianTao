package top.stillmisty.xiantao.service;

import java.util.Random;
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

/** 用户服务 处理用户相关的业务逻辑 */
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
   * @param openId 平台用户ID
   * @param nickname 玩家道号
   * @return 注册结果
   */
  @Transactional
  public ServiceResult<RegisterResult> createUser(
      PlatformType platform, String openId, String nickname) {
    var existingAuth = userAuthRepository.findByPlatformAndOpenId(platform, openId);
    if (existingAuth.isPresent()) {
      return ServiceResult.businessFailure("阁下已在仙路中~");
    }

    if (userRepository.existsByNickname(nickname)) {
      return ServiceResult.businessFailure("此道号已被他人使用，请另择佳名~");
    }

    var user = userRepository.save(User.create().setNickname(nickname));

    log.info("创建用户成功 - UserId: {}, Nickname: {}", user.getId(), user.getNickname());

    UserAuth userAuth = UserAuth.init(platform, openId, user.getId());
    userAuthRepository.save(userAuth);

    log.info("创建授权记录成功 - UserId: {}, Platform: {}, OpenId: {}", user.getId(), platform, openId);

    MBTIPersonality randomMBTI = getRandomMBTI();
    fudiService.createFudi(user.getId(), randomMBTI);

    log.info("创建福地成功 - UserId: {}, MBTI: {}", user.getId(), randomMBTI.getCode());

    return new ServiceResult.Success<>(
        new RegisterResult(true, "注册成功~", user.getId(), user.getNickname()));
  }

  /** 随机获取一个MBTI人格类型 */
  private MBTIPersonality getRandomMBTI() {
    MBTIPersonality[] personalities = MBTIPersonality.values();
    return personalities[RANDOM.nextInt(personalities.length)];
  }
}
