package top.stillmisty.xiantao.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.entity.UserAuth;
import top.stillmisty.xiantao.domain.user.enums.AttributeType;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserAuthRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户修仙游戏服务
 * 处理：注册、打坐、突破、加点等核心游戏逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCultivationService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    
    /**
     * 注册新用户（我要修仙）
     * @param platform 平台类型
     * @param openId 平台用户 ID
     * @param nickname 道号/昵称
     * @return 注册结果
     */
    public RegisterResult registerUser(PlatformType platform, String openId, String nickname) {
        // 检查是否已注册
        var userAuth = userAuthRepository.findByPlatformAndOpenId(platform, openId);
        if (userAuth.isPresent()) {
            log.warn("用户已注册：{}", openId);
            return RegisterResult.builder()
                    .success(false)
                    .message("这位道友你已入仙途了哦~")
                    .build();
        }
        // 检查昵称是否重复
        if (userRepository.existsByNickname(nickname)) {
            log.warn("昵称已存在：{}", nickname);
            return RegisterResult.builder()
                    .success(false)
                    .message("这个道号已经被别人声称过了")
                    .build();
        }
        
        // 创建新用户并保存
        User newUser = User.init();
        newUser.setNickname(nickname);
        User savedUser = userRepository.save(newUser);
        
        // 创建平台绑定并保存
        var newUserAuth = UserAuth.init(platform, openId, savedUser.getId());
        userAuthRepository.save(newUserAuth);
        
        log.info("用户注册成功：id={}, nickname={}, platform={}", savedUser.getId(), nickname, platform);
        return RegisterResult.builder()
                .success(true)
                .message("道友成功踏入仙途")
                .userId(savedUser.getId())
                .nickname(savedUser.getNickname())
                .build();
    }
    
    /**
     * 查看用户状态（状态）
     * @param userId 用户 ID
     * @return 用户状态 VO，如果用户不存在返回 null
     */
    public UserStatusVO getUserStatus(UUID userId) {
        return userRepository.findById(userId)
                .map(this::convertToUserStatusVO)
                .orElse(null);
    }
    
    /**
     * 开始打坐（打坐）
     * @param userId 用户 ID
     * @return 打坐开始结果
     */
    public MeditationStartResult startMeditation(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 检查当前状态
                    if (user.getStatus() == UserStatus.MEDITATING) {
                        return MeditationStartResult.builder()
                                .success(false)
                                .message("你已经在打坐中了")
                                .userStatus(convertToUserStatusVO(user))
                                .build();
                    }
                        
                    if (user.getStatus() != UserStatus.IDLE) {
                        return MeditationStartResult.builder()
                                .success(false)
                                .message("当前状态无法打坐，请先结束其他活动")
                                .userStatus(convertToUserStatusVO(user))
                                .build();
                    }
                        
                    // 开始打坐
                    user.setStatus(UserStatus.MEDITATING);
                    user.setAfkStartTime(LocalDateTime.now());
                    userRepository.save(user);
                        
                    return MeditationStartResult.builder()
                            .success(true)
                            .message("开始打坐，静心修炼...")
                            .userStatus(convertToUserStatusVO(user))
                            .build();
                })
                .orElse(MeditationStartResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }
    
    /**
     * 结束打坐并结算（打坐结束）
     * @param userId 用户 ID
     * @return 打坐结算结果
     */
    public MeditationEndResult endMeditation(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 检查是否在打坐中
                    if (user.getStatus() != UserStatus.MEDITATING) {
                        return MeditationEndResult.builder()
                                .success(false)
                                .message("你不在打坐状态")
                                .build();
                    }
                    
                    if (user.getAfkStartTime() == null) {
                        user.setStatus(UserStatus.IDLE);
                        userRepository.save(user);
                        return MeditationEndResult.builder()
                                .success(false)
                                .message("打坐异常结束，未获得收益")
                                .userStatus(convertToUserStatusVO(user))
                                .build();
                    }
                    
                    // 计算打坐时间
                    Duration duration = Duration.between(user.getAfkStartTime(), LocalDateTime.now());
                    long minutes = duration.toMinutes();
                    
                    if (minutes < 1) {
                        user.setStatus(UserStatus.IDLE);
                        user.setAfkStartTime(null);
                        userRepository.save(user);
                        return MeditationEndResult.builder()
                                .success(false)
                                .message("打坐时间太短，未获得收益")
                                .userStatus(convertToUserStatusVO(user))
                                .build();
                    }
                    
                    // 结算逻辑
                    int hpRestored = 0;
                    long expGained = 0;
                    boolean expCapReached = false;
                    
                    // 恢复生命值（每分钟恢复最大生命的 5%）
                    if (!user.isFullHp()) {
                        int healPerMinute = (int) (user.calculateMaxHp() * 0.05);
                        hpRestored = user.heal((int) (healPerMinute * minutes));
                    }
                    
                    // 获取经验值（满血后每分钟获得经验）
                    if (user.isFullHp()) {
                        // 基础经验：每分钟获得等级*10 的经验
                        long baseExpPerMinute = user.getLevel() * 10L;
                        long totalExp = baseExpPerMinute * minutes;
                        
                        // 限制：一次打坐最多获取当前等级升级经验的 2 倍
                        long maxExpPerSession = user.calculateExpToNextLevel() * 2;
                        long actualExp = Math.min(totalExp, maxExpPerSession);
                        
                        expGained = user.addExp(actualExp);
                        expCapReached = actualExp < totalExp;
                    }
                    
                    // 更新状态
                    user.setStatus(UserStatus.IDLE);
                    user.setAfkStartTime(null);
                    userRepository.save(user);
                    
                    // 检查是否可以升级
                    boolean canLevelUp = user.canLevelUp();
                    
                    StringBuilder message = new StringBuilder();
                    message.append("打坐结束，本次打坐时长：").append(minutes).append("分钟\n");
                    if (hpRestored > 0) {
                        message.append("恢复生命值：").append(hpRestored).append("点\n");
                    } else if (!user.isFullHp()) {
                        message.append("生命已满，无需恢复\n");
                    }
                    if (expGained > 0) {
                        message.append("获得经验值：").append(expGained).append("点\n");
                        if (expCapReached) {
                            message.append("(已达到单次打坐经验上限)\n");
                        }
                    } else if (!user.isFullHp()) {
                        message.append("生命未满，未获得经验\n");
                    }
                    if (canLevelUp) {
                        message.append("\n恭喜！经验已满，可以使用 #突破 进行升级\n");
                    }
                    
                    return MeditationEndResult.builder()
                            .success(true)
                            .message(message.toString())
                            .durationMinutes(minutes)
                            .hpRestored(hpRestored)
                            .expGained(expGained)
                            .leveledUp(canLevelUp)
                            .expCapReached(expCapReached)
                            .userStatus(convertToUserStatusVO(user))
                            .build();
                })
                .orElse(MeditationEndResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }
    
    /**
     * 尝试突破升级（突破）
     * @param userId 用户 ID
     * @return 突破结果
     */
    public BreakthroughResult attemptBreakthrough(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 检查是否可以升级
                    if (!user.canLevelUp()) {
                        return BreakthroughResult.builder()
                                .success(false)
                                .message("经验不足，无法突破")
                                .build();
                    }
                    
                    // 计算成功率
                    double successRate = user.calculateBreakthroughSuccessRate();
                    boolean success = user.attemptBreakthrough();
                    
                    // 保存用户
                    userRepository.save(user);
                    
                    StringBuilder message = new StringBuilder();
                    message.append("突破").append(success ? "成功" : "失败").append("！\n");
                    message.append("成功率：").append(String.format("%.1f%%", successRate * 100)).append("\n");
                    
                    if (success) {
                        message.append("恭喜突破到 ").append(user.getLevel()).append(" 级！\n");
                        message.append("获得 1 点自由属性点，当前共有").append(user.getFreeStatPoints()).append("点\n");
                        message.append("生命值已回满\n");
                    } else {
                        message.append("突破失败，失败次数：").append(user.getBreakthroughFailCount()).append("\n");
                        message.append("下次成功率：").append(String.format("%.1f%%", user.calculateBreakthroughSuccessRate() * 100)).append("\n");
                        message.append("不要气馁，继续修炼！\n");
                    }
                    
                    return BreakthroughResult.builder()
                            .success(true)
                            .message(message.toString())
                            .breakthroughSuccess(success)
                            .successRate(successRate)
                            .newLevel(user.getLevel())
                            .freeStatPoints(user.getFreeStatPoints())
                            .failCount(user.getBreakthroughFailCount())
                            .nextBreakthroughRate(user.calculateBreakthroughSuccessRate())
                            .userStatus(convertToUserStatusVO(user))
                            .build();
                })
                .orElse(BreakthroughResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }
    
    /**
     * 分配属性点（加点）
     * @param userId 用户 ID
     * @param statType 属性类型
     * @param points 点数
     * @return 分配结果
     */
    public StatAllocationResult allocateStatPoints(UUID userId, AttributeType statType, int points) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 验证点数
                    if (points <= 0) {
                        return StatAllocationResult.builder()
                                .success(false)
                                .message("点数必须为正数")
                                .build();
                    }
                    
                    if (user.getFreeStatPoints() < points) {
                        return StatAllocationResult.builder()
                                .success(false)
                                .message("自由属性点不足，当前只有" + user.getFreeStatPoints() + "点")
                                .build();
                    }
                    
                    // 分配属性点
                    boolean success = user.allocateStatPoints(statType, points);
                    if (!success) {
                        return StatAllocationResult.builder()
                                .success(false)
                                .message("属性名称无效，请使用：str(力量), con(体质), agi(敏捷), wis(智慧)")
                                .build();
                    }
                    
                    // 保存用户
                    userRepository.save(user);
                    
                    return StatAllocationResult.builder()
                            .success(true)
                            .message(String.format(
                                    "成功分配%d点%s属性",
                                    points, statType.getName()))
                            .statType(statType)
                            .pointsAllocated(points)
                            .remainingFreePoints(user.getFreeStatPoints())
                            .currentStr(user.getStatStr())
                            .currentCon(user.getStatCon())
                            .currentAgi(user.getStatAgi())
                            .currentWis(user.getStatWis())
                            .userStatus(convertToUserStatusVO(user))
                            .build();
                })
                .orElse(StatAllocationResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }
    
    /**
     * 转换为用户状态 VO
     */
    private UserStatusVO convertToUserStatusVO(User user) {
        UserStatusVO.UserStatusVOBuilder builder = UserStatusVO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .level(user.getLevel())
                .exp(user.getExp())
                .expToNextLevel(user.calculateExpToNextLevel())
                .status(user.getStatus())
                .statusName(user.getStatus().getName())
                .hpCurrent(user.getHpCurrent())
                .hpMax(user.calculateMaxHp())
                .hpPercentage(user.getHpCurrent() * 100.0 / user.calculateMaxHp())
                .statStr(user.getStatStr())
                .statCon(user.getStatCon())
                .statAgi(user.getStatAgi())
                .statWis(user.getStatWis())
                .freeStatPoints(user.getFreeStatPoints())
                .coins(user.getCoins())
                .spiritStones(user.getSpiritStones())
                .breakthroughFailCount(user.getBreakthroughFailCount())
                .nextBreakthroughRate(user.calculateBreakthroughSuccessRate())
                .afkStartTime(user.getAfkStartTime());
        
        // 如果是打坐状态，计算已打坐时长
        if (user.getStatus() == UserStatus.MEDITATING && user.getAfkStartTime() != null) {
            Duration duration = Duration.between(user.getAfkStartTime(), LocalDateTime.now());
            builder.meditationDurationMinutes(duration.toMinutes());
        }
        
        return builder.build();
    }
}