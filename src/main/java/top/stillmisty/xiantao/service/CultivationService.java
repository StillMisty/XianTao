package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.service.annotation.Authenticated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 修仙核心服务
 * 处理突破、护道等核心修仙机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CultivationService {

    // 护道系统常量
    private static final int MAX_PROTECTOR_COUNT = 3; // 护道者最多同时为3人护道
    private static final double BASE_BONUS_PERCENTAGE = 5.0; // 基础加成5%
    private static final double LEVEL_DIFF_BONUS_PERCENTAGE = 1.0; // 每级差距额外1%
    private static final double MAX_TOTAL_BONUS_PERCENTAGE = 20.0; // 总加成上限20%
    private final UserRepository userRepository;
    private final MapService mapService;
    private final DaoProtectionRepository daoProtectionRepository;
    private final PlayerBuffRepository playerBuffRepository;
    // ===================== 公开 API（含认证） =====================

    @Authenticated
    @Transactional
    public ServiceResult<BreakthroughResult> attemptBreakthrough(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(attemptBreakthrough(userId));
    }

    @Authenticated
    @Transactional
    public ServiceResult<DaoProtectionResult> establishProtection(PlatformType platform, String openId, String protegeNickname) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(establishProtection(userId, protegeNickname));
    }

    @Authenticated
    @Transactional
    public ServiceResult<DaoProtectionResult> removeProtection(PlatformType platform, String openId, String protegeNickname) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(removeProtection(userId, protegeNickname));
    }

    @Authenticated
    public ServiceResult<DaoProtectionQueryResult> queryProtectionInfo(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(queryProtectionInfo(userId));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 突破境界
     *
     * @param userId 用户ID
     * @return 突破结果
     */
    @Transactional
    public BreakthroughResult attemptBreakthrough(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 检查修为是否足够
        long expNeeded = user.calculateExpToNextLevel();
        if (user.getExp() < expNeeded) {
            return BreakthroughResult.builder()
                    .success(false)
                    .message(String.format("修为不足，突破需要 %d 修为，当前仅有 %d 修为", expNeeded, user.getExp()))
                    .successRate(user.calculateBreakthroughSuccessRate())
                    .newLevel(user.getLevel())
                    .failCount(user.getBreakthroughFailCount())
                    .nextBreakthroughRate(user.calculateBreakthroughSuccessRate())
                    .build();
        }

        // 计算护道加成
        double protectionBonus = calculateProtectionBonus(user);

        // 计算突破丹药加成
        List<PlayerBuff> breakthroughBuffs = playerBuffRepository.findActiveByUserIdAndType(user.getId(), "breakthrough");
        double pillBonus = breakthroughBuffs.stream().mapToInt(PlayerBuff::getValue).sum();

        // 计算最终成功率
        double baseSuccessRate = user.calculateBreakthroughSuccessRate();
        double finalSuccessRate = Math.min(100.0, baseSuccessRate + protectionBonus + pillBonus);

        // 执行突破判定
        boolean breakthroughSuccess = Math.random() * 100 < finalSuccessRate;

        if (breakthroughSuccess) {
            // 突破成功
            int oldLevel = user.getLevel();
            user.setLevel(oldLevel + 1);
            user.setExp(user.getExp() - expNeeded);
            user.setBreakthroughFailCount(0);
            user.setHpCurrent(user.calculateMaxHp()); // 恢复满血

            // 清除该用户的所有护道关系
            clearProtegeRelations(userId);

            // 清除突破丹药加成
            playerBuffRepository.deleteByUserIdAndType(userId, "breakthrough");

            userRepository.save(user);

            return BreakthroughResult.builder()
                    .success(true)
                    .message(String.format("恭喜！突破成功！晋升至第%d层！", user.getLevel()))
                    .breakthroughSuccess(true)
                    .successRate(finalSuccessRate)
                    .newLevel(user.getLevel())
                    .failCount(0)
                    .nextBreakthroughRate(user.calculateBreakthroughSuccessRate())
                    .build();
        } else {
            // 突破失败
            // 扣除当前境界升至下境界所需的标准修为值
            long newExp = Math.max(0, user.getExp() - expNeeded);
            user.setExp(newExp);
            user.setBreakthroughFailCount(user.getBreakthroughFailCount() + 1);

            // 清除该用户的所有护道关系
            clearProtegeRelations(userId);

            // 清除突破丹药加成
            playerBuffRepository.deleteByUserIdAndType(userId, "breakthrough");

            userRepository.save(user);

            return BreakthroughResult.builder()
                    .success(true)
                    .message(String.format("突破失败！道基反噬，损失 %d 修为，当前修为 %d", expNeeded, newExp))
                    .breakthroughSuccess(false)
                    .successRate(finalSuccessRate)
                    .newLevel(user.getLevel())
                    .failCount(user.getBreakthroughFailCount())
                    .nextBreakthroughRate(user.calculateBreakthroughSuccessRate())
                    .build();
        }
    }

    /**
     * 建立护道关系
     *
     * @param protectorId     护道者ID
     * @param protegeNickname 被护道者的道号
     * @return 护道结果
     */
    @Transactional
    public DaoProtectionResult establishProtection(Long protectorId, String protegeNickname) {
        User protector = userRepository.findById(protectorId).orElseThrow();

        // 查找被护道者
        Optional<User> protegeOpt = findUserByNickname(protegeNickname);
        if (protegeOpt.isEmpty()) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format("未找到道号为【%s】的修士", protegeNickname))
                    .build();
        }

        User protege = protegeOpt.get();

        // 检查境界压制：护道者境界必须 >= 被护道者境界
        if (protector.getLevel() < protege.getLevel()) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format(
                            "你的境界（第%d层）低于%s（第%d层），无法为其护道",
                            protector.getLevel(), protege.getNickname(), protege.getLevel()
                    ))
                    .build();
        }

        // 检查护道者是否已达上限
        long currentProtectingCount = daoProtectionRepository.countByProtectorId(protectorId);
        if (currentProtectingCount >= MAX_PROTECTOR_COUNT) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format("你当前已在为%d位道友护道，分身乏术。请先使用「护道解除」解除部分关系", MAX_PROTECTOR_COUNT))
                    .build();
        }

        // 检查是否已存在护道关系
        Optional<DaoProtection> existingRelation = daoProtectionRepository.findByProtectorAndProtege(protectorId, protege.getId());
        if (existingRelation.isPresent()) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format("你已在为%s护道", protege.getNickname()))
                    .build();
        }

        // 创建护道关系
        DaoProtection protection = DaoProtection.create()
                .setProtectorId(protectorId)
                .setProtegeId(protege.getId());
        daoProtectionRepository.save(protection);

        // 计算单人加成
        double singleBonus = calculateSingleProtectorBonus(protector, protege);

        return DaoProtectionResult.builder()
                .success(true)
                .message(String.format(
                        "已与%s建立护道契约！当其在同地点突破时，你将提供 %.1f%% 的成功率加成",
                        protege.getNickname(), singleBonus
                ))
                .protectorId(protector.getId())
                .protectorName(protector.getNickname())
                .protectorLevel(protector.getLevel())
                .protegeId(protege.getId())
                .protegeName(protege.getNickname())
                .protegeLevel(protege.getLevel())
                .singleProtectorBonus(singleBonus)
                .isInSameLocation(isInSameLocation(protector, protege))
                .build();
    }

    /**
     * 解除护道关系
     *
     * @param protectorId     护道者ID
     * @param protegeNickname 被护道者的道号
     * @return 解除结果
     */
    @Transactional
    public DaoProtectionResult removeProtection(Long protectorId, String protegeNickname) {
        // 查找被护道者
        Optional<User> protegeOpt = findUserByNickname(protegeNickname);
        if (protegeOpt.isEmpty()) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format("未找到道号为【%s】的修士", protegeNickname))
                    .build();
        }

        User protege = protegeOpt.get();

        // 查找护道关系
        Optional<DaoProtection> protectionOpt = daoProtectionRepository.findByProtectorAndProtege(protectorId, protege.getId());
        if (protectionOpt.isEmpty()) {
            return DaoProtectionResult.builder()
                    .success(false)
                    .message(String.format("你并未为%s护道", protege.getNickname()))
                    .build();
        }

        // 删除护道关系
        daoProtectionRepository.deleteById(protectionOpt.get().getId());

        return DaoProtectionResult.builder()
                .success(true)
                .message(String.format("已解除与%s的护道契约", protege.getNickname()))
                .protectorId(protectorId)
                .protegeId(protege.getId())
                .protegeName(protege.getNickname())
                .build();
    }

    /**
     * 查询护道信息
     *
     * @param userId 用户ID
     * @return 护道查询结果
     */
    public DaoProtectionQueryResult queryProtectionInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 查询正在为谁护道
        List<DaoProtection> protectingList = daoProtectionRepository.findByProtectorId(userId);
        List<ProtectionInfo> protectingInfoList = new ArrayList<>();

        if (!protectingList.isEmpty()) {
            var protegeIds = protectingList.stream().map(DaoProtection::getProtegeId).distinct().toList();
            var protegeMap = userRepository.findByIds(protegeIds).stream()
                    .collect(java.util.stream.Collectors.toMap(top.stillmisty.xiantao.domain.user.entity.User::getId, u -> u));

            for (DaoProtection protection : protectingList) {
                User protege = protegeMap.get(protection.getProtegeId());
                if (protege != null) {
                    boolean inSameLocation = isInSameLocation(user, protege);
                    double bonus = calculateSingleProtectorBonus(user, protege);

                    protectingInfoList.add(ProtectionInfo.builder()
                                                   .userId(protege.getId())
                                                   .userName(protege.getNickname())
                                                   .userLevel(protege.getLevel())
                                                   .locationId(protege.getLocationId())
                                                    .locationName(mapService.getMapName(protege.getLocationId()))
                                                   .isInSameLocation(inSameLocation)
                                                   .bonusPercentage(bonus)
                                                   .build());
                }
            }
        }

        // 查询有谁在为自己护道
        List<DaoProtection> protectedByList = daoProtectionRepository.findByProtegeId(userId);
        List<ProtectionInfo> protectedByInfoList = new ArrayList<>();
        double totalBonus = 0.0;
        int sameLocationCount = 0;

        if (!protectedByList.isEmpty()) {
            var protectorIds = protectedByList.stream().map(DaoProtection::getProtectorId).distinct().toList();
            var protectorMap = userRepository.findByIds(protectorIds).stream()
                    .collect(java.util.stream.Collectors.toMap(top.stillmisty.xiantao.domain.user.entity.User::getId, u -> u));

            for (DaoProtection protection : protectedByList) {
                User protector = protectorMap.get(protection.getProtectorId());
                if (protector != null) {
                    boolean inSameLocation = isInSameLocation(user, protector);

                    // 只有同地点才提供加成
                    double bonus = 0.0;
                    if (inSameLocation) {
                        bonus = calculateSingleProtectorBonus(protector, user);
                        totalBonus += bonus;
                        sameLocationCount++;
                    }

                    protectedByInfoList.add(ProtectionInfo.builder()
                                                    .userId(protector.getId())
                                                    .userName(protector.getNickname())
                                                    .userLevel(protector.getLevel())
                                                    .locationId(protector.getLocationId())
                                                     .locationName(mapService.getMapName(protector.getLocationId()))
                                                    .isInSameLocation(inSameLocation)
                                                    .bonusPercentage(bonus)
                                                    .build());
                }
            }
        }

        // 限制总加成上限
        totalBonus = Math.min(MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);

        String message;
        if (protectedByInfoList.isEmpty()) {
            message = "天地孤寂，无道友相护。";
        } else if (sameLocationCount == 0) {
            message = "虽有道友护道，但皆不在同地点，无法提供加成。";
        } else {
            message = String.format(
                    "共有 %d 位道友为你护道，其中 %d 位在同地点，总加成 %.1f%%",
                    protectedByInfoList.size(), sameLocationCount, totalBonus
            );
        }

        return DaoProtectionQueryResult.builder()
                .success(true)
                .message(message)
                .protectingList(protectingInfoList)
                .protectingCount(protectingInfoList.size())
                .maxProtectingCount(MAX_PROTECTOR_COUNT)
                .protectedByList(protectedByInfoList)
                .totalBonusPercentage(totalBonus)
                .allInSameLocation(sameLocationCount == protectedByInfoList.size() && !protectedByInfoList.isEmpty())
                .build();
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 根据道号查找用户
     */
    private Optional<User> findUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    /**
     * 计算单个护道者的加成
     * 公式：5% + (护道者境界层级 - 突破者境界层级) × 1%
     */
    private double calculateSingleProtectorBonus(User protector, User protege) {
        int levelDiff = protector.getLevel() - protege.getLevel();
        return BASE_BONUS_PERCENTAGE + (levelDiff * LEVEL_DIFF_BONUS_PERCENTAGE);
    }

    /**
     * 计算总护道加成（考虑位置）
     */
    private double calculateProtectionBonus(User protege) {
        List<DaoProtection> protections = daoProtectionRepository.findByProtegeId(protege.getId());
        double totalBonus = 0.0;

        for (DaoProtection protection : protections) {
            Optional<User> protectorOpt = userRepository.findById(protection.getProtectorId());
            if (protectorOpt.isPresent()) {
                User protector = protectorOpt.get();

                // 检查是否在同地点
                if (isInSameLocation(protector, protege)) {
                    totalBonus += calculateSingleProtectorBonus(protector, protege);
                }
            }
        }

        // 限制上限
        return Math.min(MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);
    }

    /**
     * 检查两个用户是否在同一地点
     */
    private boolean isInSameLocation(User user1, User user2) {
        return user1.getLocationId().equals(user2.getLocationId());
    }

    /**
     * 清除被护道者的所有护道关系
     */
    private void clearProtegeRelations(Long protegeId) {
        daoProtectionRepository.deleteByProtegeId(protegeId);
    }

}
