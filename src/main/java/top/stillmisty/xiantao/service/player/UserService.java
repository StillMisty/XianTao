package top.stillmisty.xiantao.service.player;

import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.vo.RegisterResult;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserAuthRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.fudi.FudiService;
import top.stillmisty.xiantao.service.inventory.EquipmentService;

/** 用户服务 处理用户相关的业务逻辑 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private static final Random RANDOM = new Random();
  private static final long STARTER_SPIRIT_STONES = 100L;
  private static final String STARTER_WEAPON_NAME = "桃木剑";

  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final FudiService fudiService;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final EquipmentService equipmentService;
  private final GameEventService gameEventService;

  @Transactional
  public ServiceResult<String> changeNickname(Long userId, String newNickname) {
    if (userRepository.existsByNickname(newNickname)) {
      throw new BusinessException(ErrorCode.NICKNAME_TAKEN);
    }
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHARACTER_NOT_FOUND));
    user.setNickname(newNickname);
    userRepository.save(user);
    log.info("改号成功 - UserId: {}, NewNickname: {}", userId, newNickname);
    return new ServiceResult.Success<>("道号已改为【" + newNickname + "】");
  }

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

    var user =
        userRepository.save(
            Player.create().setNickname(nickname).setSpiritStones(STARTER_SPIRIT_STONES));

    log.info("创建用户成功 - UserId: {}, Nickname: {}", user.getId(), user.getNickname());

    UserAuth userAuth = UserAuth.init(platform, openId, user.getId());
    userAuthRepository.save(userAuth);

    log.info("创建授权记录成功 - UserId: {}, Platform: {}, OpenId: {}", user.getId(), platform, openId);

    grantStarterWeapon(user.getId());

    saveGuideEvents(user.getId());

    MBTIPersonality randomMBTI = getRandomMBTI();
    fudiService.createFudi(user.getId(), randomMBTI);

    log.info("创建福地成功 - UserId: {}, MBTI: {}", user.getId(), randomMBTI.getCode());

    return new ServiceResult.Success<>(
        new RegisterResult(true, "注册成功~", user.getId(), user.getNickname()));
  }

  /** 新手引导 — 写入事件队列，随注册回复一并送达 */
  private void saveGuideEvents(Long userId) {
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.GUIDE)
            .withNarrative("行囊中有一柄桃木剑，打开「背包」佩上它吧。可用「状态」内观己身。", Map.of()));
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.GUIDE)
            .withNarrative("青石镇乃安居之所，无从修炼。不妨「前往 翠竹林」，以「历练」采气炼体，「历练结算」收取所得。", Map.of()));
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.GUIDE)
            .withNarrative("修为圆满便可行「突破」；囊中羞涩可接「悬赏」；诸般法门尽在「帮助」。", Map.of()));
  }

  /** 随机获取一个MBTI人格类型 */
  private MBTIPersonality getRandomMBTI() {
    MBTIPersonality[] personalities = MBTIPersonality.values();
    return personalities[RANDOM.nextInt(personalities.length)];
  }

  /** 发放入门武器（不自动穿戴，留给玩家首次装备操作） */
  private void grantStarterWeapon(Long userId) {
    equipmentTemplateRepository
        .findByName(STARTER_WEAPON_NAME)
        .ifPresentOrElse(
            tmpl -> equipmentService.createEquipment(userId, tmpl.getId()),
            () -> log.warn("入门武器模板不存在: {}", STARTER_WEAPON_NAME));
    log.info(
        "发放新手礼包 - UserId: {}, 灵石: {}, 武器: {}", userId, STARTER_SPIRIT_STONES, STARTER_WEAPON_NAME);
  }
}
