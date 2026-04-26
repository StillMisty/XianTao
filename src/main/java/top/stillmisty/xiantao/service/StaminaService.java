package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/**
 * 体力服务
 * 处理体力消耗、恢复、查询等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaminaService {

    // 体力消耗常量
    private static final int STAMINA_COST_PER_EXPLORATION = 20; // 探索每次消耗20点
    private static final int STAMINA_COST_PER_TRAVEL_MINUTE = 5;
    private static final int STAMINA_RECOVERY_PER_MEDITATION_MINUTE = 2; // 打坐每分钟恢复2点
    private final UserRepository userRepository;

    /**
     * 获取用户体力信息
     *
     * @param userId 用户ID
     * @return 体力信息字符串
     */
    public String getStaminaInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 懒加载计算离线恢复
        int recovered = user.calculateOfflineStaminaRecovery();
        userRepository.save(user);

        int currentStamina = user.getStaminaCurrent();
        int maxStamina = user.calculateMaxStamina();
        int percent = (int) ((double) currentStamina / maxStamina * 100);

        StringBuilder sb = new StringBuilder();
        sb.append("💪 【体力状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("当前体力：").append(currentStamina).append("/").append(maxStamina)
                .append(" (").append(percent).append("%)\n");

        if (recovered > 0) {
            sb.append("离线恢复：+").append(recovered).append(" 体力\n");
        }

        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 体力影响探索和旅行，可通过打坐或使用丹药恢复");

        return sb.toString();
    }

    /**
     * 检查并消耗旅行体力
     *
     * @param userId        用户ID
     * @param travelMinutes 旅行时长（分钟）
     * @return 是否成功，失败时返回错误消息
     */
    public StaminaCheckResult checkAndConsumeTravelStamina(Long userId, int travelMinutes) {
        User user = userRepository.findById(userId).orElseThrow();

        // 懒加载计算离线恢复
        user.calculateOfflineStaminaRecovery();

        int requiredStamina = travelMinutes * STAMINA_COST_PER_TRAVEL_MINUTE;

        if (!user.hasEnoughStamina(requiredStamina)) {
            return StaminaCheckResult.failure(
                    String.format(
                            "体力不足！需要 %d 点体力，当前仅有 %d 点",
                            requiredStamina, user.getStaminaCurrent()
                    )
            );
        }

        // 消耗体力
        int consumed = user.consumeStamina(requiredStamina);
        userRepository.save(user);

        log.info("用户 {} 消耗 {} 点体力用于 {} 分钟旅行", userId, consumed, travelMinutes);

        return StaminaCheckResult.success(consumed, user.getStaminaCurrent());
    }

    /**
     * 检查并消耗探索体力
     *
     * @param userId 用户ID
     * @return 是否成功，失败时返回错误消息
     */
    public StaminaCheckResult checkAndConsumeExplorationStamina(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 懒加载计算离线恢复
        user.calculateOfflineStaminaRecovery();

        if (!user.hasEnoughStamina(STAMINA_COST_PER_EXPLORATION)) {
            return StaminaCheckResult.failure(
                    String.format(
                            "体力不足！探索需要 %d 点体力，当前仅有 %d 点。请先打坐恢复体力",
                            STAMINA_COST_PER_EXPLORATION, user.getStaminaCurrent()
                    )
            );
        }

        // 消耗体力
        int consumed = user.consumeStamina(STAMINA_COST_PER_EXPLORATION);
        userRepository.save(user);

        log.info("用户 {} 消耗 {} 点体力用于探索", userId, consumed);

        return StaminaCheckResult.success(consumed, user.getStaminaCurrent());
    }

    /**
     * 打坐恢复体力
     *
     * @param userId          用户ID
     * @param durationMinutes 打坐时长（分钟）
     * @return 恢复的体力值
     */
    public int restoreStaminaByMeditation(Long userId, long durationMinutes) {
        User user = userRepository.findById(userId).orElseThrow();

        // 计算恢复量
        int recoveryAmount = (int) (durationMinutes * STAMINA_RECOVERY_PER_MEDITATION_MINUTE);

        // 恢复体力
        int actualRecovered = user.restoreStamina(recoveryAmount);
        userRepository.save(user);

        log.info("用户 {} 通过打坐 {} 分钟恢复 {} 点体力", userId, durationMinutes, actualRecovered);

        return actualRecovered;
    }

    /**
     * 使用丹药恢复体力
     *
     * @param userId        用户ID
     * @param staminaAmount 恢复的体力值
     * @return 实际恢复的体力值
     */
    public int restoreStaminaByItem(Long userId, int staminaAmount) {
        User user = userRepository.findById(userId).orElseThrow();

        int actualRecovered = user.restoreStamina(staminaAmount);
        userRepository.save(user);

        log.info("用户 {} 使用丹药恢复 {} 点体力", userId, actualRecovered);

        return actualRecovered;
    }

    /**
     * 体力检查结果
     */
    public record StaminaCheckResult(
            boolean success,
            String message,
            int consumedStamina,
            int remainingStamina
    ) {
        public static StaminaCheckResult success(int consumed, int remaining) {
            return new StaminaCheckResult(true, null, consumed, remaining);
        }

        public static StaminaCheckResult failure(String message) {
            return new StaminaCheckResult(false, message, 0, 0);
        }
    }
}
